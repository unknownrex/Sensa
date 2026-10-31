package com.unknownrex.sensa.core.capture.parser

/**
 * Shared amount/merchant extraction for rupiah payment notifications.
 *
 * Amounts are always whole rupiah: `Rp25.000` and `Rp25,000` both become `25000`.
 */
internal object RupiahMatcher {

    private val amountRegex = Regex("""(?i)\bRp\.?\s*([0-9][0-9.,]*)""")

    private val merchantPatterns = listOf(
        Regex(
            """(?i)\bke\s+([A-Za-z0-9.'\- ]{3,60}?)""" +
                """(?=\.|,|\s+sebesar|\s+senilai|\s+berhasil|\s+pada|$)""",
        ),
        Regex(
            """(?i)\bdi\s+([A-Za-z0-9.'\- ]{3,60}?)""" +
                """(?=\.|,|\s+sebesar|\s+senilai|\s+berhasil|\s+pada|$)""",
        ),
        Regex(
            """(?i)\bdari\s+([A-Za-z0-9.'\- ]{3,60}?)""" +
                """(?=\.|,|\s+sebesar|\s+senilai|\s+berhasil|$)""",
        ),
    )

    /** Words that show up near a merchant but are never the merchant itself. */
    private val noiseWords = listOf(
        "aplikasi", "gopay", "gojek", "bca", "mobile", "lihat", "detail", "transaksi",
        "pembayaran", "saldo", "rekening", "akun", "aplikasi gopay", "bca mobile",
    )

    fun amountOrNull(text: String): Long? {
        val match = amountRegex.find(text) ?: return null
        val digits = match.groupValues[1].filter { it.isDigit() }
        if (digits.isEmpty() || digits.length > 12) return null
        return digits.toLongOrNull()?.takeIf { it > 0L }
    }

    fun merchantOrNull(text: String): String? {
        for (pattern in merchantPatterns) {
            val raw = pattern.find(text)?.groupValues?.getOrNull(1)?.trim() ?: continue
            val cleaned = raw.trim(' ', '.', ',', '!')
            if (cleaned.length < 3) continue
            if (cleaned.all { it.isDigit() }) continue
            val lower = cleaned.lowercase()
            if (noiseWords.any { lower == it || lower.startsWith("$it ") }) continue
            return cleaned
        }
        return null
    }
}
