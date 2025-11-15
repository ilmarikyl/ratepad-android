package com.ilmariware.currencyconverterwidget.data.models

data class ExchangeRate(
    val fromCurrency: Currency,
    val toCurrency: Currency,
    val rate: Double,
    val timestamp: Long
)

data class ExchangeRateResponse(
    val rates: Map<String, Double>,
    val base: String,
    val date: String
)



