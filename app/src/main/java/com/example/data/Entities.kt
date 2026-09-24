package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "EXPENSE" or "INCOME"
    val title: String,
    val amount: Double,
    val category: String,
    val categoryIcon: String = "category",
    val categoryColor: Long = 0xFF4CAF50,
    val date: Long = System.currentTimeMillis(),
    val frequency: String = "Único", // Único, Diario, Semanal, Quincenal, Mensual
    val isAntExpense: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val isActive: Boolean = true,
    val isDefault: Boolean = false,
    val categoryType: String = "EXPENSE" // "EXPENSE" or "INCOME"
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val monthYear: String // e.g. "2026-09"
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val targetDate: Long = System.currentTimeMillis() + 30L * 24 * 3600 * 1000,
    val icon: String = "savings",
    val color: Long = 0xFF009688
)

@Entity(tableName = "savings_transactions")
data class SavingsTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val amount: Double, // positive deposit, negative withdrawal
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "future_expenses")
data class FutureExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val dueDate: Long,
    val category: String,
    val priority: String = "MEDIA", // ALTA, MEDIA, BAJA
    val repeat: String = "Único", // Único, Mensual, Anual
    val status: String = "PENDIENTE" // PENDIENTE, PAGADO, CANCELADO
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val creditor: String,
    val originalAmount: Double,
    val remainingBalance: Double,
    val installmentAmount: Double,
    val dueDate: Long,
    val remainingInstallments: Int,
    val interestRate: Double = 0.0,
    val frequency: String = "Mensual"
)

@Entity(tableName = "debt_payments")
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Long = 1,
    val userName: String = "Usuario",
    val userEmail: String = "usuario@correo.com",
    val userHandle: String = "usuario",
    val memberSince: String = "12 de marzo de 2024",
    val currency: String = "COP", // COP, USD, EUR, MXN
    val antExpenseLimit: Double = 5000.0,
    val promptAntExpense: Boolean = true,
    val isDarkMode: Boolean? = false,
    val notificationsEnabled: Boolean = true,
    val pinCode: String = "",
    val isPinEnabled: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val hideBalancesDefault: Boolean = false,
    val dateFormat: String = "DD/MM/AAAA",
    val firstDayOfPeriod: Int = 1,
    val biometricsEnabled: Boolean = true,
    val backupEnabled: Boolean = true,
    val reminderExpenses: Boolean = true,
    val reminderUpcomingPayments: Boolean = true,
    val reminderDebtDue: Boolean = true,
    val reminderBudgetLimit: Boolean = true,
    val reminderSavingsGoals: Boolean = false,
    val reminderWeeklySummary: Boolean = true,
    val password: String = "",
    val isAccountCreated: Boolean = false
)

@Entity(tableName = "credit_card_reminders")
data class CreditCardReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardName: String,
    val bankName: String,
    val feeAmount: Double,
    val feeFrequency: String = "Mensual", // Mensual, Trimestral, Semestral, Anual
    val cutOffDay: Int = 15,
    val paymentDay: Int = 5,
    val isExonerated: Boolean = false,
    val exonerationCondition: String = "",
    val reminderEnabled: Boolean = true,
    val notifyCutOff: Boolean = true,
    val notifyPayment: Boolean = true,
    val lastPaidMonthYear: String = "",
    val colorHex: Long = 0xFF1E3A8A
)
