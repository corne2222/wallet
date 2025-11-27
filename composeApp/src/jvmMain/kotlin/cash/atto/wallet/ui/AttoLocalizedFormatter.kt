package cash.atto.wallet.ui

import java.text.NumberFormat
import java.util.Locale

actual object AttoLocalizedFormatter {
    actual fun format(value: String): String = try {
        NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            maximumFractionDigits = ATTO_CASH_DECIMALS
            minimumFractionDigits = 0
        }.format(value.toBigDecimal())
    } catch (_: Exception) {
        "…"
    }
}
