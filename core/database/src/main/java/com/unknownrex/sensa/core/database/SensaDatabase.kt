package com.unknownrex.sensa.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.unknownrex.sensa.core.database.dao.CategoryDao
import com.unknownrex.sensa.core.database.dao.MerchantRuleDao
import com.unknownrex.sensa.core.database.dao.TransactionDao
import com.unknownrex.sensa.core.database.entity.CategoryEntity
import com.unknownrex.sensa.core.database.entity.MerchantRuleEntity
import com.unknownrex.sensa.core.database.entity.TransactionEntity
import com.unknownrex.sensa.core.domain.model.DefaultCategories

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        MerchantRuleEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SensaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun merchantRuleDao(): MerchantRuleDao

    companion object {
        const val NAME = "sensa.db"

        fun build(context: Context): SensaDatabase = Room
            .databaseBuilder(context, SensaDatabase::class.java, NAME)
            .addCallback(SeedCategoriesCallback)
            .build()

        fun buildInMemory(context: Context): SensaDatabase = Room
            .inMemoryDatabaseBuilder(context, SensaDatabase::class.java)
            .addCallback(SeedCategoriesCallback)
            .build()
    }
}

/**
 * Default categories ship with the database so a fresh install can categorize immediately.
 */
private object SeedCategoriesCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        DefaultCategories.All.forEach { category ->
            db.execSQL(
                "INSERT OR IGNORE INTO categories " +
                    "(id, name, icon_key, color_argb, is_default, sort_order) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(
                    category.id,
                    category.name,
                    category.iconKey,
                    category.colorArgb,
                    if (category.isDefault) 1 else 0,
                    category.sortOrder,
                ),
            )
        }
    }
}
