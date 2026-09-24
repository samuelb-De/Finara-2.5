package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.NumberFormat
import java.util.*

class FinanceRepository(private val dao: FinanceDao) {

    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allBudgets: Flow<List<BudgetEntity>> = dao.getAllBudgets()
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = dao.getAllSavingsGoals()
    val allSavingsTransactions: Flow<List<SavingsTransactionEntity>> = dao.getAllSavingsTransactions()
    val allFutureExpenses: Flow<List<FutureExpenseEntity>> = dao.getAllFutureExpenses()
    val allDebts: Flow<List<DebtEntity>> = dao.getAllDebts()
    val allDebtPayments: Flow<List<DebtPaymentEntity>> = dao.getAllDebtPayments()
    val allCreditCardReminders: Flow<List<CreditCardReminderEntity>> = dao.getAllCreditCardReminders()
    val userSettings: Flow<UserSettingsEntity?> = dao.getUserSettings()

    suspend fun getUserSettingsDirect(): UserSettingsEntity? = dao.getUserSettingsDirect()

    suspend fun saveUserSettings(settings: UserSettingsEntity) {
        dao.insertUserSettings(settings)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return dao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        dao.updateTransaction(transaction)
    }

    suspend fun updateTransactionCategory(oldCategory: String, newCategory: String) {
        dao.updateTransactionCategory(oldCategory, newCategory)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        dao.deleteTransactionById(id)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return dao.insertCategory(category)
    }

    suspend fun insertCategories(categories: List<CategoryEntity>) {
        dao.insertCategories(categories)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        dao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        dao.deleteCategory(category)
    }

    suspend fun insertBudget(budget: BudgetEntity): Long {
        return dao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) {
        dao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        dao.deleteBudget(budget)
    }

    suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long {
        return dao.insertSavingsGoal(goal)
    }

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) {
        dao.updateSavingsGoal(goal)
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        dao.deleteSavingsGoal(goal)
    }

    suspend fun addSavingsTransaction(goal: SavingsGoalEntity, amount: Double, note: String) {
        val newSaved = (goal.savedAmount + amount).coerceAtLeast(0.0)
        dao.updateSavingsGoal(goal.copy(savedAmount = newSaved))
        dao.insertSavingsTransaction(
            SavingsTransactionEntity(
                goalId = goal.id,
                amount = amount,
                note = note
            )
        )
    }

    suspend fun insertSavingsTransaction(item: SavingsTransactionEntity): Long {
        return dao.insertSavingsTransaction(item)
    }

    suspend fun insertFutureExpense(futureExpense: FutureExpenseEntity): Long {
        return dao.insertFutureExpense(futureExpense)
    }

    suspend fun updateFutureExpense(futureExpense: FutureExpenseEntity) {
        dao.updateFutureExpense(futureExpense)
    }

    suspend fun deleteFutureExpense(futureExpense: FutureExpenseEntity) {
        dao.deleteFutureExpense(futureExpense)
    }

    suspend fun markFutureExpensePaid(futureExpense: FutureExpenseEntity) {
        dao.updateFutureExpense(futureExpense.copy(status = "PAGADO"))
        // Also register as an actual expense
        dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                title = futureExpense.title,
                amount = futureExpense.amount,
                category = futureExpense.category,
                date = System.currentTimeMillis(),
                frequency = futureExpense.repeat,
                isAntExpense = false,
                notes = "Pago de gasto futuro: ${futureExpense.title}"
            )
        )
    }

    suspend fun insertDebt(debt: DebtEntity): Long {
        return dao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: DebtEntity) {
        dao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: DebtEntity) {
        dao.deleteDebt(debt)
    }

    suspend fun recordDebtPayment(debt: DebtEntity, paymentAmount: Double, note: String) {
        val newBalance = (debt.remainingBalance - paymentAmount).coerceAtLeast(0.0)
        val remainingInstallments = if (debt.remainingInstallments > 0) debt.remainingInstallments - 1 else 0
        dao.updateDebt(
            debt.copy(
                remainingBalance = newBalance,
                remainingInstallments = remainingInstallments,
                dueDate = debt.dueDate + (30L * 24 * 3600 * 1000) // Next month
            )
        )
        dao.insertDebtPayment(
            DebtPaymentEntity(
                debtId = debt.id,
                amount = paymentAmount,
                date = System.currentTimeMillis(),
                note = note
            )
        )
        // Also register as expense under Deudas
        dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                title = "Pago deuda: ${debt.title}",
                amount = paymentAmount,
                category = "Deudas",
                date = System.currentTimeMillis(),
                isAntExpense = false,
                notes = "Cuota pagada a ${debt.creditor}"
            )
        )
    }

    suspend fun insertCreditCardReminder(item: CreditCardReminderEntity): Long {
        return dao.insertCreditCardReminder(item)
    }

    suspend fun updateCreditCardReminder(item: CreditCardReminderEntity) {
        dao.updateCreditCardReminder(item)
    }

    suspend fun deleteCreditCardReminder(item: CreditCardReminderEntity) {
        dao.deleteCreditCardReminder(item)
    }

    suspend fun payCreditCardFee(item: CreditCardReminderEntity) {
        val currentMonthYear = getCurrentMonthYear()
        dao.updateCreditCardReminder(item.copy(lastPaidMonthYear = currentMonthYear))
        if (item.feeAmount > 0) {
            dao.insertTransaction(
                TransactionEntity(
                    type = "EXPENSE",
                    title = "Cuota de manejo: ${item.cardName}",
                    amount = item.feeAmount,
                    category = "Deudas",
                    date = System.currentTimeMillis(),
                    frequency = item.feeFrequency,
                    isAntExpense = false,
                    notes = "Pago de cuota de manejo tarjeta ${item.bankName}"
                )
            )
        }
    }

    suspend fun clearDemoData() {
        dao.deleteAllTransactions()
        dao.deleteAllBudgets()
        dao.deleteAllSavingsGoals()
        dao.deleteAllSavingsTransactions()
        dao.deleteAllFutureExpenses()
        dao.deleteAllDebts()
        dao.deleteAllDebtPayments()
    }

    suspend fun loadDemoData() {
        clearDemoData()
        val now = System.currentTimeMillis()
        val oneDay = 24L * 3600 * 1000

        // Incomes
        dao.insertTransaction(
            TransactionEntity(
                type = "INCOME",
                title = "Salario Mensual",
                amount = 3500000.0,
                category = "Salario / Ingresos",
                categoryIcon = "attach_money",
                categoryColor = 0xFF1B5E20,
                date = now - (5 * oneDay),
                frequency = "Mensual",
                isAntExpense = false,
                notes = "Pago de nómina principal"
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = "INCOME",
                title = "Proyecto Freelance",
                amount = 750000.0,
                category = "Salario / Ingresos",
                categoryIcon = "attach_money",
                categoryColor = 0xFF1B5E20,
                date = now - (2 * oneDay),
                frequency = "Único",
                notes = "Desarrollo web cliente externo"
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = "INCOME",
                title = "Venta artículos usados",
                amount = 120000.0,
                category = "Otros",
                categoryIcon = "more_horiz",
                categoryColor = 0xFF757575,
                date = now,
                frequency = "Único",
                notes = "Venta bicicleta antigua"
            )
        )

        // Expenses
        dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                title = "Supermercado Éxito",
                amount = 345000.0,
                category = "Alimentación",
                categoryIcon = "restaurant",
                categoryColor = 0xFFF57C00,
                date = now - (3 * oneDay),
                isAntExpense = false,
                notes = "Compras quincena"
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                title = "Arriendo Apartamento",
                amount = 1100000.0,
                category = "Vivienda",
                categoryIcon = "home",
                categoryColor = 0xFF00897B,
                date = now - (10 * oneDay),
                frequency = "Mensual",
                isAntExpense = false,
                notes = "Pago arriendo mensual"
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                title = "Servicios Públicos (Luz y Gas)",
                amount = 145000.0,
                category = "Servicios",
                categoryIcon = "electrical_services",
                categoryColor = 0xFFFF8F00,
                date = now - (7 * oneDay),
                isAntExpense = false
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                title = "Gasolina Moto",
                amount = 35000.0,
                category = "Transporte",
                categoryIcon = "directions_bus",
                categoryColor = 0xFF1976D2,
                date = now - (1 * oneDay),
                isAntExpense = false
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                title = "Almuerzo Restaurante",
                amount = 18000.0,
                category = "Alimentación",
                categoryIcon = "restaurant",
                categoryColor = 0xFFF57C00,
                date = now,
                isAntExpense = false
            )
        )

        // Ant Expenses (Gastos hormiga)
        dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                title = "Café Latte y galleta",
                amount = 4500.0,
                category = "Alimentación",
                categoryIcon = "restaurant",
                categoryColor = 0xFFF57C00,
                date = now,
                isAntExpense = true,
                notes = "Puesto de la esquina"
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                title = "Snack papitas",
                amount = 2500.0,
                category = "Alimentación",
                categoryIcon = "restaurant",
                categoryColor = 0xFFF57C00,
                date = now,
                isAntExpense = true
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                title = "Transporte adicional (Pasaje)",
                amount = 3200.0,
                category = "Transporte",
                categoryIcon = "directions_bus",
                categoryColor = 0xFF1976D2,
                date = now - (1 * oneDay),
                isAntExpense = true
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                title = "Agua embotellada",
                amount = 2000.0,
                category = "Alimentación",
                categoryIcon = "restaurant",
                categoryColor = 0xFFF57C00,
                date = now - (2 * oneDay),
                isAntExpense = true
            )
        )

        // Budgets
        dao.insertBudget(
            BudgetEntity(
                category = "Alimentación",
                monthlyLimit = 600000.0,
                monthYear = getCurrentMonthYear()
            )
        )
        dao.insertBudget(
            BudgetEntity(
                category = "Transporte",
                monthlyLimit = 150000.0,
                monthYear = getCurrentMonthYear()
            )
        )
        dao.insertBudget(
            BudgetEntity(
                category = "Entretenimiento",
                monthlyLimit = 120000.0,
                monthYear = getCurrentMonthYear()
            )
        )
        dao.insertBudget(
            BudgetEntity(
                category = "Servicios",
                monthlyLimit = 180000.0,
                monthYear = getCurrentMonthYear()
            )
        )

        // Savings Goals
        val compGoalId = dao.insertSavingsGoal(
            SavingsGoalEntity(
                title = "Comprar Computador Portátil",
                targetAmount = 3000000.0,
                savedAmount = 1200000.0,
                targetDate = now + (90 * oneDay),
                icon = "laptop",
                color = 0xFF00897B
            )
        )
        dao.insertSavingsTransaction(
            SavingsTransactionEntity(
                goalId = compGoalId,
                amount = 1200000.0,
                date = now - (15 * oneDay),
                note = "Aporte inicial con prima"
            )
        )

        dao.insertSavingsGoal(
            SavingsGoalEntity(
                title = "Fondo de Emergencia",
                targetAmount = 2500000.0,
                savedAmount = 850000.0,
                targetDate = now + (180 * oneDay),
                icon = "shield",
                color = 0xFF2E7D32
            )
        )

        // Future Expenses
        dao.insertFutureExpense(
            FutureExpenseEntity(
                title = "Internet Fibra Óptica",
                amount = 95000.0,
                dueDate = now + (4 * oneDay),
                category = "Servicios",
                priority = "ALTA",
                repeat = "Mensual",
                status = "PENDIENTE"
            )
        )
        dao.insertFutureExpense(
            FutureExpenseEntity(
                title = "Mantenimiento Preventivo Moto",
                amount = 130000.0,
                dueDate = now + (12 * oneDay),
                category = "Transporte",
                priority = "MEDIA",
                repeat = "Único",
                status = "PENDIENTE"
            )
        )
        dao.insertFutureExpense(
            FutureExpenseEntity(
                title = "Matrícula Curso Certificación",
                amount = 450000.0,
                dueDate = now + (25 * oneDay),
                category = "Educación",
                priority = "ALTA",
                repeat = "Único",
                status = "PENDIENTE"
            )
        )

        // Debts
        val debtId = dao.insertDebt(
            DebtEntity(
                title = "Crédito Computador",
                creditor = "Banco Davivienda",
                originalAmount = 2000000.0,
                remainingBalance = 1200000.0,
                installmentAmount = 150000.0,
                dueDate = now + (8 * oneDay),
                remainingInstallments = 8,
                interestRate = 1.8,
                frequency = "Mensual"
            )
        )
        dao.insertDebtPayment(
            DebtPaymentEntity(
                debtId = debtId,
                amount = 150000.0,
                date = now - (22 * oneDay),
                note = "Pago cuota mes anterior"
            )
        )
    }

    companion object {
        fun getCurrentMonthYear(): String {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            return String.format(Locale.US, "%04d-%02d", year, month)
        }

        fun formatCurrency(amount: Double, currency: String = "COP"): String {
            val format = NumberFormat.getIntegerInstance(Locale("es", "CO"))
            val formatted = format.format(amount.toLong())
            return when (currency) {
                "USD" -> "$$formatted USD"
                "EUR" -> "€$formatted"
                "MXN" -> "$$formatted MXN"
                else -> "$ $formatted"
            }
        }
    }
}
