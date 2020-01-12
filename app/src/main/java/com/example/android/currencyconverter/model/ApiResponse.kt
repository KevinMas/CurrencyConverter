package com.example.android.currencyconverter.model

import java.math.BigDecimal

/**
 * API response model class
 */
data class ApiResponse(var success : String,
                       var timestamp : Long,
                       var currencies : Map<String, String>,
                       var quotes : Map<String, BigDecimal>,
                       var error : ErrorResponse)

/**
 * Error model class for API response
 */
data class ErrorResponse(var code: Int,
                         var info : String)