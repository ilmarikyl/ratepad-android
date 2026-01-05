package com.ilmariware.currencyconverterwidget.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Fallback API using @fawazahmed0/currency-api hosted on jsDelivr CDN.
 * This API supports many more currencies than Frankfurter, including TWD, BGN, etc.
 * No API key required.
 * 
 * API format: https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/{base}.json
 */
class FallbackExchangeRateApi {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    /**
     * Fetches the exchange rate from the fallback API.
     * @param fromCurrency The source currency code (e.g., "USD")
     * @param toCurrency The target currency code (e.g., "TWD")
     * @return The exchange rate, or null if not found
     */
    fun getExchangeRate(fromCurrency: String, toCurrency: String): Double? {
        val baseLower = fromCurrency.lowercase()
        val targetLower = toCurrency.lowercase()
        
        val url = "$BASE_URL$baseLower.json"
        Log.d(TAG, "Fetching from fallback API: $url")
        
        val request = Request.Builder()
            .url(url)
            .build()
        
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Fallback API request failed: ${response.code()}")
                    return null
                }
                
                val body = response.body()?.string() ?: return null
                val json = gson.fromJson(body, JsonObject::class.java)
                
                // The rates are nested under the base currency code
                val ratesObject = json.getAsJsonObject(baseLower)
                if (ratesObject == null) {
                    Log.e(TAG, "No rates found for base currency: $baseLower")
                    return null
                }
                
                val rate = ratesObject.get(targetLower)?.asDouble
                if (rate != null) {
                    Log.d(TAG, "Fallback API rate: $fromCurrency -> $toCurrency = $rate")
                } else {
                    Log.e(TAG, "No rate found for target currency: $targetLower")
                }
                rate
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from fallback API", e)
            null
        }
    }
    
    companion object {
        private const val TAG = "FallbackExchangeRateApi"
        private const val BASE_URL = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/"
    }
}
