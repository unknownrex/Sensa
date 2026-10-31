package com.unknownrex.sensa.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["status", "timestamp"]),
        Index(value = ["category_id", "timestamp"]),
        Index(value = ["dedupe_key"], unique = true),
    ],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "amount_rupiah") val amountRupiah: Long,
    val merchant: String,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    val source: String,
    val timestamp: Long,
    val note: String?,
    val status: String,
    val confidence: Float?,
    @ColumnInfo(name = "dedupe_key") val dedupeKey: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "icon_key") val iconKey: String,
    @ColumnInfo(name = "color_argb") val colorArgb: Long,
    @ColumnInfo(name = "is_default") val isDefault: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)

@Entity(
    tableName = "merchant_rules",
    indices = [Index(value = ["normalized_merchant"], unique = true)],
)
data class MerchantRuleEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "normalized_merchant") val normalizedMerchant: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "is_user_learned") val isUserLearned: Boolean,
    @ColumnInfo(name = "hit_count") val hitCount: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
