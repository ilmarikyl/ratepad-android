package com.ilmariware.currencyconverterwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import com.ilmariware.currencyconverterwidget.R
import com.ilmariware.currencyconverterwidget.data.CurrencyRepository
import com.ilmariware.currencyconverterwidget.data.WidgetPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CurrencyConverterWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val preferences = WidgetPreferences(context)
        for (appWidgetId in appWidgetIds) {
            preferences.deleteWidgetConfig(appWidgetId)
            WidgetUpdateWorker.cancelUpdate(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // First widget added
    }

    override fun onDisabled(context: Context) {
        // Last widget removed
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_BUTTON_CLICK -> {
                val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                val buttonValue = intent.getStringExtra(EXTRA_BUTTON_VALUE)
                
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && buttonValue != null) {
                    handleButtonClick(context, widgetId, buttonValue)
                }
            }
        }
    }

    private fun handleButtonClick(context: Context, widgetId: Int, buttonValue: String) {
        val preferences = WidgetPreferences(context)
        val calculator = WidgetCalculator()
        val currentInput = preferences.getCurrentInput(widgetId)
        
        val newInput = calculator.handleInput(currentInput, buttonValue)
        preferences.setCurrentInput(widgetId, newInput)
        
        // Update the widget display
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateWidget(context, appWidgetManager, widgetId)
    }

    companion object {
        const val ACTION_BUTTON_CLICK = "com.ilmariware.currencyconverterwidget.BUTTON_CLICK"
        const val EXTRA_WIDGET_ID = "widget_id"
        const val EXTRA_BUTTON_VALUE = "button_value"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            Log.d(TAG, "updateWidget called for widget $widgetId")
            try {
                val preferences = WidgetPreferences(context)
                
                // Get widget configuration
                val sourceCurrency = preferences.getSourceCurrency(widgetId)
                val targetCurrency = preferences.getTargetCurrency(widgetId)
                val currentInput = preferences.getCurrentInput(widgetId)
                val theme = preferences.getTheme(widgetId)
                
                Log.d(TAG, "Widget config: $sourceCurrency -> $targetCurrency, input: $currentInput, theme: ${theme.displayName}")
                
                // Use widget layout
                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                Log.d(TAG, "RemoteViews created")
                
                // Apply theme colors
                views.setInt(R.id.widgetRoot, "setBackgroundColor", theme.backgroundColor)
                views.setInt(R.id.displayContainer, "setBackgroundResource", theme.displayBackgroundDrawable)
                views.setTextColor(R.id.sourceCurrencyLabel, theme.textColor)
                views.setTextColor(R.id.inputDisplay, theme.textColor)
                views.setTextColor(R.id.targetCurrencyLabel, theme.targetTextColor)
                views.setTextColor(R.id.outputDisplay, theme.targetTextColor)
                views.setTextColor(R.id.lastUpdatedText, theme.timestampColor)
                
                // Set currency labels
                views.setTextViewText(R.id.sourceCurrencyLabel, sourceCurrency.code)
                views.setTextViewText(R.id.targetCurrencyLabel, targetCurrency.code)
                
                // Set input display
                views.setTextViewText(R.id.inputDisplay, formatNumber(currentInput))
                
                // Calculate and set output using cached rate (synchronous)
                val inputValue = currentInput.toDoubleOrNull() ?: 0.0
                val cachedRate = preferences.getCachedRate(sourceCurrency, targetCurrency)
                
                Log.d(TAG, "Cached rate: $cachedRate, input value: $inputValue")
                
                if (cachedRate != null) {
                    val converted = inputValue * cachedRate
                    val formattedOutput = String.format(Locale.US, "%.2f", converted)
                    views.setTextViewText(R.id.outputDisplay, formattedOutput)
                    Log.d(TAG, "Output set to: $formattedOutput")
                    
                    // Update timestamp (if it exists in the layout)
                    try {
                        val timestamp = preferences.getCachedRateTimestamp(sourceCurrency, targetCurrency)
                        val timeText = formatTimestamp(timestamp)
                        views.setTextViewText(R.id.lastUpdatedText, "Rate updated: $timeText")
                    } catch (e: Exception) {
                        // Layout doesn't have timestamp
                        Log.d(TAG, "No timestamp display in this layout")
                    }
                } else {
                    Log.w(TAG, "No cached rate found!")
                    views.setTextViewText(R.id.outputDisplay, "0.00")
                    
                    // Fetch rate in background
                    CoroutineScope(Dispatchers.IO).launch {
                        val repository = CurrencyRepository(context)
                        repository.getExchangeRate(sourceCurrency, targetCurrency, forceRefresh = false)
                        // After fetching, update widget again
                        updateWidget(context, appWidgetManager, widgetId)
                    }
                }
                
                // Apply button colors
                applyButtonColors(views, theme)
                
                // Set up button click listeners
                setupButtonListeners(context, views, widgetId)
                Log.d(TAG, "Button listeners set up")
                
                // Update the widget
                appWidgetManager.updateAppWidget(widgetId, views)
                Log.d(TAG, "Widget updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget", e)
                e.printStackTrace()
            }
        }

        private fun applyButtonColors(views: RemoteViews, theme: com.ilmariware.currencyconverterwidget.data.models.WidgetTheme) {
            val buttonIds = listOf(
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btn000, R.id.btnBackspace, R.id.btnClear
            )
            
            buttonIds.forEach { buttonId ->
                try {
                    views.setInt(buttonId, "setBackgroundResource", theme.buttonBackgroundDrawable)
                    views.setTextColor(buttonId, theme.buttonTextColor)
                } catch (e: Exception) {
                    Log.d(TAG, "Could not set color for button $buttonId")
                }
            }
        }

        private fun setupButtonListeners(context: Context, views: RemoteViews, widgetId: Int) {
            val buttonIds = listOf(
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btnBackspace, R.id.btn000
            )
            
            val buttonValues = listOf(
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "⌫", "000"
            )
            
            buttonIds.forEachIndexed { index, buttonId ->
                try {
                    val intent = Intent(context, CurrencyConverterWidget::class.java).apply {
                        action = ACTION_BUTTON_CLICK
                        putExtra(EXTRA_WIDGET_ID, widgetId)
                        putExtra(EXTRA_BUTTON_VALUE, buttonValues[index])
                    }
                    
                    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }
                    
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        widgetId * 100 + index,
                        intent,
                        flags
                    )
                    
                    views.setOnClickPendingIntent(buttonId, pendingIntent)
                } catch (e: Exception) {
                    // Button doesn't exist in this layout
                }
            }
            
            // Clear button
            try {
                val clearIntent = Intent(context, CurrencyConverterWidget::class.java).apply {
                    action = ACTION_BUTTON_CLICK
                    putExtra(EXTRA_WIDGET_ID, widgetId)
                    putExtra(EXTRA_BUTTON_VALUE, "C")
                }
                
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                
                val clearPendingIntent = PendingIntent.getBroadcast(
                    context,
                    widgetId * 100 + 99,
                    clearIntent,
                    flags
                )
                
                views.setOnClickPendingIntent(R.id.btnClear, clearPendingIntent)
            } catch (e: Exception) {
                // Layout doesn't have clear button
            }
        }

        private fun formatNumber(input: String): String {
            return if (input.isEmpty() || input == "0") "0" else input
        }

        private fun formatTimestamp(timestamp: Long): String {
            if (timestamp == 0L) return "Never"
            
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60_000 -> "Just now"
                diff < 3600_000 -> "${diff / 60_000}m ago"
                diff < 86400_000 -> "${diff / 3600_000}h ago"
                diff < 604800_000 -> "${diff / 86400_000}d ago"
                else -> {
                    val sdf = SimpleDateFormat("MMM d", Locale.US)
                    sdf.format(Date(timestamp))
                }
            }
        }

        private const val TAG = "CurrencyConverterWidget"
    }
}

