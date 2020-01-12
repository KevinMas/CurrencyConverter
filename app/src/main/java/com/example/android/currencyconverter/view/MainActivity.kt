package com.example.android.currencyconverter.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import com.example.android.currencyconverter.viewmodel.CurrencyViewModel
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.currencyconverter.R
import com.example.android.currencyconverter.model.Currency
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal

/**
 * Main Activity of Currency Converter
 */
class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    // Adapter for RecyclerView
    private lateinit var mAdapter: RatesAdapter
    private lateinit var mAmountEditText: EditText
    private lateinit var mConvertButton: Button
    private lateinit var mCurrencySpinner: Spinner
    private lateinit var currencyViewModel: CurrencyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currencyViewModel = ViewModelProviders.of(this).get(CurrencyViewModel::class.java)

        mAmountEditText = findViewById(R.id.amount_edit_text)
        mConvertButton = findViewById(R.id.convert_button)
        mCurrencySpinner = findViewById(R.id.currency_spinner)
        mCurrencySpinner.onItemSelectedListener = this

        // Prepare the RecyclerView with empty content
        rates_recycler_view.layoutManager =  LinearLayoutManager(this)
        // rates_recycler_view.layoutManager =  GridLayoutManager(this, 2)
        mAdapter = RatesAdapter(this, mutableListOf() )
        rates_recycler_view.adapter = mAdapter

        // When clicking convert button, check if amount and currency are present.
        mConvertButton.setOnClickListener {
            val amount: BigDecimal? = mAmountEditText.text.toString().toBigDecimalOrNull()
            if(amount != null && mCurrencySpinner.selectedItemPosition != 0) {
                // If amount and currency are filed, call convert function from viewModel
                currencyViewModel.convert(amount)
            } else {
                Toast.makeText(this, "Select a currency or an number.", Toast.LENGTH_LONG).show()
            }
        }
        // When Currencies liveData has been update, there is two cases,
        // if spinner is still empty it means that data has been updated for it.
        // Otherwise, it means that something has been converted and we need to update the RecyclerView
        currencyViewModel.mCurrencyList.observe(this, Observer { list ->
            if (mCurrencySpinner.adapter == null) {
                initSpinner(list.currencies)
            } else {
                mAdapter.setList(list.currencies.filterNotNull())
            }
        })

        // When any error has been triggered, show them in a Toast
        currencyViewModel.mErrorMessage.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })
        // WHen everything is prepared, ask the viewModel to fetch data
        currencyViewModel.fetchInformation()
    }

    /**
     * When Spinner has nothing selected (never happen, but it is necessary)
     */
    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d("MainActivity","onNothingSelected invoked")
    }

    /**
     * When a item is selected, notify viewModel
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        currencyViewModel.updateCurrentSelectedCurrency(id.toInt())
    }

    /**
     * Initialisation for the Spinner. Customize text using currency code and its name.
     */
    private fun initSpinner(items: List<Currency?>) {
        val spinnerArrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            items.map { if (it != null) "(" + it.code + ") " + it.name else "Select a currency" }.toMutableList()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        mCurrencySpinner.adapter = spinnerArrayAdapter
    }

    /**
     * When apply is destroyed, cancel our task that will retrieve rates every 30min
     */
    override fun onDestroy() {
        currencyViewModel.onDestroyed()
        super.onDestroy()
    }
}
