package com.ilmariware.currencyconverterwidget.data.models

enum class Currency(val code: String, val displayName: String, val symbol: String, val isCommon: Boolean = false) {
    // Most Common Currencies
    USD("USD", "US Dollar", "$", true),
    EUR("EUR", "Euro", "€", true),
    GBP("GBP", "British Pound", "£", true),
    JPY("JPY", "Japanese Yen", "¥", true),
    CAD("CAD", "Canadian Dollar", "C$", true),
    AUD("AUD", "Australian Dollar", "A$", true),
    CHF("CHF", "Swiss Franc", "CHF", true),
    CNY("CNY", "Chinese Yuan", "¥", true),
    INR("INR", "Indian Rupee", "₹", true),
    
    // All Other Currencies (Alphabetical)
    BGN("BGN", "Bulgarian Lev", "лв"),
    BRL("BRL", "Brazilian Real", "R$"),
    CZK("CZK", "Czech Koruna", "Kč"),
    DKK("DKK", "Danish Krone", "kr"),
    HKD("HKD", "Hong Kong Dollar", "HK$"),
    HUF("HUF", "Hungarian Forint", "Ft"),
    IDR("IDR", "Indonesian Rupiah", "Rp"),
    ILS("ILS", "Israeli Shekel", "₪"),
    ISK("ISK", "Icelandic Króna", "kr"),
    KRW("KRW", "South Korean Won", "₩"),
    MXN("MXN", "Mexican Peso", "MX$"),
    MYR("MYR", "Malaysian Ringgit", "RM"),
    NOK("NOK", "Norwegian Krone", "kr"),
    NZD("NZD", "New Zealand Dollar", "NZ$"),
    PHP("PHP", "Philippine Peso", "₱"),
    PLN("PLN", "Polish Złoty", "zł"),
    RON("RON", "Romanian Leu", "lei"),
    SEK("SEK", "Swedish Krona", "kr"),
    SGD("SGD", "Singapore Dollar", "S$"),
    THB("THB", "Thai Baht", "฿"),
    TRY("TRY", "Turkish Lira", "₺"),
    ZAR("ZAR", "South African Rand", "R");

    companion object {
        fun fromCode(code: String): Currency? {
            return values().find { it.code == code }
        }
        
        fun getCommonCurrencies(): List<Currency> {
            return values().filter { it.isCommon }
        }
        
        fun getOtherCurrencies(): List<Currency> {
            return values().filter { !it.isCommon }
        }
    }
}



