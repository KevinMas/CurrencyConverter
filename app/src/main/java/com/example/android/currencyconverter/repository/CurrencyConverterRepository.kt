package com.example.android.currencyconverter.repository

import android.content.Context
import com.example.android.currencyconverter.model.ApiResponse
import com.example.android.currencyconverter.model.Currencies
import com.example.android.currencyconverter.repository.service.CurrencyConverterService
import com.example.android.currencyconverter.utils.SHARED_PREFERENCES_CURRENCY_KEY
import com.example.android.currencyconverter.utils.SHARED_PREFERENCES_FILE
import com.example.android.currencyconverter.utils.SHARED_PREFERENCES_TIMESTAMP_KEY
import com.google.gson.Gson
import retrofit2.Call

/**
 * Repository that handle Currency converter API
 */
class CurrencyConverterRepository {

    // service
    private var currencyService: CurrencyConverterService = getCurrencyConverterService()

    /**
     * Service for retrieve list of currencies
     */
    fun getCurrencyNameList() : Call<ApiResponse> {
        return currencyService.getCurrencyNameList()
    }

    /**
     * Service for retrieving rates USD to all currencies
     */
    fun getCurrencyRatesList() : Call<ApiResponse> {
        return currencyService.getExchangeRatesList()
    }

    /**
     * get backup of currencies information
     */
    fun getCurrencyRatesListFromSharedPref(context: Context): Currencies? {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val value = sharedPref?.getString(SHARED_PREFERENCES_CURRENCY_KEY, "")
        if( value != null) {
            return Gson().fromJson(value, Currencies::class.java)
        }
        return null
    }

    /**
     * get backup of latest retrieve timestamp
     */
    fun getTimestampFromSharedPref(context: Context): Long? {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        return sharedPref.getLong(SHARED_PREFERENCES_TIMESTAMP_KEY, 0)
    }

    /**
     * Write latest currencies information and timestamp in SharedPreferences
     */
    fun writeCurrencyRatesListToSharedPref(context: Context, currencies: Currencies, timestamp: Long = -1) {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE).edit()
        val value =  Gson().toJson(currencies, Currencies::class.java)
        sharedPref.putString(SHARED_PREFERENCES_CURRENCY_KEY, value)
        if(timestamp > -1) sharedPref.putLong(SHARED_PREFERENCES_TIMESTAMP_KEY, timestamp)
        sharedPref.apply()
    }

}