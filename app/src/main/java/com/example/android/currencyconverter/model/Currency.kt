package com.example.android.currencyconverter.model

import java.math.BigDecimal

data class Currency(
    var code: String,
    var name: String,
    var usdRate: BigDecimal? = null,
    var latestCalculation: BigDecimal? = null
)