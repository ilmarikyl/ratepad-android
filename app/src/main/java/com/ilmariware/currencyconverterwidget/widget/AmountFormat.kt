package com.ilmariware.currencyconverterwidget.widget

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

/**
 * Display formatting for calculator amounts.
 *
 * Typed input is stored with '.' as the decimal mark and no grouping.
 * Display inserts locale grouping, and falls back to commas when the locale
 * does not provide a distinct thousands separator.
 */
object AmountFormat {

    fun formatInput(raw: String, locale: Locale = Locale.getDefault()): String {
        val normalized = raw.ifBlank { "0" }
        val separators = separatorsFor(locale)
        val negative = normalized.startsWith("-")
        val unsigned = if (negative) normalized.substring(1) else normalized
        val decimalIndex = unsigned.indexOf('.')
        val integerDigits = if (decimalIndex >= 0) unsigned.substring(0, decimalIndex) else unsigned
        val fraction = if (decimalIndex >= 0) unsigned.substring(decimalIndex + 1) else null
        val grouped = groupInteger(integerDigits.ifEmpty { "0" }, locale, separators.first)
        val showMinus = negative && (grouped != "0" || fraction != null)
        val body = if (showMinus) "-$grouped" else grouped
        return if (fraction == null) body else body + separators.second + fraction
    }

    fun formatResult(value: Double, showDecimals: Boolean, locale: Locale = Locale.getDefault()): String {
        val raw = rawResult(value)
        if (showDecimals) return formatInput(raw, locale)
        val whole = BigDecimal(raw).setScale(0, RoundingMode.HALF_UP).toPlainString()
        return formatInput(whole, locale)
    }

    /** Plain amount for the clipboard: no grouping, '.' decimal mark, two decimal places. */
    fun rawResult(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    fun decimalSeparator(locale: Locale = Locale.getDefault()): Char = separatorsFor(locale).second

    internal fun resolveSeparators(symbols: DecimalFormatSymbols?): Pair<Char, Char> {
        if (symbols == null) return ',' to '.'
        val grouping = symbols.groupingSeparator
        val decimal = symbols.decimalSeparator
        if (grouping.code == 0 || grouping == decimal) return ',' to '.'
        val safeDecimal = if (decimal.code == 0) '.' else decimal
        return grouping to safeDecimal
    }

    internal fun groupEveryThree(digits: String, separator: Char): String {
        val builder = StringBuilder()
        digits.reversed().forEachIndexed { index, char ->
            if (index > 0 && index % 3 == 0) builder.append(separator)
            builder.append(char)
        }
        return builder.reverse().toString()
    }

    private fun separatorsFor(locale: Locale): Pair<Char, Char> {
        val symbols = try {
            (NumberFormat.getNumberInstance(locale) as DecimalFormat).decimalFormatSymbols
        } catch (e: Exception) {
            null
        }
        return resolveSeparators(symbols)
    }

    private fun groupInteger(integerDigits: String, locale: Locale, fallbackSeparator: Char): String {
        val digits = integerDigits.filter { it.isDigit() }.trimStart('0').ifEmpty { "0" }
        if (digits.length <= 3) return digits
        return localeGrouped(digits, locale) ?: groupEveryThree(digits, fallbackSeparator)
    }

    private fun localeGrouped(digits: String, locale: Locale): String? {
        return try {
            val format = NumberFormat.getIntegerInstance(locale) as DecimalFormat
            format.isGroupingUsed = true
            format.maximumFractionDigits = 0
            format.minimumFractionDigits = 0
            format.roundingMode = RoundingMode.UNNECESSARY
            val symbols = format.decimalFormatSymbols
            symbols.zeroDigit = '0'
            format.decimalFormatSymbols = symbols
            val formatted = format.format(BigDecimal(digits))
            val asciiDigits = formatted.filter { it in '0'..'9' }
            if (asciiDigits == digits && formatted.any { it !in '0'..'9' }) formatted else null
        } catch (e: Exception) {
            null
        }
    }
}
