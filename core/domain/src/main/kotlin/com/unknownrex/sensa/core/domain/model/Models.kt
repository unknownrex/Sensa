package com.unknownrex.sensa.core.domain.model

/** Where a transaction came from. Manual rows are entered by the user. */
enum class PaymentSource(val id: String) {
    GOPAY("gopay"),
    BCA_MOBILE("bca_mobile"),
    MANUAL("manual"),
    UNKNOWN("unknown"),
    ;

    companion object {
        fun fromId(id: String): PaymentSource = entries.firstOrNull { it.id == id } ?: UNKNOWN
    }
}

enum class TransactionStatus {
    /** Saved automatically above the confidence threshold. */
    AUTO,

    /** Confirmed by the user (directly or through the review inbox). */
    CONFIRMED,

    /** Below the confidence threshold; waiting in the review inbox. */
    NEEDS_REVIEW,
}

data class Transaction(
    val id: String,
    val amountRupiah: Long,
    val merchant: String,
    val categoryId: String?,
    val source: PaymentSource,
    val timestamp: Long,
    val note: String? = null,
    val status: TransactionStatus,
    val confidence: Float? = null,
    val dedupeKey: String? = null,
    val createdAt: Long,
) {
    val isUncategorized: Boolean get() = categoryId == null
}

data class Category(
    val id: String,
    val name: String,
    val iconKey: String,
    val colorArgb: Long,
    val isDefault: Boolean,
    val sortOrder: Int,
)

data class MerchantRule(
    val id: String,
    val normalizedMerchant: String,
    val categoryId: String,
    val isUserLearned: Boolean,
    val hitCount: Int = 0,
    val createdAt: Long,
)

/** Small, serializable snapshot of the user's preferences. */
data class SensaSettings(
    val onboardingCompleted: Boolean = false,
    val dynamicColor: Boolean = false,
    val reduceTransparency: Boolean = false,
    val reduceMotion: Boolean = false,
)
