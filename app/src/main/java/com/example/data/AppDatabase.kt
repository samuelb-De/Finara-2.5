package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        SavingsTransactionEntity::class,
        FutureExpenseEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        UserSettingsEntity::class,
        CreditCardReminderEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN categoryType TEXT NOT NULL DEFAULT 'EXPENSE'")
            }
        }

        fun getDatabase(
            context: Context,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finanzas_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.financeDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: FinanceDao) {
            val defaultCategories = listOf(
                // Gastos
                CategoryEntity(name = "Alimentación", iconName = "restaurant", colorHex = 0xFFF57C00, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                CategoryEntity(name = "Transporte", iconName = "directions_bus", colorHex = 0xFF1976D2, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                CategoryEntity(name = "Vivienda", iconName = "home", colorHex = 0xFF00897B, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                CategoryEntity(name = "Educación", iconName = "school", colorHex = 0xFF7B1FA2, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                CategoryEntity(name = "Salud", iconName = "local_hospital", colorHex = 0xFFE53935, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                CategoryEntity(name = "Entretenimiento", iconName = "sports_esports", colorHex = 0xFF5E35B1, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                CategoryEntity(name = "Tecnología", iconName = "memory", colorHex = 0xFF0288D1, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                CategoryEntity(name = "Ropa", iconName = "checkroom", colorHex = 0xFFD81B60, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                CategoryEntity(name = "Servicios", iconName = "electrical_services", colorHex = 0xFFFF8F00, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                CategoryEntity(name = "Deudas", iconName = "credit_card", colorHex = 0xFF8D6E63, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                CategoryEntity(name = "Ahorro", iconName = "savings", colorHex = 0xFF2E7D32, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                CategoryEntity(name = "Otros", iconName = "more_horiz", colorHex = 0xFF757575, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                // Ingresos
                CategoryEntity(name = "Salario", iconName = "attach_money", colorHex = 0xFF10B981, isActive = true, isDefault = true, categoryType = "INCOME"),
                CategoryEntity(name = "Honorarios / Freelance", iconName = "receipt", colorHex = 0xFF0D9488, isActive = true, isDefault = true, categoryType = "INCOME"),
                CategoryEntity(name = "Negocio / Ventas", iconName = "store", colorHex = 0xFF3B82F6, isActive = true, isDefault = true, categoryType = "INCOME"),
                CategoryEntity(name = "Inversiones", iconName = "trending_up", colorHex = 0xFF8B5CF6, isActive = true, isDefault = true, categoryType = "INCOME"),
                CategoryEntity(name = "Regalos", iconName = "card_giftcard", colorHex = 0xFFEC4899, isActive = true, isDefault = true, categoryType = "INCOME"),
                CategoryEntity(name = "Otros ingresos", iconName = "payments", colorHex = 0xFF64748B, isActive = true, isDefault = true, categoryType = "INCOME")
            )
            dao.insertCategories(defaultCategories)

            // Insert initial default settings
            dao.insertUserSettings(
                UserSettingsEntity(
                    id = 1,
                    userName = "Usuario",
                    userEmail = "usuario@correo.com",
                    userHandle = "usuario",
                    currency = "COP",
                    antExpenseLimit = 5000.0,
                    promptAntExpense = true,
                    isDarkMode = false,
                    notificationsEnabled = true,
                    hasCompletedOnboarding = false
                )
            )
        }
    }
}
