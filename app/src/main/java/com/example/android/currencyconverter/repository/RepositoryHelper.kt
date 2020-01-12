package com.example.android.currencyconverter.repository

import com.example.android.currencyconverter.repository.service.CurrencyConverterService
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Helper function to get CurrencyConverter service
 */
fun getCurrencyConverterService(): CurrencyConverterService {
    return Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .baseUrl("http://apilayer.net/api/") // Put your base URL
        .client(httpBuilder.build())
        .build()
        .create(CurrencyConverterService::class.java)
}

/**
 * Helper that create an OkHttp object with log for debug
 */
private val httpBuilder: OkHttpClient.Builder
    get() {
        // log interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        // create http client
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .readTimeout(30, TimeUnit.SECONDS)
    }