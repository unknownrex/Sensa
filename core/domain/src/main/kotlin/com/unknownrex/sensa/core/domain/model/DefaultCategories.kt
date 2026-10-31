package com.unknownrex.sensa.core.domain.model

/**
 * Default category set from design.md §3.2. Ids are stable strings because they are persisted on
 * transactions and merchant rules.
 */
object DefaultCategories {
    val All: List<Category> = listOf(
        category("food", "Food and drink", "food", 0xFFFFB454, 0),
        category("groceries", "Groceries", "groceries", 0xFF8BE28B, 1),
        category("transport", "Transport", "transport", 0xFF6AB7FF, 2),
        category("shopping", "Shopping", "shopping", 0xFFFF8FB1, 3),
        category("subscriptions", "Subscriptions", "subscriptions", 0xFFB79CFF, 4),
        category("bills", "Bills and utilities", "bills", 0xFFF5E27A, 5),
        category("health", "Health", "health", 0xFFFF7A7A, 6),
        category("education", "Education", "education", 0xFF5EEAD4, 7),
        category("entertainment", "Entertainment", "entertainment", 0xFFFF9F6B, 8),
        category("uncategorized", "Uncategorized", "uncategorized", 0xFF9DB5AE, 9),
    )

    const val UNCATEGORIZED_ID = "uncategorized"

    private fun category(
        id: String,
        name: String,
        iconKey: String,
        colorArgb: Long,
        sortOrder: Int,
    ) = Category(
        id = id,
        name = name,
        iconKey = iconKey,
        colorArgb = colorArgb,
        isDefault = true,
        sortOrder = sortOrder,
    )
}
