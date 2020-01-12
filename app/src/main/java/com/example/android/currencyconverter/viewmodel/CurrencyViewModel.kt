package com.example.android.currencyconverter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.android.currencyconverter.model.ApiResponse
import com.example.android.currencyconverter.model.Currencies
import com.example.android.currencyconverter.model.Currency
import com.example.android.currencyconverter.repository.CurrencyConverterRepository
import com.example.android.currencyconverter.utils.ERROR_SERVER
import com.example.android.currencyconverter.utils.RATES_FETCH_PERIOD
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

class CurrencyViewModel(application: Application) : AndroidViewModel(application) {

    // Task that will take care of fetching latest rates every Utils.RATES_FETCH_PERIOD
    private var mFetcherTask: TimerTask? = null
    // Information about the current selected currency
    var mSelectedCurrency: Currency? = null
    // Repository that handle fetching data
    private var currencyConverterRepository = CurrencyConverterRepository()

    // liveData holding Currencies Model data
    val mCurrencyList: MutableLiveData<Currencies> = MutableLiveData()
    // liveData to notify UI of error
    val mErrorMessage: MutableLiveData<String> = MutableLiveData()

    /**
     * Process that will retrieve currencies list from server.
     * It is called only once when launching app.Because list of currencies
     * doesn't change that often, so it is not necessary to update it like we update rates.
     *
     */
    private fun getAvailableCurrency() {
        currencyConverterRepository.getCurrencyNameList().enqueue(object : Callback<ApiResponse> {

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.success == "true") {
                            onGetAvailableCurrencySuccess(it.currencies)
                        } else {
                            mErrorMessage.value = ERROR_SERVER
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                mErrorMessage.value = ERROR_SERVER
            }
        })
    }

    /**
     * Callback when succeed to retrieve list of currencies. Update LiveData and SharedPreferences
     */
    private fun onGetAvailableCurrencySuccess(currenciesList: Map<String, String>) {
        val currencies = Currencies(mutableListOf(null))
        currenciesList.forEach { (key, value) -> currencies.currencies.add(Currency(key, value)) }
        // write in SharedPreferences, but not the timestamp because there is not the rates.
        currencyConverterRepository.writeCurrencyRatesListToSharedPref(
            getApplication(),
            currencies
        )
        // Update liveData
        mCurrencyList.value = currencies
    }

    /**
     * Update local information about currently selected currency
     */
    fun updateCurrentSelectedCurrency(id: Int) {
        if (id == 0) return
        val currency = mCurrencyList.value?.currencies?.get(id)
        currency?.let { mSelectedCurrency = currency }
    }

    /**
     * Process that will ask repository to access server in order to retrieve rates information.
     * TODO May be logic to move this to repository logic
     */
    private fun updateExchangeRates() {
        currencyConverterRepository.getCurrencyRatesList().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.success == "true") {
                            onGetUpdateRatesSuccess(it.quotes, it.timestamp)
                        } else {
                            mErrorMessage.value = ERROR_SERVER
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                mErrorMessage.value = ERROR_SERVER
            }
        })
    }

    /**
     * CallBack when Rates has been retrieved successfully.
     */
    private fun onGetUpdateRatesSuccess(quotes: Map<String, BigDecimal>, timestamp: Long) {
        // if LiveData is not null, update rates for all currencies
        mCurrencyList.value?.let {
            it.currencies.forEach { currency -> currency?.usdRate = quotes["USD" + currency?.code] }
            // Update in the same time SharedPreferences
            currencyConverterRepository.writeCurrencyRatesListToSharedPref(
                getApplication(),
                it,
                timestamp
            )
        }
    }

    /**
     * Function that converts "amount" in all currency using mSelectedCurrency.
     * There is three conversion use-case:
     *  - USD to Any : amount * ratio
     *  - Any to USD : amount * (1/ration)
     *  - Any to Any : (Any to USD) * ratio
     *
     */
    fun convert(amount: BigDecimal) {
        // If currency hasn't been loaded yet, trigger error message
        if (mCurrencyList.value == null || mCurrencyList.value?.currencies == null) {
            mErrorMessage.value = "Select a currency please."
            return
        }

        mSelectedCurrency?.let {
            val list = mCurrencyList.value
            // If selected currency is USD, multiply directly ratio with amount
            if (it.code == "USD") {
                list?.currencies?.forEach { item ->
                    item?.latestCalculation = amount * item?.usdRate!!
                }
            } else {
                // If currency is not USD, we need first to convert into USD and then into desired currency
                // But if current currency is USD, no need to convert twice
                list?.currencies?.forEach { item ->
                    item?.let {
                        // Convert desired currency to USD
                        var result = amount * (BigDecimal(1).divide(
                            mSelectedCurrency!!.usdRate!!,
                            5,
                            RoundingMode.HALF_DOWN
                        ))
                        // Check if current currency is not USD, if not, convert USD to desired currency
                        if (item.code != "USD") {
                            result *= item.usdRate!!
                        }
                        item.latestCalculation = result
                    }
                }
            }
            // Update LiveData to notify UI that something changed
            mCurrencyList.value = list
        }
    }

    /**
     * Function that handle initialisation of data.
     * It will ask repository and even initiate task for retrieving data every 30min.
     *
     */
    fun fetchInformation() {
        val data = currencyConverterRepository.getCurrencyRatesListFromSharedPref(getApplication())
        val timeStamp = currencyConverterRepository.getTimestampFromSharedPref(getApplication())

        // If there is no data in Shared preferences, go fetch them from server
        if (data == null) getAvailableCurrency()

        timeStamp?.let {
            // If 30min (RATES_FETCH_PERIOD) has not passed yet since last timestamp, use data directly
            if (timeStamp > ((System.currentTimeMillis() / 1000) - RATES_FETCH_PERIOD)) {
                mCurrencyList.value = data
                return
            }
        }
        // Otherwise fetch once directly and then automatically every 30min
        mFetcherTask = Timer("rateFetcher", false).schedule(100, RATES_FETCH_PERIOD) {
            updateExchangeRates()
        }
    }

    /**
     * Cancel the running task when application is destroyed
     */
    fun onDestroyed() {
        mFetcherTask?.cancel()
    }

}
