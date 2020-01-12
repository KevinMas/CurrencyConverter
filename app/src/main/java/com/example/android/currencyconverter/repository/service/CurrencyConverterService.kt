package com.example.android.currencyconverter.repository.service

import com.example.android.currencyconverter.model.ApiResponse
import com.example.android.currencyconverter.utils.CURRENCY_CONVERTER_API_KEY
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Service to access Currency converter API
 */
interface CurrencyConverterService {

    @GET("list?")
    fun getCurrencyNameList(
        @Query("access_key") apiKey: String = CURRENCY_CONVERTER_API_KEY
    ): Call<ApiResponse>

    @GET("live?")
    fun getExchangeRatesList(
        @Query("access_key") apiKey: String = CURRENCY_CONVERTER_API_KEY
    ): Call<ApiResponse>

}