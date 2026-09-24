package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class AppDestination(val label: String, val icon: ImageVector) {
    INICIO("Inicio", Icons.Default.Home),
    MOVIMIENTOS("Movimientos", Icons.Default.ReceiptLong),
    DEUDAS("Deudas pendientes", Icons.Default.CreditCard),
    METAS_AHORRO("Metas de ahorro", Icons.Default.Savings),
    MAS("Más", Icons.Default.Menu)
}

enum class AuthMode {
    LOGIN,
    REGISTER
}

enum class MoreSubDestination {
    MENU,
    MY_PROFILE,
    EDIT_PROFILE,
    REGISTER,
    PREFERENCES,
    PRIVACY_SECURITY,
    NOTIFICATIONS,
    CATEGORIES_LIMITS,
    MY_DATA,
    HELP,
    APP_INFO,
    ANT_EXPENSES,
    SAVINGS,
    DEBTS,
    FUTURE_EXPENSES,
    CALENDAR,
    REPORTS,
    BUDGETS
}

enum class CreationScreenType {
    GASTO,
    INGRESO,
    AHORRO,
    GASTO_FUTURO,
    PAGO_DEUDA
}

@Composable
fun MainAppScreen(
    viewModel: FinanceViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar messages from ViewModel
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Mandatory Authentication & Session Gate
    // User must create an account or log in before accessing any feature in Finara
    val isAccountAlreadyConfigured = state.userSettings.isAccountCreated ||
            (state.userSettings.hasCompletedOnboarding && state.userSettings.userEmail.isNotBlank() && state.userSettings.userEmail != "usuario@correo.com")

    var authMode by remember(isAccountAlreadyConfigured) {
        mutableStateOf(if (isAccountAlreadyConfigured) AuthMode.LOGIN else AuthMode.REGISTER)
    }

    if (!state.isLoggedIn) {
        when (authMode) {
            AuthMode.LOGIN -> {
                LoginScreen(
                    state = state,
                    onLogin = { identifier, pass ->
                        viewModel.logIn(identifier, pass)
                    },
                    onNavigateToRegister = {
                        authMode = AuthMode.REGISTER
                    },
                    onResetPassword = { emailOrUser, newPass ->
                        viewModel.resetPassword(emailOrUser, newPass)
                    }
                )
            }
            AuthMode.REGISTER -> {
                RegisterScreen(
                    state = state,
                    onBack = if (isAccountAlreadyConfigured) {
                        { authMode = AuthMode.LOGIN }
                    } else null,
                    onNavigateToLogin = {
                        authMode = AuthMode.LOGIN
                    },
                    onRegisterComplete = { name, email, handle, curr, pass, pin, initialBal ->
                        viewModel.registerAccount(
                            name = name,
                            email = email,
                            handle = handle,
                            currency = curr,
                            pass = pass,
                            pin = pin,
                            initialBalance = initialBal
                        )
                    }
                )
            }
        }
        return
    }

    // Check if locked by PIN (only active while logged in)
    if (!state.isUnlocked && state.userSettings.isPinEnabled && state.userSettings.pinCode.isNotEmpty()) {
        PinLockScreen(
            onUnlock = { pin -> viewModel.unlockWithPin(pin) }
        )
        return
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.checkCardReminders(context)
    }

    var currentDestination by remember { mutableStateOf(AppDestination.INICIO) }
    var currentMoreSubDestination by remember { mutableStateOf(MoreSubDestination.MENU) }

    // State for Quick Actions Overlay (Dashboard acciones rápidas.png)
    var showQuickActions by remember { mutableStateOf(false) }
    var activeCreationScreen by remember { mutableStateOf<CreationScreenType?>(null) }
    var autoOpenAddDebtDialog by remember { mutableStateOf(false) }

    // Display Creation Screen as Full Screen overlay if active
    if (activeCreationScreen != null) {
        when (activeCreationScreen) {
            CreationScreenType.GASTO -> {
                NuevoGastoScreen(
                    state = state,
                    onBack = { activeCreationScreen = null },
                    onSave = { amount, category, date, paymentMethod, isAnt, notes ->
                        viewModel.addExpense(
                            amount = amount,
                            category = category,
                            title = category,
                            date = date,
                            isAntExpense = isAnt,
                            notes = notes
                        )
                    }
                )
            }
            CreationScreenType.INGRESO -> {
                NuevoIngresoScreen(
                    state = state,
                    onBack = { activeCreationScreen = null },
                    onSave = { amount, title, type, date, frequency, notes ->
                        viewModel.addIncome(
                            amount = amount,
                            title = title,
                            category = type,
                            date = date,
                            frequency = frequency,
                            notes = notes
                        )
                    }
                )
            }
            CreationScreenType.AHORRO -> {
                AgregarAhorroScreen(
                    state = state,
                    onBack = { activeCreationScreen = null },
                    onSave = { goalId, goalTitle, targetAmount, amount, date, note, targetDate ->
                        if (goalId > 0) {
                            val targetGoal = state.savingsGoals.find { it.id == goalId }
                            if (targetGoal != null) {
                                viewModel.addSavingsMovement(
                                    goal = targetGoal,
                                    amount = amount,
                                    isDeposit = true,
                                    note = note
                                )
                            }
                        } else {
                            viewModel.createGoalAndDeposit(
                                title = goalTitle,
                                targetAmount = if (targetAmount > 0) targetAmount else amount * 3,
                                initialDeposit = amount,
                                targetDate = targetDate,
                                note = note
                            )
                        }
                    }
                )
            }
            CreationScreenType.GASTO_FUTURO -> {
                NuevoGastoFuturoScreen(
                    state = state,
                    onBack = { activeCreationScreen = null },
                    onSave = { title, amount, dueDate, category, priority, repeat ->
                        viewModel.addFutureExpense(
                            title = title,
                            amount = amount,
                            dueDate = dueDate,
                            category = category,
                            priority = priority,
                            repeat = repeat
                        )
                    }
                )
            }
            CreationScreenType.PAGO_DEUDA -> {
                if (state.debts.isEmpty() || state.debts.all { it.remainingBalance <= 0 }) {
                    activeCreationScreen = null
                    currentDestination = AppDestination.DEUDAS
                    autoOpenAddDebtDialog = true
                    viewModel.showSnackbar("No tienes deudas registradas. Crea una deuda nueva para comenzar.")
                } else {
                    RegistrarPagoScreen(
                        state = state,
                        onBack = { activeCreationScreen = null },
                        onOpenCreateDebt = {
                            activeCreationScreen = null
                            currentDestination = AppDestination.DEUDAS
                            autoOpenAddDebtDialog = true
                        },
                        onSave = { debtId, debtTitle, amount, date, note ->
                            val debt = state.debts.find { it.id == debtId }
                                ?: DebtEntity(title = debtTitle, creditor = "Entidad", originalAmount = amount * 10, remainingBalance = amount * 10, installmentAmount = amount, dueDate = date, remainingInstallments = 10)
                            viewModel.recordDebtPayment(
                                debt = debt,
                                paymentAmount = amount,
                                note = note
                            )
                        }
                    )
                }
            }
            null -> {}
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                AppDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentDestination = destination
                            showQuickActions = false
                            if (destination == AppDestination.MAS) {
                                currentMoreSubDestination = MoreSubDestination.MENU
                            }
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.label,
                                tint = if (isSelected) ForestGreen else Color(0xFF94A3B8)
                            )
                        },
                        label = {
                            Text(
                                destination.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ForestGreen else Color(0xFF64748B)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MintLight,
                            selectedIconColor = ForestGreen,
                            selectedTextColor = ForestGreen,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentDestination == AppDestination.INICIO || currentDestination == AppDestination.MOVIMIENTOS) {
                FloatingActionButton(
                    onClick = { showQuickActions = !showQuickActions },
                    containerColor = ForestGreen,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("fab_quick_add")
                ) {
                    Icon(
                        if (showQuickActions) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (showQuickActions) "Cerrar acciones" else "Acciones rápidas",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentDestination) {
                AppDestination.INICIO -> {
                    DashboardScreen(
                        state = state,
                        onNavigateToProfile = {
                            currentDestination = AppDestination.MAS
                            currentMoreSubDestination = MoreSubDestination.MY_PROFILE
                        },
                        onNavigateToTransactions = { currentDestination = AppDestination.MOVIMIENTOS },
                        onNavigateToBudgets = {
                            currentDestination = AppDestination.MAS
                            currentMoreSubDestination = MoreSubDestination.BUDGETS
                        },
                        onNavigateToReports = {
                            currentDestination = AppDestination.MAS
                            currentMoreSubDestination = MoreSubDestination.REPORTS
                        },
                        onNavigateToAntExpenses = {
                            currentDestination = AppDestination.MAS
                            currentMoreSubDestination = MoreSubDestination.ANT_EXPENSES
                        },
                        onNavigateToSavings = {
                            currentDestination = AppDestination.METAS_AHORRO
                        },
                        onNavigateToDebts = {
                            currentDestination = AppDestination.DEUDAS
                        },
                        onNavigateToFutureExpenses = {
                            currentDestination = AppDestination.MAS
                            currentMoreSubDestination = MoreSubDestination.FUTURE_EXPENSES
                        },
                        onOpenQuickAdd = { showQuickActions = true }
                    )
                }
                AppDestination.MOVIMIENTOS -> {
                    TransactionsScreen(
                        state = state,
                        onDeleteTransaction = { viewModel.deleteTransaction(it) },
                        onUpdateTransaction = { viewModel.updateTransaction(it) },
                        onOpenQuickAdd = {
                            activeCreationScreen = CreationScreenType.GASTO
                        },
                        onAddExpense = {
                            activeCreationScreen = CreationScreenType.GASTO
                        },
                        onAddIncome = {
                            activeCreationScreen = CreationScreenType.INGRESO
                        }
                    )
                }
                AppDestination.DEUDAS -> {
                    DebtsScreen(
                        state = state,
                        onAddDebt = { title, creditor, orig, rem, inst, date, numInst ->
                            viewModel.addDebt(title, creditor, orig, rem, inst, date, numInst)
                        },
                        onRecordPayment = { debt, amt ->
                            viewModel.recordDebtPayment(debt, amt)
                        },
                        onDeleteDebt = { viewModel.deleteDebt(it) },
                        onBack = null,
                        autoOpenAddDialog = autoOpenAddDebtDialog,
                        onAddDialogDismissed = { autoOpenAddDebtDialog = false },
                        onAddCardReminder = { viewModel.addCreditCardReminder(it, context) },
                        onUpdateCardReminder = { viewModel.updateCreditCardReminder(it, context) },
                        onDeleteCardReminder = { viewModel.deleteCreditCardReminder(it) },
                        onPayCardFee = { viewModel.payCreditCardFee(it) },
                        onTestNotification = { card, isCutOff, daysBefore ->
                            viewModel.testCardNotification(context, card, isCutOff, daysBefore)
                        }
                    )
                }
                AppDestination.METAS_AHORRO -> {
                    SavingsScreen(
                        state = state,
                        onAddGoal = { title, target, saved, date ->
                            viewModel.addSavingsGoal(title, target, saved, date)
                        },
                        onDeleteGoal = { viewModel.deleteSavingsGoal(it) },
                        onAddMovement = { goal, amt, isDep, note ->
                            viewModel.addSavingsMovement(goal, amt, isDep, note)
                        },
                        onBack = null
                    )
                }
                AppDestination.MAS -> {
                    when (currentMoreSubDestination) {
                        MoreSubDestination.MENU -> {
                            MoreMenuScreen(
                                state = state,
                                onNavigateToProfile = { currentMoreSubDestination = MoreSubDestination.MY_PROFILE },
                                onNavigateToCategories = { currentMoreSubDestination = MoreSubDestination.CATEGORIES_LIMITS },
                                onNavigateToMyData = { currentMoreSubDestination = MoreSubDestination.MY_DATA },
                                onNavigateToHelp = { currentMoreSubDestination = MoreSubDestination.HELP },
                                onNavigateToAppInfo = { currentMoreSubDestination = MoreSubDestination.APP_INFO },
                                onNavigateToAntExpenses = { currentMoreSubDestination = MoreSubDestination.ANT_EXPENSES },
                                onNavigateToSavings = { currentDestination = AppDestination.METAS_AHORRO },
                                onNavigateToDebts = { currentMoreSubDestination = MoreSubDestination.DEBTS },
                                onNavigateToFutureExpenses = { currentMoreSubDestination = MoreSubDestination.FUTURE_EXPENSES },
                                onNavigateToCalendar = { currentMoreSubDestination = MoreSubDestination.CALENDAR },
                                onNavigateToReports = { currentMoreSubDestination = MoreSubDestination.REPORTS },
                                onNavigateToRegister = { currentMoreSubDestination = MoreSubDestination.REGISTER }
                            )
                        }
                        MoreSubDestination.MY_PROFILE -> {
                            MyProfileScreen(
                                state = state,
                                onBack = { currentMoreSubDestination = MoreSubDestination.MENU },
                                onNavigateToEditProfile = { currentMoreSubDestination = MoreSubDestination.EDIT_PROFILE },
                                onNavigateToPreferences = { currentMoreSubDestination = MoreSubDestination.PREFERENCES },
                                onNavigateToPrivacy = { currentMoreSubDestination = MoreSubDestination.PRIVACY_SECURITY },
                                onNavigateToNotifications = { currentMoreSubDestination = MoreSubDestination.NOTIFICATIONS },
                                onNavigateToCategories = { currentMoreSubDestination = MoreSubDestination.CATEGORIES_LIMITS },
                                onNavigateToMyData = { currentMoreSubDestination = MoreSubDestination.MY_DATA },
                                onNavigateToHelp = { currentMoreSubDestination = MoreSubDestination.HELP },
                                onNavigateToAppInfo = { currentMoreSubDestination = MoreSubDestination.APP_INFO },
                                onNavigateToRegister = { currentMoreSubDestination = MoreSubDestination.REGISTER }
                            )
                        }
                        MoreSubDestination.EDIT_PROFILE -> {
                            EditProfileScreen(
                                state = state,
                                onBack = { currentMoreSubDestination = MoreSubDestination.MY_PROFILE },
                                onSave = { name, email, handle ->
                                    viewModel.updateUserSettings(
                                        state.userSettings.copy(
                                            userName = name,
                                            userEmail = email,
                                            userHandle = handle
                                        )
                                    )
                                },
                                onSaveInitialSetup = { inc, exp, sav ->
                                    viewModel.completeOnboarding(inc, exp, sav, false, false)
                                }
                            )
                        }
                        MoreSubDestination.PREFERENCES -> {
                            PreferencesScreen(
                                state = state,
                                onBack = { currentMoreSubDestination = MoreSubDestination.MY_PROFILE },
                                onUpdateTheme = { mode ->
                                    viewModel.updateUserSettings(state.userSettings.copy(isDarkMode = mode))
                                },
                                onUpdateCurrency = { curr ->
                                    viewModel.updateUserSettings(state.userSettings.copy(currency = curr))
                                },
                                onUpdateDateFormat = { fmt ->
                                    viewModel.updateUserSettings(state.userSettings.copy(dateFormat = fmt))
                                },
                                onUpdateFirstDay = { day ->
                                    viewModel.updateUserSettings(state.userSettings.copy(firstDayOfPeriod = day))
                                }
                            )
                        }
                        MoreSubDestination.PRIVACY_SECURITY -> {
                            PrivacySecurityScreen(
                                state = state,
                                onBack = { currentMoreSubDestination = MoreSubDestination.MY_PROFILE },
                                onUpdateSecurity = { hideBalances, biometrics, pin, pinEnabled ->
                                    viewModel.updateUserSettings(
                                        state.userSettings.copy(
                                            hideBalancesDefault = hideBalances,
                                            biometricsEnabled = biometrics,
                                            pinCode = pin,
                                            isPinEnabled = pinEnabled
                                        )
                                    )
                                },
                                onSignOut = {
                                    viewModel.logOut()
                                    authMode = AuthMode.LOGIN
                                }
                            )
                        }
                        MoreSubDestination.NOTIFICATIONS -> {
                            NotificationsScreen(
                                state = state,
                                onBack = { currentMoreSubDestination = MoreSubDestination.MY_PROFILE },
                                onUpdateSettings = { viewModel.updateUserSettings(it) }
                            )
                        }
                        MoreSubDestination.CATEGORIES_LIMITS -> {
                            CategoriesLimitsScreen(
                                state = state,
                                onBack = { currentMoreSubDestination = MoreSubDestination.MENU },
                                onToggleCategory = { viewModel.updateCategory(it.copy(isActive = !it.isActive)) },
                                onAddCategory = { name, icon, color, type ->
                                    viewModel.addCategory(name, icon, color, type)
                                },
                                onUpdateCategory = { cat, oldName ->
                                    viewModel.updateCategory(cat, oldName)
                                },
                                onDeleteCategory = { cat ->
                                    viewModel.deleteCategory(cat)
                                },
                                onUpdateAntLimit = { limit ->
                                    viewModel.updateUserSettings(state.userSettings.copy(antExpenseLimit = limit))
                                }
                            )
                        }
                        MoreSubDestination.MY_DATA -> {
                            MyDataScreen(
                                state = state,
                                onBack = { currentMoreSubDestination = MoreSubDestination.MENU },
                                onExportCsv = { viewModel.showSnackbar("Archivo CSV descargado con éxito.") },
                                onExportExcel = { viewModel.showSnackbar("Archivo Excel exportado con éxito.") },
                                onExportPdf = { viewModel.showSnackbar("Reporte en PDF generado con éxito.") },
                                onToggleBackup = { enabled ->
                                    viewModel.updateUserSettings(state.userSettings.copy(backupEnabled = enabled))
                                    viewModel.showSnackbar(if (enabled) "Copia de seguridad activada." else "Copia de seguridad desactivada.")
                                },
                                onClearDataConfirmed = {
                                    viewModel.clearAllData()
                                    viewModel.showSnackbar("Todos los datos fueron eliminados correctamente.")
                                }
                            )
                        }
                        MoreSubDestination.HELP -> {
                            HelpScreen(
                                onBack = { currentMoreSubDestination = MoreSubDestination.MENU }
                            )
                        }
                        MoreSubDestination.APP_INFO -> {
                            AppInfoScreen(
                                onBack = { currentMoreSubDestination = MoreSubDestination.MENU }
                            )
                        }
                        MoreSubDestination.ANT_EXPENSES -> {
                            AntExpensesScreen(
                                state = state,
                                onUpdateUserSettings = { viewModel.updateUserSettings(it) },
                                onOpenQuickAdd = {
                                    activeCreationScreen = CreationScreenType.GASTO
                                },
                                onBack = { currentMoreSubDestination = MoreSubDestination.MENU }
                            )
                        }
                        MoreSubDestination.SAVINGS -> {
                            SavingsScreen(
                                state = state,
                                onAddGoal = { title, target, saved, date ->
                                    viewModel.addSavingsGoal(title, target, saved, date)
                                },
                                onDeleteGoal = { viewModel.deleteSavingsGoal(it) },
                                onAddMovement = { goal, amt, isDep, note ->
                                    viewModel.addSavingsMovement(goal, amt, isDep, note)
                                },
                                onBack = { currentMoreSubDestination = MoreSubDestination.MENU }
                            )
                        }
                        MoreSubDestination.DEBTS -> {
                            DebtsScreen(
                                state = state,
                                onAddDebt = { title, creditor, orig, rem, inst, date, numInst ->
                                    viewModel.addDebt(title, creditor, orig, rem, inst, date, numInst)
                                },
                                onRecordPayment = { debt, amt ->
                                    viewModel.recordDebtPayment(debt, amt)
                                },
                                onDeleteDebt = { viewModel.deleteDebt(it) },
                                onBack = { currentMoreSubDestination = MoreSubDestination.MENU },
                                autoOpenAddDialog = autoOpenAddDebtDialog,
                                onAddDialogDismissed = { autoOpenAddDebtDialog = false },
                                onAddCardReminder = { viewModel.addCreditCardReminder(it, context) },
                                onUpdateCardReminder = { viewModel.updateCreditCardReminder(it, context) },
                                onDeleteCardReminder = { viewModel.deleteCreditCardReminder(it) },
                                onPayCardFee = { viewModel.payCreditCardFee(it) },
                                onTestNotification = { card, isCutOff, daysBefore ->
                                    viewModel.testCardNotification(context, card, isCutOff, daysBefore)
                                }
                            )
                        }
                        MoreSubDestination.FUTURE_EXPENSES -> {
                            FutureExpensesScreen(
                                state = state,
                                onMarkPaid = { viewModel.markFutureExpensePaid(it) },
                                onDelete = { viewModel.deleteFutureExpense(it) },
                                onOpenQuickAdd = {
                                    activeCreationScreen = CreationScreenType.GASTO_FUTURO
                                },
                                onBack = { currentMoreSubDestination = MoreSubDestination.MENU }
                            )
                        }
                        MoreSubDestination.CALENDAR -> {
                            CalendarScreen(
                                state = state,
                                onBack = { currentMoreSubDestination = MoreSubDestination.MENU }
                            )
                        }
                        MoreSubDestination.REGISTER -> {
                            RegisterScreen(
                                state = state,
                                onBack = { currentMoreSubDestination = MoreSubDestination.MENU },
                                onNavigateToLogin = {
                                    viewModel.logOut()
                                    authMode = AuthMode.LOGIN
                                },
                                onRegisterComplete = { name, email, handle, curr, pass, pin, initialBal ->
                                    viewModel.registerAccount(
                                        name = name,
                                        email = email,
                                        handle = handle,
                                        currency = curr,
                                        pass = pass,
                                        pin = pin,
                                        initialBalance = initialBal
                                    )
                                    currentDestination = AppDestination.INICIO
                                }
                            )
                        }
                        MoreSubDestination.REPORTS -> {
                            ReportsScreen(
                                state = state,
                                onSelectPeriod = { viewModel.selectReportPeriod(it) },
                                onShowSnackbar = { viewModel.showSnackbar(it) }
                            )
                        }
                        MoreSubDestination.BUDGETS -> {
                            BudgetsScreen(
                                state = state,
                                onSetBudget = { cat, limit -> viewModel.setBudget(cat, limit) },
                                onDeleteBudget = { viewModel.deleteBudget(it) }
                            )
                        }
                    }
                }
            }

            // Quick Actions Floating Menu (Dashboard acciones rápidas.png)
            if (showQuickActions) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { showQuickActions = false }
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 80.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 8.dp,
                            modifier = Modifier.widthIn(min = 230.dp)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                QuickActionRow(
                                    icon = Icons.Default.RemoveCircleOutline,
                                    iconColor = CrimsonRed,
                                    title = "Agregar gasto",
                                    onClick = {
                                        showQuickActions = false
                                        activeCreationScreen = CreationScreenType.GASTO
                                    }
                                )
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                QuickActionRow(
                                    icon = Icons.Default.AddCircleOutline,
                                    iconColor = ForestGreen,
                                    title = "Agregar Ingreso",
                                    onClick = {
                                        showQuickActions = false
                                        activeCreationScreen = CreationScreenType.INGRESO
                                    }
                                )
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                QuickActionRow(
                                    icon = Icons.Default.CreditCard,
                                    iconColor = CrimsonRed,
                                    title = "Registrar pago de deuda",
                                    onClick = {
                                        showQuickActions = false
                                        if (state.debts.isEmpty() || state.debts.all { it.remainingBalance <= 0 }) {
                                            currentDestination = AppDestination.DEUDAS
                                            autoOpenAddDebtDialog = true
                                            viewModel.showSnackbar("No tienes deudas registradas. Crea una deuda nueva para comenzar.")
                                        } else {
                                            activeCreationScreen = CreationScreenType.PAGO_DEUDA
                                        }
                                    }
                                )
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                QuickActionRow(
                                    icon = Icons.Default.Savings,
                                    iconColor = ForestGreen,
                                    title = "Agregar ahorro",
                                    onClick = {
                                        showQuickActions = false
                                        activeCreationScreen = CreationScreenType.AHORRO
                                    }
                                )
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                QuickActionRow(
                                    icon = Icons.Default.Event,
                                    iconColor = ForestGreen,
                                    title = "agregar gasto futuro",
                                    onClick = {
                                        showQuickActions = false
                                        activeCreationScreen = CreationScreenType.GASTO_FUTURO
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E293B)
        )
    }
}
