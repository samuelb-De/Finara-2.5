package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.theme.*
import java.util.*

enum class QuickAddTab(val label: String, val icon: String) {
    GASTO("Agregar gasto", "restaurant"),
    INGRESO("Agregar Ingreso", "attach_money"),
    DEUDA("Registar pago de deuda", "credit_card"),
    AHORRO("Agregar ahorro", "savings"),
    FUTURO("Agregar gasto futuro", "flight")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddModal(
    state: FinanceUiState,
    initialTab: QuickAddTab = QuickAddTab.GASTO,
    onDismiss: () -> Unit,
    onAddExpense: (amount: Double, category: String, title: String, isAnt: Boolean, notes: String) -> Unit,
    onAddIncome: (amount: Double, title: String, category: String, frequency: String, notes: String) -> Unit,
    onAddSavings: (goal: SavingsGoalEntity, amount: Double, isDeposit: Boolean, note: String) -> Unit,
    onAddFutureExpense: (title: String, amount: Double, dueDate: Long, category: String, priority: String, repeat: String) -> Unit,
    onAddDebtPayment: (debt: DebtEntity, amount: Double, note: String) -> Unit,
    onOpenCreateDebt: () -> Unit = {}
) {
    var currentTab by remember { mutableStateOf(initialTab) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("quick_add_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registro Rápido",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab selector
            ScrollableTabRow(
                selectedTabIndex = currentTab.ordinal,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickAddTab.values().forEach { tab ->
                    Tab(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        text = {
                            Text(
                                text = tab.label,
                                fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (currentTab) {
                QuickAddTab.GASTO -> QuickExpenseForm(state, onAddExpense, onDismiss)
                QuickAddTab.INGRESO -> QuickIncomeForm(state, onAddIncome, onDismiss)
                QuickAddTab.AHORRO -> QuickSavingsForm(state, onAddSavings, onDismiss)
                QuickAddTab.FUTURO -> QuickFutureExpenseForm(state, onAddFutureExpense, onDismiss)
                QuickAddTab.DEUDA -> QuickDebtPaymentForm(state, onAddDebtPayment, onDismiss, onOpenCreateDebt)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun QuickExpenseForm(
    state: FinanceUiState,
    onSave: (amount: Double, category: String, title: String, isAnt: Boolean, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(state.categories.firstOrNull()?.name ?: "Alimentación") }
    var titleText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var isAntExpense by remember { mutableStateOf(false) }

    val amountValue = NumberFormatHelper.parseToDouble(amountText)
    val isUnderAntLimit = amountValue > 0 && amountValue <= state.userSettings.antExpenseLimit

    // Auto-suggest ant expense if under threshold and user enabled prompt
    LaunchedEffect(amountValue) {
        if (state.userSettings.promptAntExpense && isUnderAntLimit) {
            isAntExpense = true
        } else if (!isUnderAntLimit) {
            isAntExpense = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Value field (Large, centered)
        OutlinedTextField(
            value = amountText,
            onValueChange = { input ->
                amountText = NumberFormatHelper.formatWithThousands(input)
            },
            label = { Text("Valor (${state.userSettings.currency}) *") },
            placeholder = { Text("0") },
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = FinanceRed),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("expense_amount_input"),
            leadingIcon = {
                Text(
                    text = if (state.userSettings.currency == "EUR") "€" else "$",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FinanceRed,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        )

        // Category Selector Chips
        Text(
            text = "Categoría *",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val expenseCats = state.categories.filter {
                it.isActive && (it.categoryType == "EXPENSE" || (it.categoryType.isEmpty() && it.name != "Salario / Ingresos" && it.name != "Ahorro"))
            }
            items(expenseCats) { cat ->
                val isSelected = selectedCategory == cat.name
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat.name },
                    label = { Text(cat.name) },
                    leadingIcon = {
                        Icon(
                            imageVector = IconHelper.getIconVector(cat.iconName),
                            contentDescription = cat.name,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color(cat.colorHex),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        // Optional Title / Description
        OutlinedTextField(
            value = titleText,
            onValueChange = { titleText = it },
            label = { Text("Descripción (opcional)") },
            placeholder = { Text("Ej: Almuerzo, Café, Gasolina...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Ant Expense Prompt if below threshold
        if (state.userSettings.promptAntExpense && isUnderAntLimit) {
            Card(
                colors = CardDefaults.cardColors(containerColor = FinanceAmberLight.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = FinanceAmberDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¿Marcar como gasto hormiga? (≤ ${FinanceRepository.formatCurrency(state.userSettings.antExpenseLimit, state.userSettings.currency)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = isAntExpense,
                        onCheckedChange = { isAntExpense = it }
                    )
                }
            }
        }

        // Save Button
        Button(
            onClick = {
                if (amountValue > 0) {
                    onSave(amountValue, selectedCategory, titleText, isAntExpense, notesText)
                    onDismiss()
                }
            },
            enabled = amountValue > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_expense_button"),
            colors = ButtonDefaults.buttonColors(containerColor = FinanceRed)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Gasto", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun QuickIncomeForm(
    state: FinanceUiState,
    onSave: (amount: Double, title: String, category: String, frequency: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val incomeCats = remember(state.categories) {
        val list = state.categories.filter {
            it.isActive && (it.categoryType == "INCOME" || it.name in listOf("Salario", "Salario / Ingresos", "Honorarios", "Honorarios / Freelance", "Negocio", "Negocio / Ventas", "Inversión", "Inversiones", "Regalo", "Regalos", "Ventas", "Freelance", "Otros ingresos"))
        }
        if (list.isNotEmpty()) list else state.categories.filter { it.name == "Salario / Ingresos" || it.name == "Salario" }
    }
    var amountText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var selectedCategory by remember(incomeCats) {
        mutableStateOf(incomeCats.firstOrNull()?.name ?: "Salario")
    }
    var selectedFrequency by remember { mutableStateOf("Mensual") }
    val frequencies = listOf("Único", "Diario", "Semanal", "Quincenal", "Mensual")

    val amountValue = NumberFormatHelper.parseToDouble(amountText)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = amountText,
            onValueChange = { input ->
                amountText = NumberFormatHelper.formatWithThousands(input)
            },
            label = { Text("Valor del Ingreso (${state.userSettings.currency}) *") },
            placeholder = { Text("0") },
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = FinanceGreen),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("income_amount_input")
        )

        // Categoría de Ingreso
        Text(
            text = "Categoría de ingreso *",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(incomeCats) { cat ->
                val isSelected = selectedCategory == cat.name
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat.name },
                    label = { Text(cat.name) },
                    leadingIcon = {
                        Icon(
                            imageVector = IconHelper.getIconVector(cat.iconName),
                            contentDescription = cat.name,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color(cat.colorHex),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        OutlinedTextField(
            value = titleText,
            onValueChange = { titleText = it },
            label = { Text("Concepto / Descripción (opcional)") },
            placeholder = { Text("Ej: Salario mensual, Pago de cliente...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Frecuencia", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(frequencies) { freq ->
                FilterChip(
                    selected = selectedFrequency == freq,
                    onClick = { selectedFrequency = freq },
                    label = { Text(freq) }
                )
            }
        }

        Button(
            onClick = {
                if (amountValue > 0) {
                    val finalTitle = if (titleText.isNotBlank()) titleText else selectedCategory
                    onSave(amountValue, finalTitle, selectedCategory, selectedFrequency, "")
                    onDismiss()
                }
            },
            enabled = amountValue > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FinanceGreen)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Ingreso", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun QuickSavingsForm(
    state: FinanceUiState,
    onSave: (goal: SavingsGoalEntity, amount: Double, isDeposit: Boolean, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (state.savingsGoals.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No tienes objetivos de ahorro creados todavía.")
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Ve a la sección 'Más > Ahorros' para crear tu primer objetivo de ahorro.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedGoal by remember { mutableStateOf(state.savingsGoals.first()) }
    var isDeposit by remember { mutableStateOf(true) } // true: Aporte, false: Retiro
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    val amountValue = NumberFormatHelper.parseToDouble(amountText)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Tab Aporte / Retiro
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { isDeposit = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDeposit) FinanceTeal else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isDeposit) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("+ Aportar")
            }
            Button(
                onClick = { isDeposit = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isDeposit) FinanceAmber else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (!isDeposit) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("- Retirar")
            }
        }

        // Goal selector
        Text("Objetivo de ahorro", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.savingsGoals) { goal ->
                FilterChip(
                    selected = selectedGoal.id == goal.id,
                    onClick = { selectedGoal = goal },
                    label = { Text(goal.title) },
                    leadingIcon = {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = FinanceTeal, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = NumberFormatHelper.formatWithThousands(it) },
            label = { Text("Monto a ${if (isDeposit) "aportar" else "retirar"} *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Nota (opcional)") },
            placeholder = { Text("Ej: Ahorro quincena, bono...") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (amountValue > 0) {
                    onSave(selectedGoal, amountValue, isDeposit, noteText)
                    onDismiss()
                }
            },
            enabled = amountValue > 0,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isDeposit) FinanceTeal else FinanceAmber)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar Movimiento", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuickFutureExpenseForm(
    state: FinanceUiState,
    onSave: (title: String, amount: Double, dueDate: Long, category: String, priority: String, repeat: String) -> Unit,
    onDismiss: () -> Unit
) {
    var titleText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Servicios") }
    var selectedPriority by remember { mutableStateOf("MEDIA") }
    var daysAhead by remember { mutableStateOf(7) }

    val amountValue = NumberFormatHelper.parseToDouble(amountText)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = titleText,
            onValueChange = { titleText = it },
            label = { Text("Nombre del gasto futuro *") },
            placeholder = { Text("Ej: Internet, Matrícula, Mantenimiento...") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = NumberFormatHelper.formatWithThousands(it) },
            label = { Text("Valor estimado (${state.userSettings.currency}) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Vencimiento", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(7 to "En 7 días", 15 to "En 15 días", 30 to "En 30 días", 60 to "En 2 meses").forEach { (days, label) ->
                FilterChip(
                    selected = daysAhead == days,
                    onClick = { daysAhead = days },
                    label = { Text(label) }
                )
            }
        }

        Text("Prioridad", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALTA", "MEDIA", "BAJA").forEach { prio ->
                FilterChip(
                    selected = selectedPriority == prio,
                    onClick = { selectedPriority = prio },
                    label = { Text(prio) }
                )
            }
        }

        Button(
            onClick = {
                if (titleText.isNotBlank() && amountValue > 0) {
                    val dueDate = System.currentTimeMillis() + (daysAhead * 24L * 3600 * 1000)
                    onSave(titleText, amountValue, dueDate, selectedCategory, selectedPriority, "Único")
                    onDismiss()
                }
            },
            enabled = titleText.isNotBlank() && amountValue > 0,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FinanceBlue)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Programar Gasto Futuro", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuickDebtPaymentForm(
    state: FinanceUiState,
    onSave: (debt: DebtEntity, amount: Double, note: String) -> Unit,
    onDismiss: () -> Unit,
    onOpenCreateDebt: () -> Unit = {}
) {
    val activeDebts = state.debts.filter { it.remainingBalance > 0 }
    if (activeDebts.isEmpty()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No tienes deudas activas pendientes.", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onDismiss()
                    onOpenCreateDebt()
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear nueva deuda")
            }
        }
        return
    }

    var selectedDebt by remember { mutableStateOf(activeDebts.first()) }
    var amountText by remember { mutableStateOf(NumberFormatHelper.formatWithThousands(selectedDebt.installmentAmount.toLong().toString())) }
    var noteText by remember { mutableStateOf("") }
    val amountValue = NumberFormatHelper.parseToDouble(amountText)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Selecciona la deuda a abonar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(activeDebts) { debt ->
                FilterChip(
                    selected = selectedDebt.id == debt.id,
                    onClick = {
                        selectedDebt = debt
                        amountText = NumberFormatHelper.formatWithThousands(debt.installmentAmount.toLong().toString())
                    },
                    label = { Text("${debt.title} (${FinanceRepository.formatCurrency(debt.remainingBalance, state.userSettings.currency)})") }
                )
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = NumberFormatHelper.formatWithThousands(it) },
            label = { Text("Valor de la cuota o pago *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Nota (opcional)") },
            placeholder = { Text("Ej: Cuota ${selectedDebt.remainingInstallments}...") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (amountValue > 0) {
                    onSave(selectedDebt, amountValue, noteText)
                    onDismiss()
                }
            },
            enabled = amountValue > 0,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FinanceAmberDark)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar Pago de Cuota", fontWeight = FontWeight.Bold)
        }
    }
}
