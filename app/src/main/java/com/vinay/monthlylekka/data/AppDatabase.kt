package com.vinay.monthlylekka.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(entities = [Category::class, Expense::class, Lekka::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun lekkaDao(): LekkaDao

    suspend fun clearDatabase() = withContext(Dispatchers.IO) {
        clearAllTables()
        populateDatabase()
    }

    suspend fun populateDatabase() {
        val masterLekkaId = lekkaDao().insertLekka(Lekka(name = "Master Expense Table", isMotherTable = true, isDefault = false))
        val defaultChildLekkaId = lekkaDao().insertLekka(Lekka(name = "Monthly Expenses", isMotherTable = false, isDefault = true))

        val categories = listOf(
            Category(lekkaId = defaultChildLekkaId, name = "Income", colorHex = "#2E7D32", isIncome = true),
            Category(lekkaId = defaultChildLekkaId, name = "Kirani", colorHex = "#FFB300", isIncome = false),
            Category(lekkaId = defaultChildLekkaId, name = "Kaipalle", colorHex = "#43A047", isIncome = false),
            Category(lekkaId = defaultChildLekkaId, name = "Food", colorHex = "#E53935", isIncome = false),
            Category(lekkaId = defaultChildLekkaId, name = "Bills", colorHex = "#3949AB", isIncome = false),
            Category(lekkaId = defaultChildLekkaId, name = "Others", colorHex = "#757575", isIncome = false),
            Category(lekkaId = defaultChildLekkaId, name = "Travel", colorHex = "#1E88E5", isIncome = false),
            Category(lekkaId = defaultChildLekkaId, name = "Hospital", colorHex = "#D81B60", isIncome = false)
        )
        categories.forEach { categoryDao().insertCategory(it) }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `lekkas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                database.execSQL("INSERT OR IGNORE INTO lekkas (id, name) VALUES (1, 'Default')")
                database.execSQL("CREATE TABLE IF NOT EXISTS `categories_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `lekkaId` INTEGER NOT NULL, `name` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `isIncome` INTEGER NOT NULL, FOREIGN KEY(`lekkaId`) REFERENCES `lekkas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("INSERT INTO categories_new (id, lekkaId, name, colorHex, isIncome) SELECT id, 1, name, colorHex, isIncome FROM categories")
                database.execSQL("DROP TABLE categories")
                database.execSQL("ALTER TABLE categories_new RENAME TO categories")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_lekkaId` ON `categories` (`lekkaId`)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `expenses_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `lekkaId` INTEGER NOT NULL, `description` TEXT NOT NULL, `amount` REAL NOT NULL, `categoryId` INTEGER NOT NULL, `date` TEXT NOT NULL, FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`lekkaId`) REFERENCES `lekkas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("INSERT INTO expenses_new (id, lekkaId, description, amount, categoryId, date) SELECT id, 1, description, amount, categoryId, date FROM expenses")
                database.execSQL("DROP TABLE expenses")
                database.execSQL("ALTER TABLE expenses_new RENAME TO expenses")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_categoryId` ON `expenses` (`categoryId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_lekkaId` ON `expenses` (`lekkaId`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `lekkas` ADD COLUMN `startDate` TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE `lekkas` ADD COLUMN `endDate` TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `lekkas` ADD COLUMN `isDefault` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("UPDATE `lekkas` SET `isDefault` = 1 WHERE `id` = (SELECT `id` FROM `lekkas` ORDER BY `id` ASC LIMIT 1)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `lekkas` ADD COLUMN `isMotherTable` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("""
                    INSERT OR IGNORE INTO `lekkas` (`name`, `startDate`, `endDate`, `isDefault`, `isMotherTable`) 
                    VALUES ('Master Expense Table', NULL, NULL, 0, 1)
                """)
                database.execSQL("UPDATE `lekkas` SET `isDefault` = CASE WHEN `isMotherTable` = 0 AND `id` = (SELECT `id` FROM `lekkas` WHERE `isMotherTable` = 0 ORDER BY `id` ASC LIMIT 1) THEN 1 ELSE 0 END")
                database.execSQL("UPDATE `lekkas` SET `name` = 'Master Expense Table' WHERE `isMotherTable` = 1")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monthly_lekka_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(AppDatabaseCallback(scope))
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    database.populateDatabase()
                }
            }
        }
    }
}
