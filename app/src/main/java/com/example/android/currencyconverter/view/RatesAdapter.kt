package com.example.android.currencyconverter.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.currencyconverter.R
import com.example.android.currencyconverter.model.Currency
import kotlinx.android.synthetic.main.exchange_rate_card_view.view.*
import java.math.RoundingMode

/**
 * Adapter used to manage RecyclerView that will show all currency and rates
 */
class RatesAdapter(private val context: Context, private val items: MutableList<Currency>) :
    RecyclerView.Adapter<RatesViewHolder>() {

    /**
     * Inflate the item view
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatesViewHolder {
        return RatesViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.exchange_rate_card_view, parent, false)
        )
    }

    /**
     * Return number of item to show
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Bind Data in the layout for each item
     */
    override fun onBindViewHolder(holder: RatesViewHolder, position: Int) {
        // Currency code and its name "(code) Name"
        holder.currencyNameTextView.text =
            String.format("(%s) %s", items[position].code, items[position].name)
        // Result of conversion
        holder.exchangeResultTextView.text =
            items[position].latestCalculation?.setScale(2, RoundingMode.HALF_DOWN).toString()
    }

    /**
     * Function that will set a new List of data
     */
    fun setList(list: List<Currency>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

}

/**
 * ViewHolder that match layout section with its adequate object
 */
class RatesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val currencyNameTextView: TextView = view.currency_name
    val exchangeResultTextView: TextView = view.exchange_result
}