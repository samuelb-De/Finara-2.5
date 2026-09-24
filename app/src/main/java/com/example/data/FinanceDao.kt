package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // Transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isAntExpense = 1 ORDER BY date DESC")
    fun getAntExpenses(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(item: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(item: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(item: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("UPDATE transactions SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateTransactionCategory(oldCategory: String, newCategory: String)

    // Categories
    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(item: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(items: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(item: CategoryEntity)

    @Delete
    suspend fun deleteCategory(item: CategoryEntity)

    // Budgets
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(item: BudgetEntity): Long

    @Update
    suspend fun updateBudget(item: BudgetEntity)

    @Delete
    suspend fun deleteBudget(item: BudgetEntity)

    // Savings Goals
    @Query("SELECT * FROM savings_goals")
    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(item: SavingsGoalEntity): Long

    @Update
    suspend fun updateSavingsGoal(item: SavingsGoalEntity)

    @Delete
    suspend fun deleteSavingsGoal(item: SavingsGoalEntity)

    @Query("SELECT * FROM savings_transactions ORDER BY date DESC")
    fun getAllSavingsTransactions(): Flow<List<SavingsTransactionEntity>>

    @Query("SELECT * FROM savings_transactions WHERE goalId = :goalId ORDER BY date DESC")
    fun getSavingsTransactionsForGoal(goalId: Long): Flow<List<SavingsTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsTransaction(item: SavingsTransactionEntity): Long

    // Future Expenses
    @Query("SELECT * FROM future_expenses ORDER BY dueDate ASC")
    fun getAllFutureExpenses(): Flow<List<FutureExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFutureExpense(item: FutureExpenseEntity): Long

    @Update
    suspend fun updateFutureExpense(item: FutureExpenseEntity)

    @Delete
    suspend fun deleteFutureExpense(item: FutureExpenseEntity)

    // Debts
    @Query("SELECT * FROM debts")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(item: DebtEntity): Long

    @Update
    suspend fun updateDebt(item: DebtEntity)

    @Delete
    suspend fun deleteDebt(item: DebtEntity)

    @Query("SELECT * FROM debt_payments ORDER BY date DESC")
    fun getAllDebtPayments(): Flow<List<DebtPaymentEntity>>

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY date DESC")
    fun getDebtPaymentsForDebt(debtId: Long): Flow<List<DebtPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtPayment(item: DebtPaymentEntity): Long

    // User Settings
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getUserSettingsDirect(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(item: UserSettingsEntity)

    // Bulk delete for demo clearing
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAllSavingsGoals()

    @Query("DELETE FROM savings_transactions")
    suspend fun deleteAllSavingsTransactions()

    @Query("DELETE FROM future_expenses")
    suspend fun deleteAllFutureExpenses()

    @Query("DELETE FROM debts")
    suspend fun deleteAllDebts()

    @Query("DELETE FROM debt_payments")
    suspend fun deleteAllDebtPayments()

    // Credit Card Reminders
    @Query("SELECT * FROM credit_card_reminders ORDER BY id ASC")
    fun getAllCreditCardReminders(): Flow<List<CreditCardReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCardReminder(item: CreditCardReminderEntity): Long

    @Update
    suspend fun updateCreditCardReminder(item: CreditCardReminderEntity)

    @Delete
    suspend fun deleteCreditCardReminder(item: CreditCardReminderEntity)

    @Query("DELETE FROM credit_card_reminders")
    suspend fun deleteAllCreditCardReminders()
}
