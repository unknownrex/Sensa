package com.unknownrex.sensa.core.data.mapper

import com.unknownrex.sensa.core.database.entity.CategoryEntity
import com.unknownrex.sensa.core.database.entity.MerchantRuleEntity
import com.unknownrex.sensa.core.database.entity.TransactionEntity
import com.unknownrex.sensa.core.domain.model.Category
import com.unknownrex.sensa.core.domain.model.MerchantRule
import com.unknownrex.sensa.core.domain.model.PaymentSource
import com.unknownrex.sensa.core.domain.model.Transaction
import com.unknownrex.sensa.core.domain.model.TransactionStatus

fun TransactionEntity.toTransaction(): Transaction = Transaction(
    id = id,
    amountRupiah = amountRupiah,
    merchant = merchant,
    categoryId = categoryId,
    source = PaymentSource.fromId(source),
    timestamp = timestamp,
    note = note,
    status = TransactionStatus.entries.firstOrNull { it.name == status } ?: TransactionStatus.NEEDS_REVIEW,
    confidence = confidence,
    dedupeKey = dedupeKey,
    createdAt = createdAt,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amountRupiah = amountRupiah,
    merchant = merchant,
    categoryId = categoryId,
    source = source.id,
    timestamp = timestamp,
    note = note,
    status = status.name,
    confidence = confidence,
    dedupeKey = dedupeKey,
    createdAt = createdAt,
)

fun CategoryEntity.toCategory(): Category = Category(
    id = id,
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    isDefault = isDefault,
    sortOrder = sortOrder,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    isDefault = isDefault,
    sortOrder = sortOrder,
)

fun MerchantRuleEntity.toMerchantRule(): MerchantRule = MerchantRule(
    id = id,
    normalizedMerchant = normalizedMerchant,
    categoryId = categoryId,
    isUserLearned = isUserLearned,
    hitCount = hitCount,
    createdAt = createdAt,
)

fun MerchantRule.toEntity(): MerchantRuleEntity = MerchantRuleEntity(
    id = id,
    normalizedMerchant = normalizedMerchant,
    categoryId = categoryId,
    isUserLearned = isUserLearned,
    hitCount = hitCount,
    createdAt = createdAt,
)
