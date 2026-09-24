package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.data.*
import com.example.ui.components.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

data class CategoryExpenseSummary(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val iconName: String,
    val colorHex: Long
)

data class BudgetStatus(
    val budget: BudgetEntity,
    val spent: Double,
    val remaining: Double,
    val percentage: Float,
    val isNearLimit: Boolean, // >= 80%
    val isExceeded: Boolean // > 100%
)

data class SavingsGoalDetail(
    val goal: SavingsGoalEntity,
    val remainingAmount: Double,
    val progressPercent: Float,
    val weeklyNeeded: Double,
    val monthlyNeeded: Double,
    val daysRemaining: Long
)

data class PeriodComparison(
    val currentPeriodLabel: String,
    val previousPeriodLabel: String,
    val currentIncome: Double,
    val previousIncome: Double,
    val incomeChangePercent: Double,
    val currentExpense: Double,
    val previousExpense: Double,
    val expenseChangePercent: Double,
    val currentSavings: Double,
    val previousSavings: Double,
    val savingsChangePercent: Double
)

data class InsightItem(
    val title: String,
    val description: String,
    val sourceData: String,
    val type: String // "ALERT", "POSITIVE", "INFO"
)

enum class ReportPeriod(val label: String) {
    DIA("Día"),
    SEMANA("Semana"),
    MES("Mes"),
    TRIMESTRE("Trimestre"),
    SEMESTRE("Semestre"),
    ANO("Año")
}

data class FinanceUiState(
    val userSettings: UserSettingsEntity = UserSettingsEntity(),
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val savingsGoals: List<SavingsGoalEntity> = emptyList(),
    val savingsTransactions: List<SavingsTransactionEntity> = emptyList(),
    val futureExpenses: List<FutureExpenseEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val debtPayments: List<DebtPaymentEntity> = emptyList(),
    val creditCardReminders: List<CreditCardReminderEntity> = emptyList(),

    // Computed Dashboard Metrics
    val availableBalance: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val monthSavings: Double = 0.0,
    val totalPendingDebts: Double = 0.0,

    // Today metrics
    val todaySpent: Double = 0.0,
    val todayIncome: Double = 0.0,
    val todayTransactionsCount: Int = 0,
    val todayAntExpenses: Double = 0.0,

    // Ant Expenses summary
    val antExpensesToday: Double = 0.0,
    val antExpensesWeek: Double = 0.0,
    val antExpensesMonth: Double = 0.0,
    val antExpensesYear: Double = 0.0,
    val antDailyProjectedWeekly: Double = 0.0,
    val antDailyProjectedMonthly: Double = 0.0,

    // Budget statuses
    val budgetStatuses: List<BudgetStatus> = emptyList(),

    // Savings Goals details
    val savingsGoalDetails: List<SavingsGoalDetail> = emptyList(),

    // Category distribution for current month
    val categoryExpenses: List<CategoryExpenseSummary> = emptyList(),

    // Future expenses projections
    val futureExpenses7Days: List<FutureExpenseEntity> = emptyList(),
    val futureExpenses30Days: List<FutureExpenseEntity> = emptyList(),
    val futureExpenses3Months: List<FutureExpenseEntity> = emptyList(),
    val futureExpenses30DaysTotal: Double = 0.0,

    // Selected report period
    val selectedReportPeriod: ReportPeriod = ReportPeriod.MES,
    val reportIncome: Double = 0.0,
    val reportExpense: Double = 0.0,
    val reportSavings: Double = 0.0,
    val reportDebtsPaid: Double = 0.0,
    val reportAntExpenses: Double = 0.0,
    val reportBalance: Double = 0.0,
    val reportCategoryExpenses: List<CategoryExpenseSummary> = emptyList(),
    val periodComparison: PeriodComparison? = null,

    // Insights
    val insights: List<InsightItem> = emptyList(),

    // Authentication & Session State
    val isLoggedIn: Boolean = false,
    val isUnlocked: Boolean = true,

    // Toast / Feedback message
    val snackbarMessage: String? = null
)

class FinanceViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _selectedReportPeriod = MutableStateFlow(ReportPeriod.MES)
    private val _isLoggedIn = MutableStateFlow(false)
    private val _isUnlocked = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.allTransactions,
        repository.allCategories,
        repository.allBudgets,
        repository.allSavingsGoals,
        repository.allSavingsTransactions,
        repository.allFutureExpenses,
        repository.allDebts,
        repository.allDebtPayments,
        repository.allCreditCardReminders,
        repository.userSettings,
        _selectedReportPeriod,
        _isLoggedIn,
        _isUnlocked,
        _snackbarMessage
    ) { args ->
        val transactions = args[0] as List<TransactionEntity>
        val categories = args[1] as List<CategoryEntity>
        val budgets = args[2] as List<BudgetEntity>
        val savingsGoals = args[3] as List<SavingsGoalEntity>
        val savingsTransactions = args[4] as List<SavingsTransactionEntity>
        val futureExpenses = args[5] as List<FutureExpenseEntity>
        val debts = args[6] as List<DebtEntity>
        val debtPayments = args[7] as List<DebtPaymentEntity>
        val creditCards = args[8] as List<CreditCardReminderEntity>
        val userSettings = (args[9] as? UserSettingsEntity) ?: UserSettingsEntity()
        val reportPeriod = args[10] as ReportPeriod
        val isLoggedIn = args[11] as Boolean
        val isUnlocked = args[12] as Boolean
        val snackbarMessage = args[13] as? String

        calculateUiState(
            transactions, categories, budgets, savingsGoals,
            savingsTransactions, futureExpenses, debts, debtPayments,
            creditCards,
            userSettings, reportPeriod, isLoggedIn, isUnlocked, snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanceUiState()
    )

    init {
        // Unlock immediately if PIN is not enabled
        viewModelScope.launch {
            val settings = repository.getUserSettingsDirect()
            if (settings != null && (settings.userName == "Sofía García" || settings.userName.isBlank())) {
                repository.saveUserSettings(
                    settings.copy(
                        userName = "Usuario",
                        userEmail = "usuario@correo.com",
                        userHandle = "usuario",
                        hasCompletedOnboarding = false
                    )
                )
            }
            if (settings == null || !settings.isPinEnabled || settings.pinCode.isEmpty()) {
                _isUnlocked.value = true
            }
        }

        // Ensure default categories are populated
        viewModelScope.launch {
            val existingCategories = repository.allCategories.first()
            if (existingCategories.isEmpty()) {
                val defaultCategories = listOf(
                    // Gastos
                    CategoryEntity(name = "Alimentación", iconName = "restaurant", colorHex = 0xFFF57C00, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                    CategoryEntity(name = "Transporte", iconName = "directions_bus", colorHex = 0xFF1976D2, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                    CategoryEntity(name = "Vivienda", iconName = "home", colorHex = 0xFF00897B, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                    CategoryEntity(name = "Servicios", iconName = "electrical_services", colorHex = 0xFFFF8F00, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                    CategoryEntity(name = "Educación", iconName = "school", colorHex = 0xFF7B1FA2, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                    CategoryEntity(name = "Salud", iconName = "local_hospital", colorHex = 0xFFE53935, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                    CategoryEntity(name = "Entretenimiento", iconName = "sports_esports", colorHex = 0xFF5E35B1, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                    CategoryEntity(name = "Tecnología", iconName = "memory", colorHex = 0xFF0288D1, isActive = true, isDefault = true, categoryType = "EXPENSE"),
                    CategoryEntity(name = "Ropa", iconName = "checkroom", colorHex = 0xFFD81B60, isActive = true, isDefault = true, categoryType = "EXPENSE"),
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
                repository.insertCategories(defaultCategories)
            } else {
                // Ensure default income categories exist if missing
                val hasIncomeCategory = existingCategories.any { it.categoryType == "INCOME" || it.name in listOf("Salario", "Honorarios", "Negocio", "Inversiones", "Regalos") }
                if (!hasIncomeCategory) {
                    val defaultIncome = listOf(
                        CategoryEntity(name = "Salario", iconName = "attach_money", colorHex = 0xFF10B981, isActive = true, isDefault = true, categoryType = "INCOME"),
                        CategoryEntity(name = "Honorarios / Freelance", iconName = "receipt", colorHex = 0xFF0D9488, isActive = true, isDefault = true, categoryType = "INCOME"),
                        CategoryEntity(name = "Negocio / Ventas", iconName = "store", colorHex = 0xFF3B82F6, isActive = true, isDefault = true, categoryType = "INCOME"),
                        CategoryEntity(name = "Inversiones", iconName = "trending_up", colorHex = 0xFF8B5CF6, isActive = true, isDefault = true, categoryType = "INCOME"),
                        CategoryEntity(name = "Regalos", iconName = "card_giftcard", colorHex = 0xFFEC4899, isActive = true, isDefault = true, categoryType = "INCOME"),
                        CategoryEntity(name = "Otros ingresos", iconName = "payments", colorHex = 0xFF64748B, isActive = true, isDefault = true, categoryType = "INCOME")
                    )
                    repository.insertCategories(defaultIncome)
                }
                // Also update "Salario / Ingresos" to INCOME if it exists
                val salarioIngreso = existingCategories.find { it.name == "Salario / Ingresos" && it.categoryType != "INCOME" }
                if (salarioIngreso != null) {
                    repository.updateCategory(salarioIngreso.copy(categoryType = "INCOME"))
                }
            }
        }

        // Eliminar elementos no agregados por el usuario excepto las categorías
        viewModelScope.launch {
            // Eliminar tarjeta predeterminada generada automáticamente
            val existingCards = repository.allCreditCardReminders.first()
            existingCards.forEach { card ->
                if (card.cardName == "Visa Clásica" && card.bankName == "Bancolombia") {
                    repository.deleteCreditCardReminder(card)
                }
            }
            // Eliminar deudas demo si existieran
            val existingDebts = repository.allDebts.first()
            existingDebts.forEach { debt ->
                if (debt.title == "Crédito Computador" && debt.creditor == "Banco Davivienda") {
                    repository.deleteDebt(debt)
                }
            }
            // Eliminar metas de ahorro demo si existieran
            val existingSavings = repository.allSavingsGoals.first()
            existingSavings.forEach { goal ->
                if (goal.title in listOf("Comprar Computador Portátil", "Fondo de Emergencia")) {
                    repository.deleteSavingsGoal(goal)
                }
            }
            // Eliminar gastos futuros demo si existieran
            val existingFuture = repository.allFutureExpenses.first()
            existingFuture.forEach { fe ->
                if (fe.title in listOf("Internet Fibra Óptica", "Mantenimiento Preventivo Moto", "Matrícula Curso Certificación")) {
                    repository.deleteFutureExpense(fe)
                }
            }
            // Eliminar transacciones demo si existieran
            val existingTxs = repository.allTransactions.first()
            val demoTitles = setOf(
                "Salario Mensual", "Proyecto Freelance", "Venta artículos usados",
                "Supermercado Éxito", "Arriendo Apartamento", "Servicios Públicos (Luz y Gas)",
                "Gasolina Moto", "Almuerzo Restaurante", "Café Latte y galleta",
                "Snack papitas", "Transporte adicional (Pasaje)", "Agua embotellada"
            )
            existingTxs.forEach { tx ->
                if (tx.title in demoTitles) {
                    repository.deleteTransaction(tx)
                }
            }
        }
    }

    private fun calculateUiState(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        budgets: List<BudgetEntity>,
        savingsGoals: List<SavingsGoalEntity>,
        savingsTransactions: List<SavingsTransactionEntity>,
        futureExpenses: List<FutureExpenseEntity>,
        debts: List<DebtEntity>,
        debtPayments: List<DebtPaymentEntity>,
        creditCards: List<CreditCardReminderEntity>,
        userSettings: UserSettingsEntity,
        reportPeriod: ReportPeriod,
        isLoggedIn: Boolean,
        isUnlocked: Boolean,
        snackbarMessage: String?
    ): FinanceUiState {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // Today start/end
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        val todayEnd = todayStart + (24L * 3600 * 1000)

        // Week start (Monday)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val weekStart = calendar.timeInMillis

        // Month start
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = calendar.timeInMillis

        // Year start
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        val yearStart = calendar.timeInMillis

        // Month transactions
        val monthTransactions = transactions.filter { it.date >= monthStart }
        val monthIncome = monthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val monthExpense = monthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

        // Month savings (deposits - withdrawals)
        val monthSavings = savingsTransactions.filter { it.date >= monthStart }.sumOf { it.amount }

        // Total pending debts and monthly debt obligations
        val totalPendingDebts = debts.sumOf { it.remainingBalance }
        val monthlyDebtInstallments = debts.sumOf { if (it.remainingBalance > 0) it.installmentAmount else 0.0 }

        // Saldo disponible = Ingresos del mes - Gastos del mes - cuotas de deudas pendientes del mes
        val availableBalance = monthIncome - monthExpense - monthlyDebtInstallments

        // Today metrics
        val todayTransactions = transactions.filter { it.date in todayStart until todayEnd }
        val todaySpent = todayTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val todayIncome = todayTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val todayAntExpenses = todayTransactions.filter { it.type == "EXPENSE" && it.isAntExpense }.sumOf { it.amount }

        // Ant expenses time windows
        val antToday = transactions.filter { it.isAntExpense && it.date in todayStart until todayEnd }.sumOf { it.amount }
        val antWeek = transactions.filter { it.isAntExpense && it.date >= weekStart }.sumOf { it.amount }
        val antMonth = transactions.filter { it.isAntExpense && it.date >= monthStart }.sumOf { it.amount }
        val antYear = transactions.filter { it.isAntExpense && it.date >= yearStart }.sumOf { it.amount }

        // Automatic projections for ant expenses
        val antDailyProjectedWeekly = antToday * 7.0
        val antDailyProjectedMonthly = antToday * 30.0

        // Budgets status
        val currentMonthYear = FinanceRepository.getCurrentMonthYear()
        val budgetStatuses = budgets.map { budget ->
            val spentInCategory = monthTransactions
                .filter { it.type == "EXPENSE" && it.category.equals(budget.category, ignoreCase = true) }
                .sumOf { it.amount }
            val remaining = (budget.monthlyLimit - spentInCategory).coerceAtLeast(0.0)
            val percent = if (budget.monthlyLimit > 0) ((spentInCategory / budget.monthlyLimit) * 100).toFloat() else 0f
            BudgetStatus(
                budget = budget,
                spent = spentInCategory,
                remaining = remaining,
                percentage = percent,
                isNearLimit = percent in 80.0f..100.0f,
                isExceeded = percent > 100.0f
            )
        }

        // Category breakdown for current month
        val totalMonthExpense = if (monthExpense > 0) monthExpense else 1.0
        val categoryExpenses = monthTransactions
            .filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .map { (catName, items) ->
                val sum = items.sumOf { it.amount }
                val catObj = categories.find { it.name.equals(catName, ignoreCase = true) }
                CategoryExpenseSummary(
                    category = catName,
                    amount = sum,
                    percentage = ((sum / totalMonthExpense) * 100).toFloat(),
                    iconName = catObj?.iconName ?: items.firstOrNull()?.categoryIcon ?: "category",
                    colorHex = catObj?.colorHex ?: items.firstOrNull()?.categoryColor ?: 0xFF4CAF50
                )
            }.sortedByDescending { it.amount }

        // Savings goal details
        val savingsGoalDetails = savingsGoals.map { goal ->
            val remaining = (goal.targetAmount - goal.savedAmount).coerceAtLeast(0.0)
            val progress = if (goal.targetAmount > 0) ((goal.savedAmount / goal.targetAmount) * 100).toFloat() else 0f
            val daysDiff = ((goal.targetDate - now) / (24L * 3600 * 1000)).coerceAtLeast(1)
            val weeksDiff = (daysDiff / 7.0).coerceAtLeast(1.0)
            val monthsDiff = (daysDiff / 30.0).coerceAtLeast(1.0)

            SavingsGoalDetail(
                goal = goal,
                remainingAmount = remaining,
                progressPercent = progress.coerceAtMost(100f),
                weeklyNeeded = remaining / weeksDiff,
                monthlyNeeded = remaining / monthsDiff,
                daysRemaining = daysDiff
            )
        }

        // Future expenses projections
        val oneDay = 24L * 3600 * 1000
        val pendingFuture = futureExpenses.filter { it.status == "PENDIENTE" }
        val future7 = pendingFuture.filter { it.dueDate in now..(now + 7 * oneDay) }
        val future30 = pendingFuture.filter { it.dueDate in now..(now + 30 * oneDay) }
        val future3Months = pendingFuture.filter { it.dueDate in now..(now + 90 * oneDay) }
        val future30DaysTotal = future30.sumOf { it.amount }

        // Reports calculations based on selectedReportPeriod
        val (periodStart, prevPeriodStart, prevPeriodEnd, curLabel, prevLabel) = getPeriodBounds(reportPeriod, now)

        val reportTxs = transactions.filter { it.date >= periodStart }
        val reportIncome = reportTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
        val reportExpense = reportTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val reportSavings = savingsTransactions.filter { it.date >= periodStart }.sumOf { it.amount }
        val reportDebtsPaid = debtPayments.filter { it.date >= periodStart }.sumOf { it.amount }
        val reportAnt = reportTxs.filter { it.type == "EXPENSE" && it.isAntExpense }.sumOf { it.amount }
        val reportBalance = reportIncome - reportExpense

        val prevReportTxs = transactions.filter { it.date in prevPeriodStart until prevPeriodEnd }
        val prevIncome = prevReportTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
        val prevExpense = prevReportTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val prevSavings = savingsTransactions.filter { it.date in prevPeriodStart until prevPeriodEnd }.sumOf { it.amount }

        val incomeDiff = calcPercentDiff(reportIncome, prevIncome)
        val expenseDiff = calcPercentDiff(reportExpense, prevExpense)
        val savingsDiff = calcPercentDiff(reportSavings, prevSavings)

        val periodComparison = PeriodComparison(
            currentPeriodLabel = curLabel,
            previousPeriodLabel = prevLabel,
            currentIncome = reportIncome,
            previousIncome = prevIncome,
            incomeChangePercent = incomeDiff,
            currentExpense = reportExpense,
            previousExpense = prevExpense,
            expenseChangePercent = expenseDiff,
            currentSavings = reportSavings,
            previousSavings = prevSavings,
            savingsChangePercent = savingsDiff
        )

        val totalRepExpense = if (reportExpense > 0) reportExpense else 1.0
        val reportCatExpenses = reportTxs
            .filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .map { (catName, items) ->
                val sum = items.sumOf { it.amount }
                val catObj = categories.find { it.name.equals(catName, ignoreCase = true) }
                CategoryExpenseSummary(
                    category = catName,
                    amount = sum,
                    percentage = ((sum / totalRepExpense) * 100).toFloat(),
                    iconName = catObj?.iconName ?: items.firstOrNull()?.categoryIcon ?: "category",
                    colorHex = catObj?.colorHex ?: items.firstOrNull()?.categoryColor ?: 0xFF4CAF50
                )
            }.sortedByDescending { it.amount }

        // Generate factual, data-driven financial insights
        val insights = mutableListOf<InsightItem>()

        if (antMonth > 0) {
            insights.add(
                InsightItem(
                    title = "Gastos hormiga del mes",
                    description = "Este mes has acumulado ${FinanceRepository.formatCurrency(antMonth, userSettings.currency)} en pequeños gastos diarios.",
                    sourceData = "Cálculo basado en transacciones marcadas como gasto hormiga desde el 1 del mes.",
                    type = "INFO"
                )
            )
        }

        categoryExpenses.firstOrNull()?.let { topCat ->
            insights.add(
                InsightItem(
                    title = "Mayor categoría de gasto",
                    description = "${topCat.category} representa el ${topCat.percentage.roundToInt()}% de tus gastos de este mes (${FinanceRepository.formatCurrency(topCat.amount, userSettings.currency)}).",
                    sourceData = "Cálculo basado en suma de gastos de ${topCat.category} sobre el gasto total del mes.",
                    type = "INFO"
                )
            )
        }

        if (expenseDiff != 0.0) {
            val sign = if (expenseDiff > 0) "+" else ""
            val type = if (expenseDiff > 0) "ALERT" else "POSITIVE"
            insights.add(
                InsightItem(
                    title = "Variación de gasto vs período anterior",
                    description = "En este período tus gastos variaron un $sign${expenseDiff.roundToInt()}% comparado con $prevLabel.",
                    sourceData = "Comparación entre gasto actual (${FinanceRepository.formatCurrency(reportExpense, userSettings.currency)}) y anterior (${FinanceRepository.formatCurrency(prevExpense, userSettings.currency)}).",
                    type = type
                )
            )
        }

        savingsGoalDetails.firstOrNull()?.let { topGoal ->
            insights.add(
                InsightItem(
                    title = "Objetivo de ahorro: ${topGoal.goal.title}",
                    description = "Llevas un avance del ${topGoal.progressPercent.roundToInt()}% (${FinanceRepository.formatCurrency(topGoal.goal.savedAmount, userSettings.currency)} de ${FinanceRepository.formatCurrency(topGoal.goal.targetAmount, userSettings.currency)}).",
                    sourceData = "Basado en tu meta '${topGoal.goal.title}' con fecha límite en ${topGoal.daysRemaining} días.",
                    type = "POSITIVE"
                )
            )
        }

        if (future30DaysTotal > 0) {
            insights.add(
                InsightItem(
                    title = "Compromisos próximos 30 días",
                    description = "En los próximos 30 días tienes programados ${FinanceRepository.formatCurrency(future30DaysTotal, userSettings.currency)} en gastos futuros.",
                    sourceData = "Suma de gastos futuros pendientes en la ventana de los próximos 30 días.",
                    type = "ALERT"
                )
            )
        }

        return FinanceUiState(
            userSettings = userSettings,
            transactions = transactions,
            categories = categories,
            budgets = budgets,
            savingsGoals = savingsGoals,
            savingsTransactions = savingsTransactions,
            futureExpenses = futureExpenses,
            debts = debts,
            debtPayments = debtPayments,
            creditCardReminders = creditCards,
            availableBalance = availableBalance,
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            monthSavings = monthSavings,
            totalPendingDebts = totalPendingDebts,
            todaySpent = todaySpent,
            todayIncome = todayIncome,
            todayTransactionsCount = todayTransactions.size,
            todayAntExpenses = todayAntExpenses,
            antExpensesToday = antToday,
            antExpensesWeek = antWeek,
            antExpensesMonth = antMonth,
            antExpensesYear = antYear,
            antDailyProjectedWeekly = antDailyProjectedWeekly,
            antDailyProjectedMonthly = antDailyProjectedMonthly,
            budgetStatuses = budgetStatuses,
            savingsGoalDetails = savingsGoalDetails,
            categoryExpenses = categoryExpenses,
            futureExpenses7Days = future7,
            futureExpenses30Days = future30,
            futureExpenses3Months = future3Months,
            futureExpenses30DaysTotal = future30DaysTotal,
            selectedReportPeriod = reportPeriod,
            reportIncome = reportIncome,
            reportExpense = reportExpense,
            reportSavings = reportSavings,
            reportDebtsPaid = reportDebtsPaid,
            reportAntExpenses = reportAnt,
            reportBalance = reportBalance,
            reportCategoryExpenses = reportCatExpenses,
            periodComparison = periodComparison,
            insights = insights,
            isLoggedIn = isLoggedIn,
            isUnlocked = isUnlocked,
            snackbarMessage = snackbarMessage
        )
    }

    private fun getPeriodBounds(period: ReportPeriod, now: Long): LongBounds {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        val currentPeriodStart: Long
        val prevPeriodStart: Long
        val prevPeriodEnd: Long
        val curLabel: String
        val prevLabel: String

        when (period) {
            ReportPeriod.DIA -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                currentPeriodStart = cal.timeInMillis
                prevPeriodEnd = currentPeriodStart
                prevPeriodStart = currentPeriodStart - (24L * 3600 * 1000)
                curLabel = "Hoy"
                prevLabel = "Ayer"
            }
            ReportPeriod.SEMANA -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                currentPeriodStart = cal.timeInMillis
                prevPeriodEnd = currentPeriodStart
                prevPeriodStart = currentPeriodStart - (7L * 24 * 3600 * 1000)
                curLabel = "Esta semana"
                prevLabel = "Semana anterior"
            }
            ReportPeriod.MES -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                currentPeriodStart = cal.timeInMillis
                prevPeriodEnd = currentPeriodStart
                cal.add(Calendar.MONTH, -1)
                prevPeriodStart = cal.timeInMillis
                curLabel = "Este mes"
                prevLabel = "Mes anterior"
            }
            ReportPeriod.TRIMESTRE -> {
                val month = cal.get(Calendar.MONTH)
                val quarterStartMonth = (month / 3) * 3
                cal.set(Calendar.MONTH, quarterStartMonth)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                currentPeriodStart = cal.timeInMillis
                prevPeriodEnd = currentPeriodStart
                cal.add(Calendar.MONTH, -3)
                prevPeriodStart = cal.timeInMillis
                curLabel = "Este trimestre"
                prevLabel = "Trimestre anterior"
            }
            ReportPeriod.SEMESTRE -> {
                val month = cal.get(Calendar.MONTH)
                val semesterStartMonth = if (month < 6) 0 else 6
                cal.set(Calendar.MONTH, semesterStartMonth)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                currentPeriodStart = cal.timeInMillis
                prevPeriodEnd = currentPeriodStart
                cal.add(Calendar.MONTH, -6)
                prevPeriodStart = cal.timeInMillis
                curLabel = "Este semestre"
                prevLabel = "Semestre anterior"
            }
            ReportPeriod.ANO -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                currentPeriodStart = cal.timeInMillis
                prevPeriodEnd = currentPeriodStart
                cal.add(Calendar.YEAR, -1)
                prevPeriodStart = cal.timeInMillis
                curLabel = "Este año"
                prevLabel = "Año anterior"
            }
        }
        return LongBounds(currentPeriodStart, prevPeriodStart, prevPeriodEnd, curLabel, prevLabel)
    }

    private fun calcPercentDiff(current: Double, previous: Double): Double {
        if (previous <= 0.0) return 0.0
        return ((current - previous) / previous) * 100.0
    }

    private data class LongBounds(
        val currentStart: Long,
        val previousStart: Long,
        val previousEnd: Long,
        val currentLabel: String,
        val previousLabel: String
    )

    fun selectReportPeriod(period: ReportPeriod) {
        _selectedReportPeriod.value = period
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Quick Add Expense
    fun addExpense(
        amount: Double,
        category: String,
        title: String = "",
        date: Long = System.currentTimeMillis(),
        isAntExpense: Boolean = false,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val catObj = uiState.value.categories.find { it.name.equals(category, ignoreCase = true) }
            val itemTitle = if (title.isNotBlank()) title else category
            repository.insertTransaction(
                TransactionEntity(
                    type = "EXPENSE",
                    title = itemTitle,
                    amount = amount,
                    category = category,
                    categoryIcon = catObj?.iconName ?: "restaurant",
                    categoryColor = catObj?.colorHex ?: 0xFFF57C00,
                    date = date,
                    isAntExpense = isAntExpense,
                    notes = notes
                )
            )
            showSnackbar("Gasto de ${FinanceRepository.formatCurrency(amount, uiState.value.userSettings.currency)} registrado.")
        }
    }

    // Quick Add Income
    fun addIncome(
        amount: Double,
        title: String,
        category: String = "Salario / Ingresos",
        date: Long = System.currentTimeMillis(),
        frequency: String = "Único",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val catObj = uiState.value.categories.find { it.name.equals(category, ignoreCase = true) }
            val itemTitle = if (title.isNotBlank()) title else "Ingreso"
            repository.insertTransaction(
                TransactionEntity(
                    type = "INCOME",
                    title = itemTitle,
                    amount = amount,
                    category = category,
                    categoryIcon = catObj?.iconName ?: "attach_money",
                    categoryColor = catObj?.colorHex ?: 0xFF1B5E20,
                    date = date,
                    frequency = frequency,
                    isAntExpense = false,
                    notes = notes
                )
            )
            showSnackbar("Ingreso de ${FinanceRepository.formatCurrency(amount, uiState.value.userSettings.currency)} registrado.")
        }
    }

    // Quick Add Savings Deposit / Withdrawal
    fun addSavingsMovement(
        goal: SavingsGoalEntity,
        amount: Double,
        isDeposit: Boolean,
        note: String = ""
    ) {
        viewModelScope.launch {
            val signedAmount = if (isDeposit) amount else -amount
            repository.addSavingsTransaction(goal, signedAmount, note)
            val action = if (isDeposit) "Aporte" else "Retiro"
            showSnackbar("$action de ${FinanceRepository.formatCurrency(amount, uiState.value.userSettings.currency)} a ${goal.title} guardado.")
        }
    }

    // Quick Add Future Expense
    fun addFutureExpense(
        title: String,
        amount: Double,
        dueDate: Long,
        category: String,
        priority: String = "MEDIA",
        repeat: String = "Único"
    ) {
        viewModelScope.launch {
            repository.insertFutureExpense(
                FutureExpenseEntity(
                    title = title,
                    amount = amount,
                    dueDate = dueDate,
                    category = category,
                    priority = priority,
                    repeat = repeat,
                    status = "PENDIENTE"
                )
            )
            showSnackbar("Gasto futuro programado para ${formatDate(dueDate)}.")
        }
    }

    fun markFutureExpensePaid(expense: FutureExpenseEntity) {
        viewModelScope.launch {
            repository.markFutureExpensePaid(expense)
            showSnackbar("Gasto futuro '${expense.title}' marcado como pagado.")
        }
    }

    fun deleteFutureExpense(expense: FutureExpenseEntity) {
        viewModelScope.launch {
            repository.deleteFutureExpense(expense)
            showSnackbar("Gasto futuro eliminado.")
        }
    }

    // Quick Add Debt Payment
    fun recordDebtPayment(debt: DebtEntity, paymentAmount: Double, note: String = "") {
        viewModelScope.launch {
            repository.recordDebtPayment(debt, paymentAmount, note)
            showSnackbar("Pago de cuota de ${FinanceRepository.formatCurrency(paymentAmount, uiState.value.userSettings.currency)} registrado.")
        }
    }

    fun addDebt(
        title: String,
        creditor: String,
        originalAmount: Double,
        remainingBalance: Double,
        installmentAmount: Double,
        dueDate: Long,
        remainingInstallments: Int,
        interestRate: Double = 0.0,
        frequency: String = "Mensual"
    ) {
        viewModelScope.launch {
            repository.insertDebt(
                DebtEntity(
                    title = title,
                    creditor = creditor,
                    originalAmount = originalAmount,
                    remainingBalance = remainingBalance,
                    installmentAmount = installmentAmount,
                    dueDate = dueDate,
                    remainingInstallments = remainingInstallments,
                    interestRate = interestRate,
                    frequency = frequency
                )
            )
            showSnackbar("Deuda '${title}' registrada.")
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
            showSnackbar("Deuda eliminada.")
        }
    }

    fun addSavingsGoal(
        title: String,
        targetAmount: Double,
        savedAmount: Double,
        targetDate: Long,
        icon: String = "savings",
        color: Long = 0xFF00897B
    ) {
        viewModelScope.launch {
            val goalId = repository.insertSavingsGoal(
                SavingsGoalEntity(
                    title = title,
                    targetAmount = targetAmount,
                    savedAmount = savedAmount,
                    targetDate = targetDate,
                    icon = icon,
                    color = color
                )
            )
            if (savedAmount > 0) {
                repository.insertSavingsTransaction(
                    SavingsTransactionEntity(
                        goalId = goalId,
                        amount = savedAmount,
                        note = "Aporte inicial a $title"
                    )
                )
            }
            showSnackbar("Meta '$title' con objetivo de ${FinanceRepository.formatCurrency(targetAmount, uiState.value.userSettings.currency)} creada.")
        }
    }

    fun createGoalAndDeposit(
        title: String,
        targetAmount: Double,
        initialDeposit: Double,
        targetDate: Long,
        note: String = ""
    ) {
        viewModelScope.launch {
            val newGoal = SavingsGoalEntity(
                title = title,
                targetAmount = targetAmount,
                savedAmount = initialDeposit,
                targetDate = targetDate
            )
            val goalId = repository.insertSavingsGoal(newGoal)
            if (initialDeposit > 0) {
                repository.insertSavingsTransaction(
                    SavingsTransactionEntity(
                        goalId = goalId,
                        amount = initialDeposit,
                        note = if (note.isNotBlank()) note else "Aporte inicial a $title"
                    )
                )
            }
            showSnackbar("Meta '$title' creada con objetivo de ${FinanceRepository.formatCurrency(targetAmount, uiState.value.userSettings.currency)}.")
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
            showSnackbar("Objetivo de ahorro eliminado.")
        }
    }

    fun setBudget(category: String, monthlyLimit: Double) {
        viewModelScope.launch {
            val monthYear = FinanceRepository.getCurrentMonthYear()
            val existing = uiState.value.budgets.find { it.category.equals(category, ignoreCase = true) }
            if (existing != null) {
                repository.updateBudget(existing.copy(monthlyLimit = monthlyLimit, monthYear = monthYear))
            } else {
                repository.insertBudget(
                    BudgetEntity(
                        category = category,
                        monthlyLimit = monthlyLimit,
                        monthYear = monthYear
                    )
                )
            }
            showSnackbar("Presupuesto para $category actualizado a ${FinanceRepository.formatCurrency(monthlyLimit, uiState.value.userSettings.currency)}.")
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
            showSnackbar("Presupuesto eliminado.")
        }
    }

    fun addCategory(name: String, iconName: String, colorHex: Long, categoryType: String = "EXPENSE") {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name,
                    iconName = iconName,
                    colorHex = colorHex,
                    isActive = true,
                    isDefault = false,
                    categoryType = categoryType
                )
            )
            showSnackbar("Categoría '$name' creada.")
        }
    }

    fun updateCategory(category: CategoryEntity, oldName: String? = null) {
        viewModelScope.launch {
            repository.updateCategory(category)
            if (oldName != null && oldName != category.name) {
                repository.updateTransactionCategory(oldName, category.name)
            }
            showSnackbar("Categoría '${category.name}' actualizada.")
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            showSnackbar("Categoría eliminada.")
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            showSnackbar("Transacción actualizada.")
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            showSnackbar("Transacción eliminada.")
        }
    }

    fun updateUserSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.saveUserSettings(settings)
            showSnackbar("Configuración guardada.")
        }
    }

    fun logIn(identifier: String, pass: String): Boolean {
        val settings = uiState.value.userSettings
        val trimmedId = identifier.trim().lowercase()
        val email = settings.userEmail.trim().lowercase()
        val handle = settings.userHandle.trim().lowercase().removePrefix("@")
        val name = settings.userName.trim().lowercase()
        val expectedPass = if (settings.password.isNotEmpty()) settings.password else settings.pinCode

        val idMatches = trimmedId == email ||
                trimmedId == handle ||
                trimmedId == "@$handle" ||
                trimmedId == name ||
                (email == "usuario@correo.com" && trimmedId.isNotBlank())

        val passMatches = if (expectedPass.isNotEmpty()) {
            pass == expectedPass
        } else {
            // If no password had been set previously in DB, accept non-empty password and store it
            pass.isNotEmpty()
        }

        if (idMatches && passMatches) {
            if (settings.password.isEmpty()) {
                viewModelScope.launch {
                    repository.saveUserSettings(
                        settings.copy(
                            password = pass,
                            pinCode = if (settings.pinCode.isEmpty()) pass else settings.pinCode,
                            isAccountCreated = true
                        )
                    )
                }
            }
            _isLoggedIn.value = true
            _isUnlocked.value = true
            showSnackbar("¡Bienvenido, ${settings.userName}!")
            return true
        }
        return false
    }

    fun registerAccount(
        name: String,
        email: String,
        handle: String,
        currency: String,
        pass: String,
        pin: String,
        initialBalance: Double
    ) {
        viewModelScope.launch {
            val updated = uiState.value.userSettings.copy(
                userName = name.ifBlank { "Usuario" },
                userEmail = email.ifBlank { "usuario@correo.com" },
                userHandle = handle.ifBlank { "usuario" }.removePrefix("@"),
                currency = currency,
                password = pass,
                pinCode = if (pin.isNotEmpty()) pin else pass,
                isPinEnabled = pin.isNotEmpty(),
                hasCompletedOnboarding = true,
                isAccountCreated = true
            )
            repository.saveUserSettings(updated)
            if (initialBalance > 0) {
                repository.insertTransaction(
                    TransactionEntity(
                        title = "Saldo inicial de registro",
                        amount = initialBalance,
                        category = "Salario / Ingresos",
                        type = "INCOME",
                        notes = "Registro de cuenta Finara"
                    )
                )
            }
            _isLoggedIn.value = true
            _isUnlocked.value = true
            showSnackbar("¡Cuenta creada con éxito! Bienvenido a Finara, ${name.ifBlank { "Usuario" }}")
        }
    }

    fun resetPassword(emailOrHandle: String, newPass: String): Boolean {
        val settings = uiState.value.userSettings
        val target = emailOrHandle.trim().lowercase().removePrefix("@")
        val curEmail = settings.userEmail.trim().lowercase()
        val curHandle = settings.userHandle.trim().lowercase().removePrefix("@")
        if (target == curEmail || target == curHandle || curEmail == "usuario@correo.com" || curEmail.isBlank()) {
            viewModelScope.launch {
                repository.saveUserSettings(
                    settings.copy(
                        password = newPass,
                        pinCode = if (settings.isPinEnabled) settings.pinCode else newPass,
                        isAccountCreated = true
                    )
                )
                showSnackbar("Contraseña actualizada exitosamente.")
            }
            return true
        }
        return false
    }

    fun logOut() {
        _isLoggedIn.value = false
        _isUnlocked.value = false
        showSnackbar("Sesión cerrada.")
    }

    fun unlockWithPin(pin: String): Boolean {
        val userPin = uiState.value.userSettings.pinCode
        return if (pin == userPin) {
            _isUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lockApp() {
        if (uiState.value.userSettings.isPinEnabled && uiState.value.userSettings.pinCode.isNotEmpty()) {
            _isUnlocked.value = false
        }
    }

    // Credit Card Reminders & Fee Management
    fun addCreditCardReminder(card: CreditCardReminderEntity, context: Context? = null) {
        viewModelScope.launch {
            repository.insertCreditCardReminder(card)
            context?.let { ctx ->
                NotificationHelper.scheduleAlarms(ctx, uiState.value.creditCardReminders + card)
            }
            showSnackbar("Tarjeta '${card.cardName}' agregada con recordatorios.")
        }
    }

    fun updateCreditCardReminder(card: CreditCardReminderEntity, context: Context? = null) {
        viewModelScope.launch {
            repository.updateCreditCardReminder(card)
            context?.let { ctx ->
                NotificationHelper.scheduleAlarms(ctx, uiState.value.creditCardReminders.map { if (it.id == card.id) card else it })
            }
            showSnackbar("Tarjeta '${card.cardName}' actualizada.")
        }
    }

    fun deleteCreditCardReminder(card: CreditCardReminderEntity) {
        viewModelScope.launch {
            repository.deleteCreditCardReminder(card)
            showSnackbar("Tarjeta eliminada.")
        }
    }

    fun payCreditCardFee(card: CreditCardReminderEntity) {
        viewModelScope.launch {
            repository.payCreditCardFee(card)
            showSnackbar("Pago de cuota de manejo para '${card.cardName}' registrado.")
        }
    }

    fun checkCardReminders(context: Context) {
        viewModelScope.launch {
            val cards = uiState.value.creditCardReminders
            NotificationHelper.checkAndNotifyCards(context, cards)
            NotificationHelper.scheduleAlarms(context, cards)
        }
    }

    fun testCardNotification(
        context: Context,
        card: CreditCardReminderEntity,
        isCutOff: Boolean,
        daysBefore: Int
    ) {
        NotificationHelper.triggerTestNotification(context, card, isCutOff, daysBefore)
        val typeStr = if (isCutOff) "corte" else "pago"
        val dayStr = when (daysBefore) {
            7 -> "7 días antes"
            3 -> "3 días antes"
            1 -> "el día anterior"
            else -> "el mismo día"
        }
        showSnackbar("Notificación de prueba enviada ($dayStr de $typeStr)")
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
            showSnackbar("Datos de demostración cargados exitosamente.")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearDemoData()
            showSnackbar("Todos los datos fueron borrados.")
        }
    }

    fun completeOnboarding(
        approxIncome: Double,
        approxExpense: Double,
        savingsTarget: Double,
        hasDebt: Boolean,
        hasFuture: Boolean
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val currency = uiState.value.userSettings.currency

            if (approxIncome > 0) {
                repository.insertTransaction(
                    TransactionEntity(
                        type = "INCOME",
                        title = "Ingreso Estimado",
                        amount = approxIncome,
                        category = "Salario / Ingresos",
                        date = now,
                        frequency = "Mensual"
                    )
                )
            }
            if (approxExpense > 0) {
                repository.insertTransaction(
                    TransactionEntity(
                        type = "EXPENSE",
                        title = "Gastos Generales",
                        amount = approxExpense,
                        category = "Alimentación",
                        date = now,
                        isAntExpense = false
                    )
                )
                repository.insertBudget(
                    BudgetEntity(
                        category = "Alimentación",
                        monthlyLimit = approxExpense * 1.2,
                        monthYear = FinanceRepository.getCurrentMonthYear()
                    )
                )
            }
            if (savingsTarget > 0) {
                repository.insertSavingsGoal(
                    SavingsGoalEntity(
                        title = "Mi Meta de Ahorro",
                        targetAmount = savingsTarget,
                        savedAmount = 0.0,
                        targetDate = now + (180L * 24 * 3600 * 1000)
                    )
                )
            }
            if (hasDebt) {
                repository.insertDebt(
                    DebtEntity(
                        title = "Préstamo / Crédito",
                        creditor = "Entidad Financiera",
                        originalAmount = 1500000.0,
                        remainingBalance = 1500000.0,
                        installmentAmount = 150000.0,
                        dueDate = now + (15L * 24 * 3600 * 1000),
                        remainingInstallments = 10
                    )
                )
            }
            if (hasFuture) {
                repository.insertFutureExpense(
                    FutureExpenseEntity(
                        title = "Pago Próximo Importante",
                        amount = 200000.0,
                        dueDate = now + (10L * 24 * 3600 * 1000),
                        category = "Servicios",
                        priority = "ALTA"
                    )
                )
            }

            repository.saveUserSettings(
                uiState.value.userSettings.copy(hasCompletedOnboarding = true)
            )
            showSnackbar("¡Configuración inicial completada!")
        }
    }

    companion object {
        fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es", "CO"))
            return sdf.format(Date(timestamp))
        }

        fun formatShortDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM", Locale("es", "CO"))
            return sdf.format(Date(timestamp))
        }

        fun generateCsvReport(state: FinanceUiState): String {
            val sb = StringBuilder()
            sb.append("ID,Tipo,Título,Monto,Categoría,Fecha,Frecuencia,Gasto Hormiga,Notas\n")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            state.transactions.forEach { tx ->
                val typeStr = if (tx.type == "INCOME") "Ingreso" else "Gasto"
                val antStr = if (tx.isAntExpense) "Sí" else "No"
                val dateStr = sdf.format(Date(tx.date))
                sb.append("${tx.id},\"$typeStr\",\"${tx.title}\",${tx.amount},\"${tx.category}\",\"$dateStr\",\"${tx.frequency}\",\"$antStr\",\"${tx.notes}\"\n")
            }
            return sb.toString()
        }

        fun generateTextReport(state: FinanceUiState): String {
            val currency = state.userSettings.currency
            val sb = StringBuilder()
            sb.append("═══════════════════════════════════\n")
            sb.append("   REPORTE FINANCIERO PERSONAL     \n")
            sb.append("═══════════════════════════════════\n\n")
            sb.append("Usuario: ${state.userSettings.userName}\n")
            sb.append("Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n\n")
            sb.append("RESUMEN DEL PERÍODO (${state.selectedReportPeriod.label}):\n")
            sb.append("• Ingresos: ${FinanceRepository.formatCurrency(state.reportIncome, currency)}\n")
            sb.append("• Gastos: ${FinanceRepository.formatCurrency(state.reportExpense, currency)}\n")
            sb.append("• Balance: ${FinanceRepository.formatCurrency(state.reportBalance, currency)}\n")
            sb.append("• Aportes a ahorro: ${FinanceRepository.formatCurrency(state.reportSavings, currency)}\n")
            sb.append("• Pagos a deuda: ${FinanceRepository.formatCurrency(state.reportDebtsPaid, currency)}\n")
            sb.append("• Gastos hormiga: ${FinanceRepository.formatCurrency(state.reportAntExpenses, currency)}\n\n")

            sb.append("ESTADO FINANCIERO ACTUAL:\n")
            sb.append("• Saldo disponible: ${FinanceRepository.formatCurrency(state.availableBalance, currency)}\n")
            sb.append("• Deudas pendientes: ${FinanceRepository.formatCurrency(state.totalPendingDebts, currency)}\n\n")

            if (state.categoryExpenses.isNotEmpty()) {
                sb.append("GASTOS POR CATEGORÍA:\n")
                state.categoryExpenses.forEach { cat ->
                    sb.append("• ${cat.category}: ${FinanceRepository.formatCurrency(cat.amount, currency)} (${cat.percentage.roundToInt()}%)\n")
                }
                sb.append("\n")
            }

            if (state.budgetStatuses.isNotEmpty()) {
                sb.append("ESTADO DE PRESUPUESTOS:\n")
                state.budgetStatuses.forEach { b ->
                    val status = if (b.isExceeded) "[EXCEDIDO]" else if (b.isNearLimit) "[ALERTA]" else "[OK]"
                    sb.append("• ${b.budget.category}: ${FinanceRepository.formatCurrency(b.spent, currency)} / ${FinanceRepository.formatCurrency(b.budget.monthlyLimit, currency)} $status\n")
                }
            }
            sb.append("\n═══════════════════════════════════\n")
            return sb.toString()
        }
    }
}

class FinanceViewModelFactory(
    private val repository: FinanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
