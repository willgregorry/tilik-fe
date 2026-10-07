package id.tilik.app.data.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object FinancialFormatter {

    private val indonesianSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val oneDecimalFormat = DecimalFormat("#,##0.0", indonesianSymbols)
    private val twoDecimalFormat = DecimalFormat("#,##0.00", indonesianSymbols)

    fun formatIdrShort(value: Double): String {
        val absVal = abs(value)
        val prefix = if (value < 0) "-Rp " else "Rp "

        return when {
            absVal >= 1_000_000_000_000.0 -> {
                prefix + oneDecimalFormat.format(absVal / 1_000_000_000_000.0) + " Triliun"
            }
            absVal >= 1_000_000_000.0 -> {
                prefix + oneDecimalFormat.format(absVal / 1_000_000_000.0) + " Miliar"
            }
            absVal >= 1_000_000.0 -> {
                prefix + oneDecimalFormat.format(absVal / 1_000_000.0) + " Juta"
            }
            else -> {
                prefix + oneDecimalFormat.format(absVal)
            }
        }
    }

    fun formatRatio(value: Double?): String {
        if (value == null) return "-"
        return "${oneDecimalFormat.format(value)}x"
    }

    fun formatPercentage(value: Double?): String {
        if (value == null) return "-"
        val sign = if (value > 0) "+" else ""
        return "$sign${oneDecimalFormat.format(value)}%"
    }
}
