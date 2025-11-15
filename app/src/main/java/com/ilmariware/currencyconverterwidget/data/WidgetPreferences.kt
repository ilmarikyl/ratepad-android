package com.ilmariware.currencyconverterwidget.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.ilmariware.currencyconverterwidget.data.models.Currency
import com.ilmariware.currencyconverterwidget.data.models.UpdateFrequency

class WidgetPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    // Widget configuration
    fun getSourceCurrency(widgetId: Int): Currency {
        val code = prefs.getString(getKey(widgetId, KEY_SOURCE_CURRENCY), Currency.USD.code)
        return Currency.fromCode(code!!) ?: Currency.USD
    }

    fun setSourceCurrency(widgetId: Int, currency: Currency) {
        prefs.edit().putString(getKey(widgetId, KEY_SOURCE_CURRENCY), currency.code).apply()
    }

    fun getTargetCurrency(widgetId: Int): Currency {
        val code = prefs.getString(getKey(widgetId, KEY_TARGET_CURRENCY), Currency.EUR.code)
        return Currency.fromCode(code!!) ?: Currency.EUR
    }

    fun setTargetCurrency(widgetId: Int, currency: Currency) {
        prefs.edit().putString(getKey(widgetId, KEY_TARGET_CURRENCY), currency.code).apply()
    }

    fun getUpdateFrequency(widgetId: Int): UpdateFrequency {
        val hours = prefs.getLong(getKey(widgetId, KEY_UPDATE_FREQUENCY), UpdateFrequency.DAILY.hours)
        return UpdateFrequency.fromHours(hours)
    }

    fun setUpdateFrequency(widgetId: Int, frequency: UpdateFrequency) {
        prefs.edit().putLong(getKey(widgetId, KEY_UPDATE_FREQUENCY), frequency.hours).apply()
    }

    fun getTheme(widgetId: Int): com.ilmariware.currencyconverterwidget.data.models.WidgetTheme {
        val themeOrdinal = prefs.getInt(getKey(widgetId, KEY_THEME), 0)
        return com.ilmariware.currencyconverterwidget.data.models.WidgetTheme.fromOrdinal(themeOrdinal)
    }

    fun setTheme(widgetId: Int, theme: com.ilmariware.currencyconverterwidget.data.models.WidgetTheme) {
        prefs.edit().putInt(getKey(widgetId, KEY_THEME), theme.ordinal).apply()
    }

    // Widget state
    fun getCurrentInput(widgetId: Int): String {
        return prefs.getString(getKey(widgetId, KEY_CURRENT_INPUT), "0") ?: "0"
    }

    fun setCurrentInput(widgetId: Int, input: String) {
        prefs.edit().putString(getKey(widgetId, KEY_CURRENT_INPUT), input).apply()
    }

    // Exchange rate cache
    fun getCachedRate(fromCurrency: Currency, toCurrency: Currency): Double? {
        val key = getRateKey(fromCurrency, toCurrency)
        return if (prefs.contains(key)) {
            val rate = prefs.getFloat(key, 0f).toDouble()
            if (rate > 0) rate else null
        } else null
    }

    fun setCachedRate(fromCurrency: Currency, toCurrency: Currency, rate: Double) {
        val key = getRateKey(fromCurrency, toCurrency)
        prefs.edit().putFloat(key, rate.toFloat()).apply()
    }

    fun getCachedRateTimestamp(fromCurrency: Currency, toCurrency: Currency): Long {
        val key = getRateTimestampKey(fromCurrency, toCurrency)
        return prefs.getLong(key, 0L)
    }

    fun setCachedRateTimestamp(fromCurrency: Currency, toCurrency: Currency, timestamp: Long) {
        val key = getRateTimestampKey(fromCurrency, toCurrency)
        prefs.edit().putLong(key, timestamp).apply()
    }

    fun deleteWidgetConfig(widgetId: Int) {
        prefs.edit()
            .remove(getKey(widgetId, KEY_SOURCE_CURRENCY))
            .remove(getKey(widgetId, KEY_TARGET_CURRENCY))
            .remove(getKey(widgetId, KEY_UPDATE_FREQUENCY))
            .remove(getKey(widgetId, KEY_CURRENT_INPUT))
            .apply()
    }

    private fun getKey(widgetId: Int, key: String): String {
        return "widget_${widgetId}_$key"
    }

    private fun getRateKey(from: Currency, to: Currency): String {
        return "rate_${from.code}_${to.code}"
    }

    private fun getRateTimestampKey(from: Currency, to: Currency): String {
        return "rate_timestamp_${from.code}_${to.code}"
    }

    companion object {
        private const val PREFS_NAME = "widget_prefs"
        private const val KEY_SOURCE_CURRENCY = "source_currency"
        private const val KEY_TARGET_CURRENCY = "target_currency"
        private const val KEY_UPDATE_FREQUENCY = "update_frequency"
        private const val KEY_CURRENT_INPUT = "current_input"
        private const val KEY_THEME = "theme"
    }
}



