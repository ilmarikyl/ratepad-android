package com.ilmariware.currencyconverterwidget.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ilmariware.currencyconverterwidget.data.CurrencyRepository
import com.ilmariware.currencyconverterwidget.data.WidgetPreferences
import com.ilmariware.currencyconverterwidget.data.models.UpdateFrequency
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val widgetId = inputData.getInt(KEY_WIDGET_ID, -1)
            
            if (widgetId == -1) {
                return Result.failure()
            }
            
            Log.d(TAG, "Updating exchange rate for widget $widgetId")
            
            val preferences = WidgetPreferences(applicationContext)
            val repository = CurrencyRepository(applicationContext)
            
            // Get widget configuration
            val sourceCurrency = preferences.getSourceCurrency(widgetId)
            val targetCurrency = preferences.getTargetCurrency(widgetId)
            
            // Fetch updated exchange rates in BOTH directions
            // This ensures that swapping currencies always has fresh rates
            val result1 = repository.getExchangeRate(
                sourceCurrency,
                targetCurrency,
                forceRefresh = true
            )
            
            val result2 = repository.getExchangeRate(
                targetCurrency,
                sourceCurrency,
                forceRefresh = true
            )
            
            // Consider it successful if at least one direction succeeded
            if (result1.isSuccess || result2.isSuccess) {
                if (result1.isSuccess && result2.isSuccess) {
                    Log.d(TAG, "Successfully updated exchange rates in both directions for widget $widgetId")
                } else if (result1.isSuccess) {
                    Log.w(TAG, "Updated $sourceCurrency -> $targetCurrency, but reverse direction failed")
                } else {
                    Log.w(TAG, "Updated $targetCurrency -> $sourceCurrency, but reverse direction failed")
                }
                
                // Update the widget display
                val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                CurrencyConverterWidget.updateWidget(
                    applicationContext,
                    appWidgetManager,
                    widgetId
                )
                
                Result.success()
            } else {
                Log.e(TAG, "Failed to update exchange rates in both directions for widget $widgetId")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widget", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "WidgetUpdateWorker"
        private const val KEY_WIDGET_ID = "widget_id"
        private const val WORK_NAME_PREFIX = "widget_update_"

        fun scheduleUpdate(
            context: Context,
            widgetId: Int,
            frequency: UpdateFrequency
        ) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                frequency.hours,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_WIDGET_ID to widgetId))
                .addTag(getWorkTag(widgetId))
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                getWorkName(widgetId),
                ExistingPeriodicWorkPolicy.REPLACE,
                updateRequest
            )
            
            Log.d(TAG, "Scheduled update for widget $widgetId every ${frequency.hours} hours")
        }

        fun cancelUpdate(context: Context, widgetId: Int) {
            WorkManager.getInstance(context).cancelUniqueWork(getWorkName(widgetId))
            Log.d(TAG, "Cancelled update for widget $widgetId")
        }

        private fun getWorkName(widgetId: Int): String {
            return "$WORK_NAME_PREFIX$widgetId"
        }

        private fun getWorkTag(widgetId: Int): String {
            return "widget_$widgetId"
        }
    }
}



