package com.unknownrex.sensa.core.presentation

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val Indonesia = Locale("id", "ID")

private val rupiahFormatter = ThreadLocal.withInitial {
    DecimalFormat("#,##0", DecimalFormatSymbols(Indonesia)).apply {
        maximumFractionDigits = 0
        isGroupingUsed = true
    }
}

/**
 * Formats whole rupiah as `Rp35.000` with id-ID grouping, never decimals (design.md §4).
 */
fun formatRupiah(amount: Long): String {
    val formatted = rupiahFormatter.get()!!.format(amount)
    return "Rp$formatted"
}

/**
 * Parses user input such as `35.000`, `35000` or `Rp 35.000` into whole rupiah. Returns null for
 * anything that cannot be read as a positive amount.
 */
fun parseRupiahInput(input: String): Long? {
    val digits = input.filter { it.isDigit() }
    if (digits.isEmpty()) return null
    val value = digits.toLongOrNull() ?: return null
    return value.takeIf { it > 0L }
}

/**
 * Groups raw digits with id-ID separators while the user types, so the amount field shows
 * `35.000` without a currency prefix.
 */
fun formatRupiahDigits(input: String): String {
    val digits = input.filter { it.isDigit() }.trimStart('0').ifEmpty { return "" }
    val value = digits.toLongOrNull() ?: return ""
    return rupiahFormatter.get()!!.format(value)
}
