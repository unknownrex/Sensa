package com.unknownrex.sensa.core.data.repository

import com.unknownrex.sensa.core.data.mapper.toCategory
import com.unknownrex.sensa.core.data.mapper.toEntity
import com.unknownrex.sensa.core.data.mapper.toMerchantRule
import com.unknownrex.sensa.core.data.mapper.toEntity as ruleToEntity
import com.unknownrex.sensa.core.database.dao.CategoryDao
import com.unknownrex.sensa.core.database.dao.MerchantRuleDao
import com.unknownrex.sensa.core.domain.DataError
import com.unknownrex.sensa.core.domain.EmptyResult
import com.unknownrex.sensa.core.domain.model.Category
import com.unknownrex.sensa.core.domain.model.MerchantRule
import com.unknownrex.sensa.core.domain.repository.CategoryRepository
import com.unknownrex.sensa.core.domain.repository.MerchantRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryRepository(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { rows -> rows.map { it.toCategory() } }

    override suspend fun getCategory(id: String): Category? = categoryDao.getById(id)?.toCategory()

    override suspend fun updateCategory(category: Category): EmptyResult<DataError.Local> =
        dbCall { categoryDao.upsert(category.toEntity()) }
}

class RoomMerchantRuleRepository(
    private val merchantRuleDao: MerchantRuleDao,
) : MerchantRuleRepository {
    override fun observeRules(): Flow<List<MerchantRule>> =
        merchantRuleDao.observeAll().map { rows -> rows.map { it.toMerchantRule() } }

    override suspend fun findRule(normalizedMerchant: String): MerchantRule? =
        merchantRuleDao.findByMerchant(normalizedMerchant)?.toMerchantRule()

    override suspend fun upsert(rule: MerchantRule): EmptyResult<DataError.Local> =
        dbCall { merchantRuleDao.insert(rule.ruleToEntity()) }

    override suspend fun delete(id: String): EmptyResult<DataError.Local> =
        dbCall { merchantRuleDao.deleteById(id) }
}
