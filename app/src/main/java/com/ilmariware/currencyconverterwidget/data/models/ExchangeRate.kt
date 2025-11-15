package com.ilmariware.currencyconverterwidget.data.models

import com.google.gson.annotations.SerializedName

data class ExchangeRate(
    val fromCurrency: Currency,
    val toCurrency: Currency,
    val rate: Double,
    val timestamp: Long
)

data class ExchangeRateResponse(
    @SerializedName("rates")
    val rates: Map<String, Double>,
    @SerializedName("base")
    val base: String,
    @SerializedName("date")
    val date: String
)



