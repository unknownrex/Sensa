package com.unknownrex.sensa.core.domain.categorization

/**
 * Default keyword rules from prd.md §8. Order matters: the first match wins and more specific
 * patterns sit above generic ones. Patterns are matched case-insensitively against the
 * normalized merchant text.
 */
data class DefaultMerchantRule(
    val pattern: String,
    val categoryId: String,
    val confidence: Float,
)

object DefaultMerchantRules {
    val All: List<DefaultMerchantRule> = listOf(
        // Subscriptions first: these merchants also look like "entertainment".
        rule("netflix", "subscriptions", 0.9f),
        rule("spotify", "subscriptions", 0.9f),
        rule("disney", "subscriptions", 0.9f),
        rule("vidio", "subscriptions", 0.9f),
        rule("viu", "subscriptions", 0.9f),
        rule("youtube premium", "subscriptions", 0.9f),
        rule("apple", "subscriptions", 0.6f),
        rule("google one", "subscriptions", 0.9f),
        rule("icloud", "subscriptions", 0.9f),
        rule("canva", "subscriptions", 0.85f),

        rule("indomaret", "groceries", 0.9f),
        rule("alfamart", "groceries", 0.9f),
        rule("alfamidi", "groceries", 0.9f),
        rule("superindo", "groceries", 0.9f),
        rule("hypermart", "groceries", 0.9f),
        rule("transmart", "groceries", 0.9f),
        rule("ranch market", "groceries", 0.9f),
        rule("sayur", "groceries", 0.7f),
        rule("pasar", "groceries", 0.6f),

        rule("gojek", "transport", 0.9f),
        rule("grab", "transport", 0.9f),
        rule("maxim", "transport", 0.85f),
        rule("transjakarta", "transport", 0.9f),
        rule("krl", "transport", 0.85f),
        rule("mrt", "transport", 0.85f),
        rule("lrt", "transport", 0.85f),
        rule("pertamina", "transport", 0.85f),
        rule("spbu", "transport", 0.8f),
        rule("parkir", "transport", 0.75f),
        rule("tol", "transport", 0.7f),

        rule("starbucks", "food", 0.85f),
        rule("kopi", "food", 0.8f),
        rule("coffee", "food", 0.8f),
        rule("kfc", "food", 0.85f),
        rule("mcdonald", "food", 0.85f),
        rule("burger", "food", 0.8f),
        rule("pizza", "food", 0.8f),
        rule("warung", "food", 0.7f),
        rule("resto", "food", 0.7f),
        rule("restoran", "food", 0.75f),
        rule("bakso", "food", 0.8f),
        rule("mie", "food", 0.7f),
        rule("ayam", "food", 0.7f),
        rule("nasi", "food", 0.7f),
        rule("gofood", "food", 0.85f),
        rule("grabfood", "food", 0.85f),

        rule("shopee", "shopping", 0.85f),
        rule("tokopedia", "shopping", 0.85f),
        rule("lazada", "shopping", 0.85f),
        rule("blibli", "shopping", 0.85f),
        rule("tiktok shop", "shopping", 0.85f),
        rule("uniqlo", "shopping", 0.8f),

        rule("pln", "bills", 0.9f),
        rule("listrik", "bills", 0.9f),
        rule("telkom", "bills", 0.85f),
        rule("indihome", "bills", 0.85f),
        rule("biznet", "bills", 0.85f),
        rule("pdam", "bills", 0.9f),
        rule("air", "bills", 0.5f),
        rule("pulsa", "bills", 0.8f),
        rule("paket data", "bills", 0.85f),
        rule("bpjs", "bills", 0.9f),
        rule("telkomsel", "bills", 0.85f),
        rule("indosat", "bills", 0.85f),
        rule("xl", "bills", 0.7f),

        rule("apotek", "health", 0.85f),
        rule("kimia farma", "health", 0.9f),
        rule("guardian", "health", 0.8f),
        rule("klinik", "health", 0.85f),
        rule("rumah sakit", "health", 0.9f),
        rule("dokter", "health", 0.8f),
        rule("gym", "health", 0.8f),

        rule("sekolah", "education", 0.8f),
        rule("kampus", "education", 0.8f),
        rule("udemy", "education", 0.85f),
        rule("coursera", "education", 0.85f),
        rule("buku", "education", 0.7f),

        rule("bioskop", "entertainment", 0.85f),
        rule("cgv", "entertainment", 0.85f),
        rule("xxi", "entertainment", 0.85f),
        rule("cinema", "entertainment", 0.8f),
        rule("steam", "entertainment", 0.85f),
        rule("game", "entertainment", 0.6f),
    )

    private fun rule(pattern: String, categoryId: String, confidence: Float) =
        DefaultMerchantRule(pattern = pattern, categoryId = categoryId, confidence = confidence)
}
