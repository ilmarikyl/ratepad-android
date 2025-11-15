package com.ilmariware.currencyconverterwidget.data

import com.ilmariware.currencyconverterwidget.data.models.ExchangeRateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateApi {
    /**
     * Fetches latest exchange rates from European Central Bank
     * @param base The base currency code (e.g., "EUR")
     */
    @GET("latest")
    suspend fun getLatestRates(
        @Query("base") base: String = "EUR"
    ): Response<ExchangeRateResponse>
}



