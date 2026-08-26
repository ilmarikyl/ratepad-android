package com.ilmariware.currencyconverterwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import com.ilmariware.currencyconverterwidget.R
import com.ilmariware.currencyconverterwidget.WidgetConfigurationActivity
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

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
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
            ACTION_SWAP_CURRENCIES -> {
                val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    handleSwapCurrencies(context, widgetId)
                }
            }
            ACTION_COPY_RESULT -> {
                val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    handleCopyResult(context, widgetId)
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

    private fun handleSwapCurrencies(context: Context, widgetId: Int) {
        val preferences = WidgetPreferences(context)
        
        // Get current currencies
        val sourceCurrency = preferences.getSourceCurrency(widgetId)
        val targetCurrency = preferences.getTargetCurrency(widgetId)
        
        // Swap them
        preferences.setSourceCurrency(widgetId, targetCurrency)
        preferences.setTargetCurrency(widgetId, sourceCurrency)
        
        // Update the widget display
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateWidget(context, appWidgetManager, widgetId)
    }

    private fun handleCopyResult(context: Context, widgetId: Int) {
        val preferences = WidgetPreferences(context)
        val sourceCurrency = preferences.getSourceCurrency(widgetId)
        val targetCurrency = preferences.getTargetCurrency(widgetId)
        val inputValue = preferences.getCurrentInput(widgetId).toDoubleOrNull() ?: 0.0
        val cachedRate = preferences.getCachedRate(sourceCurrency, targetCurrency)
        val resultText = if (cachedRate != null) {
            String.format(Locale.US, "%.2f", inputValue * cachedRate)
        } else {
            "0.00"
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(context.getString(R.string.copied_result_label), resultText)
        )

        // Android 13+ already shows a clipboard confirmation overlay.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val ACTION_BUTTON_CLICK = "com.ilmariware.currencyconverterwidget.BUTTON_CLICK"
        const val ACTION_SWAP_CURRENCIES = "com.ilmariware.currencyconverterwidget.SWAP_CURRENCIES"
        const val ACTION_COPY_RESULT = "com.ilmariware.currencyconverterwidget.COPY_RESULT"
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
                
                // Apply theme colors with opacity
                val opacity = preferences.getOpacity(widgetId)
                val alpha = (opacity / 100f * 255).toInt()
                val colorWithAlpha = ColorUtils.setAlphaComponent(theme.backgroundColor, alpha)
                views.setInt(R.id.widgetRoot, "setBackgroundColor", colorWithAlpha)
                views.setInt(R.id.displayContainer, "setBackgroundResource", theme.displayBackgroundDrawable)
                views.setTextColor(R.id.sourceCurrencyLabel, theme.textColor)
                views.setTextColor(R.id.inputDisplay, theme.textColor)
                views.setTextColor(R.id.targetCurrencyLabel, theme.targetTextColor)
                views.setTextColor(R.id.outputDisplay, theme.targetTextColor)
                val timestampTint = if (opacity < 100) theme.buttonTextColor else theme.timestampColor
                views.setTextColor(R.id.lastUpdatedLabel, timestampTint)
                views.setTextColor(R.id.lastUpdatedText, timestampTint)

                // Tint swap and edit buttons to match theme
                try {
                    views.setInt(R.id.btnSwap, "setColorFilter", theme.textColor)
                } catch (e: Exception) {
                    Log.d(TAG, "Could not set swap button color")
                }
                try {
                    views.setInt(R.id.btnEdit, "setColorFilter", timestampTint)
                } catch (e: Exception) {
                    Log.d(TAG, "Could not set edit button color")
                }
                
                // Set currency labels
                views.setTextViewText(R.id.sourceCurrencyLabel, sourceCurrency.code)
                views.setTextViewText(R.id.targetCurrencyLabel, targetCurrency.code)

                val inputText = formatNumber(currentInput)
                views.setTextViewText(R.id.inputDisplay, inputText)

                val inputValue = currentInput.toDoubleOrNull() ?: 0.0
                val cachedRate = preferences.getCachedRate(sourceCurrency, targetCurrency)

                Log.d(TAG, "Cached rate: $cachedRate, input value: $inputValue")

                val outputText = if (cachedRate != null) {
                    val converted = inputValue * cachedRate
                    val formattedOutput = String.format(Locale.US, "%.2f", converted)
                    Log.d(TAG, "Output set to: $formattedOutput")

                    try {
                        val timestamp = preferences.getCachedRateTimestamp(sourceCurrency, targetCurrency)
                        views.setTextViewText(R.id.lastUpdatedText, formatTimestamp(timestamp))
                    } catch (e: Exception) {
                        Log.d(TAG, "No timestamp display in this layout")
                    }
                    formattedOutput
                } else {
                    Log.w(TAG, "No cached rate found!")
                    CoroutineScope(Dispatchers.IO).launch {
                        val repository = CurrencyRepository(context)
                        repository.getExchangeRate(sourceCurrency, targetCurrency, forceRefresh = false)
                        updateWidget(context, appWidgetManager, widgetId)
                    }
                    "0.00"
                }
                views.setTextViewText(R.id.outputDisplay, outputText)

                val widgetWidthDp = currentWidgetWidthDp(appWidgetManager, widgetId)
                val fontScale = context.resources.configuration.fontScale
                val timestampSizeSp = timestampTextSizeSp(widgetWidthDp)
                views.setTextViewTextSize(R.id.lastUpdatedLabel, TypedValue.COMPLEX_UNIT_SP, timestampSizeSp)
                views.setTextViewTextSize(R.id.lastUpdatedText, TypedValue.COMPLEX_UNIT_SP, timestampSizeSp)
                views.setTextViewTextSize(
                    R.id.inputDisplay,
                    TypedValue.COMPLEX_UNIT_SP,
                    amountTextSizeSp(inputText, widgetWidthDp, fontScale)
                )
                views.setTextViewTextSize(
                    R.id.outputDisplay,
                    TypedValue.COMPLEX_UNIT_SP,
                    amountTextSizeSp(outputText, widgetWidthDp, fontScale)
                )
                
                // Apply button colors
                applyButtonColors(views, theme, widgetWidthDp)
                
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

        private fun applyButtonColors(
            views: RemoteViews,
            theme: com.ilmariware.currencyconverterwidget.data.models.WidgetTheme,
            widgetWidthDp: Int
        ) {
            val buttonIds = listOf(
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btn000, R.id.btnBackspace, R.id.btnClear
            )
            
            buttonIds.forEach { buttonId ->
                try {
                    views.setInt(buttonId, "setBackgroundResource", theme.buttonBackgroundDrawable)
                    views.setTextColor(buttonId, theme.buttonTextColor)
                    views.setTextViewTextSize(
                        buttonId,
                        TypedValue.COMPLEX_UNIT_SP,
                        buttonLabelSizeSp(widgetWidthDp, buttonId)
                    )
                } catch (e: Exception) {
                    Log.d(TAG, "Could not set color for button $buttonId")
                }
            }
        }

        /** 18sp at 3-cell width, shrinking toward 14sp when the widget is 120dp wide. */
        private fun buttonLabelSizeSp(widgetWidthDp: Int, buttonId: Int): Float {
            val baseSp = if (buttonId == R.id.btn000) BUTTON_000_TEXT_MAX_SP else BUTTON_TEXT_MAX_SP
            val minSp = if (buttonId == R.id.btn000) BUTTON_000_TEXT_MIN_SP else BUTTON_TEXT_MIN_SP
            val range = (COMFORTABLE_WIDGET_WIDTH_DP - COMPACT_WIDGET_WIDTH_DP).toFloat()
            val t = ((widgetWidthDp - COMPACT_WIDGET_WIDTH_DP) / range).coerceIn(0f, 1f)
            return minSp + (baseSp - minSp) * t
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
            
            // Swap currencies button
            try {
                val swapIntent = Intent(context, CurrencyConverterWidget::class.java).apply {
                    action = ACTION_SWAP_CURRENCIES
                    putExtra(EXTRA_WIDGET_ID, widgetId)
                }
                
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                
                val swapPendingIntent = PendingIntent.getBroadcast(
                    context,
                    widgetId * 100 + 98,
                    swapIntent,
                    flags
                )
                
                views.setOnClickPendingIntent(R.id.btnSwap, swapPendingIntent)
            } catch (e: Exception) {
                // Layout doesn't have swap button
            }

            // Copy converted result
            try {
                val copyIntent = Intent(context, CurrencyConverterWidget::class.java).apply {
                    action = ACTION_COPY_RESULT
                    putExtra(EXTRA_WIDGET_ID, widgetId)
                }

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                val copyPendingIntent = PendingIntent.getBroadcast(
                    context,
                    widgetId * 100 + 96,
                    copyIntent,
                    flags
                )

                views.setOnClickPendingIntent(R.id.outputDisplay, copyPendingIntent)
                views.setOnClickPendingIntent(R.id.targetCurrencyLabel, copyPendingIntent)
            } catch (e: Exception) {
                // Layout doesn't have result display
            }

            // Edit / reconfigure button
            try {
                val editIntent = Intent(context, WidgetConfigurationActivity::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                val editPendingIntent = PendingIntent.getActivity(
                    context,
                    widgetId * 100 + 97,
                    editIntent,
                    flags
                )

                views.setOnClickPendingIntent(R.id.btnEdit, editPendingIntent)
            } catch (e: Exception) {
                // Layout doesn't have edit button
            }
        }

        /** 9sp at 3-cell width, a bit smaller at 120dp so the full timestamp fits beside the edit icon. */
        private fun timestampTextSizeSp(widgetWidthDp: Int): Float {
            val range = (COMFORTABLE_WIDGET_WIDTH_DP - COMPACT_WIDGET_WIDTH_DP).toFloat()
            val t = ((widgetWidthDp - COMPACT_WIDGET_WIDTH_DP) / range).coerceIn(0f, 1f)
            return TIMESTAMP_TEXT_MIN_SP + (TIMESTAMP_TEXT_MAX_SP - TIMESTAMP_TEXT_MIN_SP) * t
        }

        private fun currentWidgetWidthDp(appWidgetManager: AppWidgetManager, widgetId: Int): Int {
            val options = appWidgetManager.getAppWidgetOptions(widgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
            return if (minWidth > 0) minWidth else DEFAULT_WIDGET_WIDTH_DP
        }

        /**
         * Shrinks amount text so it stays on one line in the current widget width.
         * Each amount uses roughly half the display, after padding and the swap icon.
         */
        private fun amountTextSizeSp(text: String, widgetWidthDp: Int, fontScale: Float): Float {
            if (text.isEmpty()) return AMOUNT_TEXT_MAX_SP
            val columnWidthDp = ((widgetWidthDp - DISPLAY_CHROME_DP) / 2f).coerceAtLeast(40f)
            val charWidthAt1sp = DIGIT_WIDTH_EM * fontScale.coerceAtLeast(0.85f)
            val fitted = columnWidthDp / (text.length * charWidthAt1sp)
            return fitted.coerceIn(AMOUNT_TEXT_MIN_SP, AMOUNT_TEXT_MAX_SP)
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
        private const val DEFAULT_WIDGET_WIDTH_DP = 180
        private const val COMPACT_WIDGET_WIDTH_DP = 120
        private const val COMFORTABLE_WIDGET_WIDTH_DP = 180
        /** Root padding + display padding + swap button and margins. */
        private const val DISPLAY_CHROME_DP = 54
        /** Approximate width of a bold digit, as a fraction of the text size in sp. */
        private const val DIGIT_WIDTH_EM = 0.65f
        private const val AMOUNT_TEXT_MAX_SP = 18f
        private const val AMOUNT_TEXT_MIN_SP = 9f
        private const val BUTTON_TEXT_MAX_SP = 18f
        private const val BUTTON_TEXT_MIN_SP = 14f
        private const val BUTTON_000_TEXT_MAX_SP = 16f
        private const val BUTTON_000_TEXT_MIN_SP = 12f
        private const val TIMESTAMP_TEXT_MAX_SP = 9f
        private const val TIMESTAMP_TEXT_MIN_SP = 8f
    }
}

