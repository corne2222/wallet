package cash.atto.wallet.ui

@JsFun("() => navigator.language || 'en-US'")
private external fun currentLocale(): String

@JsFun("""
    (value, locale, maxFractionDigits, minFractionDigits) => new Intl.NumberFormat(locale, {
        maximumFractionDigits: maxFractionDigits,
        minimumFractionDigits: minFractionDigits
    }).format(value)
""")
private external fun intlFormat(
    value: Double,
    locale: String,
    maxFractionDigits: Int,
    minFractionDigits: Int
): String

actual object AttoLocalizedFormatter {
    actual fun format(value: String): String =
        value.toDoubleOrNull()?.let {
            intlFormat(it, currentLocale(), ATTO_CASH_DECIMALS, 0)
        } ?: "…"
}
