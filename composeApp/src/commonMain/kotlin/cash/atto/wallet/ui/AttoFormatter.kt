package cash.atto.wallet.ui

import com.ionspin.kotlin.bignum.decimal.BigDecimal

internal const val ATTO_CASH_DECIMALS = 9
private const val ATTO_CASH_LABEL = "Atto Cash"
private const val PLACEHOLDER = "…"

object AttoFormatter {

    fun format(value: BigDecimal?): String {
        return value
            ?.toStringExpanded()
            ?.let(::formatAmountString)
            ?: PLACEHOLDER
    }

    fun format(value: String?): String {
        return value
            ?.let(::formatAmountString)
            ?: PLACEHOLDER
    }

    fun format(value: ULong): String =
        AttoLocalizedFormatter.format(value.toString())

    private fun formatAmountString(rawValue: String): String {
        val localized = AttoLocalizedFormatter.format(rawValue)
        return if (localized == PLACEHOLDER) {
            PLACEHOLDER
        } else {
            "$localized $ATTO_CASH_LABEL"
        }
    }
}
