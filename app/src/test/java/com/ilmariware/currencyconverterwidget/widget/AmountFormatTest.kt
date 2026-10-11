package com.ilmariware.currencyconverterwidget.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.DecimalFormatSymbols
import java.util.Locale

class AmountFormatTest {

    @Test
    fun usInputGroupsThousandsAndKeepsTypedDecimals() {
        assertEquals("0", AmountFormat.formatInput("0", Locale.US))
        assertEquals("1,000", AmountFormat.formatInput("1000", Locale.US))
        assertEquals("1,234,567", AmountFormat.formatInput("1234567", Locale.US))
        assertEquals("1,234,567.", AmountFormat.formatInput("1234567.", Locale.US))
        assertEquals("1,234,567.8", AmountFormat.formatInput("1234567.8", Locale.US))
        assertEquals("1,234,567.80", AmountFormat.formatInput("1234567.80", Locale.US))
        assertEquals("0.5", AmountFormat.formatInput("0.5", Locale.US))
    }

    @Test
    fun germanUsesLocaleSeparators() {
        assertEquals("1.234.567,89", AmountFormat.formatInput("1234567.89", Locale.GERMANY))
        assertEquals(",", AmountFormat.decimalSeparator(Locale.GERMANY).toString())
    }

    @Test
    fun resultHonorsDecimalSettingAndCopyStaysRaw() {
        assertEquals("1,234.50", AmountFormat.formatResult(1234.5, showDecimals = true, Locale.US))
        assertEquals("1,235", AmountFormat.formatResult(1234.5, showDecimals = false, Locale.US))
        assertEquals("1.234,50", AmountFormat.formatResult(1234.5, showDecimals = true, Locale.GERMANY))
        assertEquals("1234.50", AmountFormat.rawResult(1234.5))
        assertEquals("1234567.89", AmountFormat.rawResult(1234567.89))
        assertFalse(AmountFormat.rawResult(1234567.89).contains(","))
    }

    @Test
    fun unusableSeparatorsFallBackToCommas() {
        assertEquals(',' to '.', AmountFormat.resolveSeparators(null))

        val same = DecimalFormatSymbols(Locale.US)
        same.groupingSeparator = same.decimalSeparator
        assertEquals(',' to '.', AmountFormat.resolveSeparators(same))

        val missing = DecimalFormatSymbols(Locale.US)
        missing.groupingSeparator = '\u0000'
        assertEquals(',' to '.', AmountFormat.resolveSeparators(missing))
    }

    @Test
    fun manualGroupingUsesTheFallbackSeparator() {
        assertEquals("1,234,567", AmountFormat.groupEveryThree("1234567", ','))
    }

    @Test
    fun localeWithoutGroupingStillInsertsCommas() {
        val formatted = AmountFormat.formatInput("1234567.89", Locale.ROOT)
        assertTrue(formatted.startsWith("1,234,567"))
        assertEquals("123456789", formatted.filter { it.isDigit() })
    }
}
