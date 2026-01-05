package com.ilmariware.currencyconverterwidget.data

import android.content.Context
import android.util.Log
import com.ilmariware.currencyconverterwidget.data.models.Currency
import com.ilmariware.currencyconverterwidget.data.models.ExchangeRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CurrencyRepository(context: Context) {
    private val preferences = WidgetPreferences(context)
    private val api: ExchangeRateApi
    private val fallbackApi: FallbackExchangeRateApi

    init {
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        // Configure Gson explicitly to handle reflection properly
        val gson = com.google.gson.GsonBuilder()
            .setLenient()
            .create()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.frankfurter.app/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        api = retrofit.create(ExchangeRateApi::class.java)
        fallbackApi = FallbackExchangeRateApi()
    }

    /**
     * Gets the exchange rate, using cache if available and not stale
     */
    suspend fun getExchangeRate(
        fromCurrency: Currency,
        toCurrency: Currency,
        forceRefresh: Boolean = false
    ): Result<ExchangeRate> = withContext(Dispatchers.IO) {
        try {
            // If currencies are the same, return 1.0
            if (fromCurrency == toCurrency) {
                return@withContext Result.success(
                    ExchangeRate(
                        fromCurrency = fromCurrency,
                        toCurrency = toCurrency,
                        rate = 1.0,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            // Check cache first if not forcing refresh
            if (!forceRefresh) {
                val cachedRate = preferences.getCachedRate(fromCurrency, toCurrency)
                val cachedTimestamp = preferences.getCachedRateTimestamp(fromCurrency, toCurrency)
                
                if (cachedRate != null && cachedTimestamp > 0) {
                    Log.d(TAG, "Using cached rate: $fromCurrency -> $toCurrency = $cachedRate")
                    return@withContext Result.success(
                        ExchangeRate(
                            fromCurrency = fromCurrency,
                            toCurrency = toCurrency,
                            rate = cachedRate,
                            timestamp = cachedTimestamp
                        )
                    )
                }
            }

            // Fetch fresh data from primary API (Frankfurter)
            Log.d(TAG, "Fetching rate from primary API: $fromCurrency -> $toCurrency")
            var rate: Double? = null
            
            try {
                val response = api.getLatestRates(base = fromCurrency.code)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        rate = body.rates[toCurrency.code]
                        if (rate != null) {
                            Log.d(TAG, "Successfully fetched rate from primary API: $rate")
                        }
                    }
                }
                
                if (rate == null) {
                    Log.w(TAG, "Primary API failed or currency not supported, trying fallback API")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Primary API error, trying fallback API: ${e.message}")
            }
            
            // Try fallback API if primary failed
            if (rate == null) {
                rate = fallbackApi.getExchangeRate(fromCurrency.code, toCurrency.code)
                if (rate != null) {
                    Log.d(TAG, "Successfully fetched rate from fallback API: $rate")
                }
            }
            
            if (rate != null) {
                val timestamp = System.currentTimeMillis()
                
                // Cache the rate
                preferences.setCachedRate(fromCurrency, toCurrency, rate)
                preferences.setCachedRateTimestamp(fromCurrency, toCurrency, timestamp)
                
                return@withContext Result.success(
                    ExchangeRate(
                        fromCurrency = fromCurrency,
                        toCurrency = toCurrency,
                        rate = rate,
                        timestamp = timestamp
                    )
                )
            }
            
            Log.e(TAG, "Failed to fetch rate from both APIs")
            Result.failure(Exception("Failed to fetch exchange rate from all sources"))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching exchange rate: ${e.javaClass.name}: ${e.message}", e)
            e.printStackTrace()
            
            // Try to return cached rate as fallback
            val cachedRate = preferences.getCachedRate(fromCurrency, toCurrency)
            val cachedTimestamp = preferences.getCachedRateTimestamp(fromCurrency, toCurrency)
            
            if (cachedRate != null && cachedTimestamp > 0) {
                Log.d(TAG, "Using cached rate as fallback")
                return@withContext Result.success(
                    ExchangeRate(
                        fromCurrency = fromCurrency,
                        toCurrency = toCurrency,
                        rate = cachedRate,
                        timestamp = cachedTimestamp
                    )
                )
            }
            
            Result.failure(e)
        }
    }

    /**
     * Converts an amount from one currency to another
     */
    suspend fun convertCurrency(
        amount: Double,
        fromCurrency: Currency,
        toCurrency: Currency
    ): Result<Double> {
        return try {
            val rateResult = getExchangeRate(fromCurrency, toCurrency)
            if (rateResult.isSuccess) {
                val rate = rateResult.getOrNull()!!.rate
                Result.success(amount * rate)
            } else {
                Result.failure(rateResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "CurrencyRepository"
    }
}

