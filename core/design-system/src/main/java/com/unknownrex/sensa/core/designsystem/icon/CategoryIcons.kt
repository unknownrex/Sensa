package com.unknownrex.sensa.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Category identity is icon + color + label everywhere (design.md §12). Icon keys are stable
 * strings persisted with the category row.
 */
object CategoryIcons {
    const val FOOD = "food"
    const val GROCERIES = "groceries"
    const val TRANSPORT = "transport"
    const val SHOPPING = "shopping"
    const val SUBSCRIPTIONS = "subscriptions"
    const val BILLS = "bills"
    const val HEALTH = "health"
    const val EDUCATION = "education"
    const val ENTERTAINMENT = "entertainment"
    const val UNCATEGORIZED = "uncategorized"

    fun iconFor(key: String): ImageVector = when (key) {
        FOOD -> Icons.Outlined.Restaurant
        GROCERIES -> Icons.Outlined.LocalGroceryStore
        TRANSPORT -> Icons.Outlined.DirectionsBus
        SHOPPING -> Icons.Outlined.ShoppingBag
        SUBSCRIPTIONS -> Icons.Outlined.Subscriptions
        BILLS -> Icons.Outlined.ReceiptLong
        HEALTH -> Icons.Outlined.FavoriteBorder
        EDUCATION -> Icons.Outlined.School
        ENTERTAINMENT -> Icons.Outlined.Movie
        else -> Icons.Outlined.Category
    }
}
