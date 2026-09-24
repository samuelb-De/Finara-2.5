package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.SavingsGoalDetail
import com.example.ui.components.CategoryIcon
import com.example.ui.components.ColombianBankDropdown
import com.example.ui.components.CustomCalendarSheet
import com.example.ui.components.IconHelper
import com.example.ui.components.NotificationHelper
import com.example.ui.components.NumberFormatHelper
import com.example.ui.components.SimpleProgressBar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// ==========================================
// 1. GASTOS HORMIGA (SECTION 4)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntExpensesScreen(
    state: FinanceUiState,
    onUpdateUserSettings: (UserSettingsEntity) -> Unit,
    onOpenQuickAdd: () -> Unit,
    onBack: () -> Unit
) {
    val currency = state.userSettings.currency
    var limitInput by remember { mutableStateOf(state.userSettings.antExpenseLimit.toLong().toString()) }
    var showLimitDialog by remember { mutableStateOf(false) }

    val antTransactions = state.transactions.filter { it.isAntExpense }.sortedByDescending { it.date }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gastos Hormiga 🐜", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { showLimitDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Configurar límite")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("ant_expenses_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FinanceAmberLight.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🐜", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Control de Fugas Financieras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Pequeños gastos diarios que sumados generan gran impacto.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Summary 4 periods
            item {
                Text("Total acumulado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AntMetricCard(title = "Hoy", amount = FinanceRepository.formatCurrency(state.antExpensesToday, currency), modifier = Modifier.weight(1f))
                    AntMetricCard(title = "Esta semana", amount = FinanceRepository.formatCurrency(state.antExpensesWeek, currency), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AntMetricCard(title = "Este mes", amount = FinanceRepository.formatCurrency(state.antExpensesMonth, currency), modifier = Modifier.weight(1f))
                    AntMetricCard(title = "Este año", amount = FinanceRepository.formatCurrency(state.antExpensesYear, currency), modifier = Modifier.weight(1f))
                }
            }

            // Proyecciones automáticas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Proyección de Impacto Futuro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Si mantienes un promedio diario similar a hoy (${FinanceRepository.formatCurrency(state.antExpensesToday, currency)}), esto gastarías:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Proyección semanal", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    FinanceRepository.formatCurrency(state.antDailyProjectedWeekly, currency),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FinanceAmberDark
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Proyección mensual", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    FinanceRepository.formatCurrency(state.antDailyProjectedMonthly, currency),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FinanceRed
                                )
                            }
                        }
                    }
                }
            }

            // Configuración rápida del umbral
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Límite para sugerir gasto hormiga", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Actualmente: ${FinanceRepository.formatCurrency(state.userSettings.antExpenseLimit, currency)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(onClick = { showLimitDialog = true }) {
                            Text("Cambiar")
                        }
                    }
                }
            }

            // Lista de gastos hormiga
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Historial de Gastos Hormiga", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onOpenQuickAdd) {
                        Text("+ Registrar")
                    }
                }
            }

            if (antTransactions.isEmpty()) {
                item {
                    Text(
                        "No tienes gastos clasificados como hormiga. Al registrar un gasto menor a ${FinanceRepository.formatCurrency(state.userSettings.antExpenseLimit, currency)}, la app te permitirá marcarlo fácilmente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(antTransactions, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        currency = currency,
                        onClick = {},
                        onDelete = {}
                    )
                }
            }
        }
    }

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text("Configurar Límite Gasto Hormiga") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Los gastos iguales o menores a este valor te preguntarán si deseas marcarlos como gastos hormiga.")
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) limitInput = it },
                        label = { Text("Límite ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Preguntar al registrar")
                        Switch(
                            checked = state.userSettings.promptAntExpense,
                            onCheckedChange = {
                                onUpdateUserSettings(state.userSettings.copy(promptAntExpense = it))
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = limitInput.toDoubleOrNull() ?: state.userSettings.antExpenseLimit
                        onUpdateUserSettings(state.userSettings.copy(antExpenseLimit = limit))
                        showLimitDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun AntMetricCard(title: String, amount: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FinanceAmberDark)
        }
    }
}

// ==========================================
// 2. AHORROS (SECTION 7)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    state: FinanceUiState,
    onAddGoal: (title: String, target: Double, saved: Double, date: Long) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    onAddMovement: (goal: SavingsGoalEntity, amount: Double, isDeposit: Boolean, note: String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val currency = state.userSettings.currency
    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForMovement by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var movementIsDeposit by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metas de Ahorro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateGoalDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva meta")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateGoalDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva meta")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("savings_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.savingsGoalDetails.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(56.dp), tint = FinanceTeal)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Aún no tienes metas de ahorro", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Crea una meta (ej. Fondo de emergencia, Vacaciones) y la app calculará cuánto necesitas ahorrar semanal y mensualmente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { showCreateGoalDialog = true }) {
                                Text("+ Crear mi primera meta")
                            }
                        }
                    }
                }
            } else {
                items(state.savingsGoalDetails, key = { it.goal.id }) { detail ->
                    SavingsGoalCard(
                        detail = detail,
                        currency = currency,
                        onDeposit = {
                            selectedGoalForMovement = detail.goal
                            movementIsDeposit = true
                        },
                        onWithdraw = {
                            selectedGoalForMovement = detail.goal
                            movementIsDeposit = false
                        },
                        onDelete = { onDeleteGoal(detail.goal) }
                    )
                }
            }
        }
    }

    // Dialog Create Goal
    if (showCreateGoalDialog) {
        CreateSavingsGoalDialog(
            currency = currency,
            onDismiss = { showCreateGoalDialog = false },
            onSave = { title, target, saved, date ->
                onAddGoal(title, target, saved, date)
                showCreateGoalDialog = false
            }
        )
    }

    // Dialog Movement (Aporte / Retiro)
    selectedGoalForMovement?.let { goal ->
        SavingsMovementDialog(
            goal = goal,
            isDeposit = movementIsDeposit,
            currency = currency,
            onDismiss = { selectedGoalForMovement = null },
            onConfirm = { amount, note ->
                onAddMovement(goal, amount, movementIsDeposit, note)
                selectedGoalForMovement = null
            }
        )
    }
}

@Composable
fun SavingsGoalCard(
    detail: SavingsGoalDetail,
    currency: String,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onDelete: () -> Unit
) {
    val g = detail.goal
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(FinanceTeal.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = FinanceTeal, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(g.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Meta: ${FinanceViewModel.formatDate(g.targetDate)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            SimpleProgressBar(
                progress = detail.progressPercent / 100f,
                color = FinanceTeal,
                height = 10.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Ahorrado: ${FinanceRepository.formatCurrency(g.savedAmount, currency)} (${detail.progressPercent.roundToInt()}%)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = FinanceTeal
                )
                Text(
                    text = "Faltan: ${FinanceRepository.formatCurrency(detail.remainingAmount, currency)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Cálculos automáticos de ahorro necesario
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Ahorro necesario semanal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(FinanceRepository.formatCurrency(detail.weeklyNeeded, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Ahorro necesario mensual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(FinanceRepository.formatCurrency(detail.monthlyNeeded, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons: Aportar / Retirar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onDeposit,
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aportar")
                }
                OutlinedButton(
                    onClick = onWithdraw,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retirar")
                }
            }
        }
    }
}

@Composable
fun CreateSavingsGoalDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (title: String, target: Double, saved: Double, date: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var savedText by remember { mutableStateOf("") }
    var monthsAhead by remember { mutableStateOf<Int?>(6) }
    var isCustomDeadlineMode by remember { mutableStateOf(false) }
    var customMonthsText by remember { mutableStateOf("6") }
    var customExactDate by remember { mutableStateOf(System.currentTimeMillis() + 180L * 24 * 3600 * 1000) }
    var useExactCalendarDate by remember { mutableStateOf(false) }
    var showCalendarPicker by remember { mutableStateOf(false) }
    var isCustomAmountMode by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "CO")) }
    val suggestedTitles = listOf("Fondo de emergencia", "Vacaciones", "Auto / Moto", "Estudios", "Vivienda")
    val presetAmounts = listOf(
        500000.0 to "500.000",
        1000000.0 to "1.000.000",
        2000000.0 to "2.000.000",
        5000000.0 to "5.000.000"
    )

    val effectiveTargetDate = if (isCustomDeadlineMode && useExactCalendarDate) {
        customExactDate
    } else {
        val months = (if (isCustomDeadlineMode) customMonthsText.toIntOrNull() else monthsAhead) ?: 6
        System.currentTimeMillis() + (months.coerceAtLeast(1) * 30L * 24 * 3600 * 1000)
    }

    if (showCalendarPicker) {
        CustomCalendarSheet(
            initialDate = customExactDate,
            onDismiss = { showCalendarPicker = false },
            onDateSelected = { selected ->
                customExactDate = selected
                useExactCalendarDate = true
                showCalendarPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Savings,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva Meta de Ahorro", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sugerencias de títulos
                Text("Ideas para tu meta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(suggestedTitles) { idea ->
                        SuggestionChip(
                            onClick = { title = idea },
                            label = { Text(idea, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la meta *") },
                    placeholder = { Text("Ej: Fondo de emergencia, Moto...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Sección Valor de la meta / Monto objetivo
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Valor de la meta ($currency) *",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isCustomAmountMode) {
                            Text(
                                text = "Personalizado",
                                style = MaterialTheme.typography.labelSmall,
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Chips de valores predeterminados y personalizado
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presetAmounts.forEach { (amt, label) ->
                            val isSelected = !isCustomAmountMode && NumberFormatHelper.parseToDouble(targetText) == amt
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    isCustomAmountMode = false
                                    targetText = NumberFormatHelper.formatWithThousands(label)
                                },
                                label = { Text("$ $label", fontSize = 11.sp) }
                            )
                        }
                        FilterChip(
                            selected = isCustomAmountMode,
                            onClick = {
                                isCustomAmountMode = true
                            },
                            label = { Text("Personalizado", fontSize = 11.sp) }
                        )
                    }

                    OutlinedTextField(
                        value = targetText,
                        onValueChange = {
                            isCustomAmountMode = true
                            targetText = NumberFormatHelper.formatWithThousands(it)
                        },
                        label = { Text(if (isCustomAmountMode) "Valor personalizado ($currency) *" else "Monto objetivo ($currency) *") },
                        placeholder = { Text("Ej: 3.500.000") },
                        leadingIcon = { Text("$ ", fontWeight = FontWeight.Bold, color = ForestGreen) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = {
                            Text(
                                if (isCustomAmountMode) "Ingresa cualquier valor personalizado que desees para tu meta"
                                else "Selecciona un valor sugerido o escribe un valor personalizado"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = savedText,
                    onValueChange = { savedText = NumberFormatHelper.formatWithThousands(it) },
                    label = { Text("Ahorro inicial ya guardado (opcional)") },
                    placeholder = { Text("0") },
                    leadingIcon = { Text("$ ", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Plazo objetivo de la meta (tiempo personalizado)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Plazo objetivo (tiempo) *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        if (isCustomDeadlineMode) {
                            Text("Tiempo personalizado", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(3 to "3 meses", 6 to "6 meses", 12 to "1 año", 24 to "2 años").forEach { (m, label) ->
                            FilterChip(
                                selected = !isCustomDeadlineMode && monthsAhead == m,
                                onClick = {
                                    isCustomDeadlineMode = false
                                    useExactCalendarDate = false
                                    monthsAhead = m
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                        FilterChip(
                            selected = isCustomDeadlineMode,
                            onClick = {
                                isCustomDeadlineMode = true
                                monthsAhead = null
                            },
                            label = { Text("Personalizado", fontSize = 11.sp) }
                        )
                    }

                    if (isCustomDeadlineMode) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = !useExactCalendarDate,
                                        onClick = { useExactCalendarDate = false },
                                        label = { Text("Por número de meses", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = useExactCalendarDate,
                                        onClick = {
                                            useExactCalendarDate = true
                                            showCalendarPicker = true
                                        },
                                        label = { Text("Fecha exacta", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (!useExactCalendarDate) {
                                    OutlinedTextField(
                                        value = customMonthsText,
                                        onValueChange = { if (it.all { c -> c.isDigit() }) customMonthsText = it },
                                        label = { Text("Número de meses objetivo *") },
                                        placeholder = { Text("Ej: 4, 8, 18, 36, 48...") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Surface(
                                        onClick = { showCalendarPicker = true },
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Fecha límite seleccionada", fontSize = 11.sp, color = Color(0xFF64748B))
                                                Text(
                                                    dateFormatter.format(Date(customExactDate)),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF1E293B)
                                                )
                                            }
                                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = ForestGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Badge de fecha final estimada
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Meta proyectada para: ${dateFormatter.format(Date(effectiveTargetDate))}",
                                fontSize = 11.sp,
                                color = ForestGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val target = NumberFormatHelper.parseToDouble(targetText)
            val isValid = title.isNotBlank() && target > 0 && effectiveTargetDate > System.currentTimeMillis()
            Button(
                onClick = {
                    val saved = NumberFormatHelper.parseToDouble(savedText)
                    if (isValid) {
                        onSave(title.trim(), target, saved, effectiveTargetDate)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Text("Crear Meta", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun SavingsMovementDialog(
    goal: SavingsGoalEntity,
    isDeposit: Boolean,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isDeposit) "Aportar a ${goal.title}" else "Retirar de ${goal.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = NumberFormatHelper.formatWithThousands(it) },
                    label = { Text("Monto ($currency) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Nota (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = NumberFormatHelper.parseToDouble(amountText)
                    if (amt > 0) {
                        onConfirm(amt, noteText)
                    }
                },
                enabled = NumberFormatHelper.parseToDouble(amountText) > 0
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ==========================================
// 3. GASTOS FUTUROS (SECTION 8)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureExpensesScreen(
    state: FinanceUiState,
    onMarkPaid: (FutureExpenseEntity) -> Unit,
    onDelete: (FutureExpenseEntity) -> Unit,
    onOpenQuickAdd: () -> Unit,
    onBack: () -> Unit
) {
    val currency = state.userSettings.currency
    var selectedWindow by remember { mutableStateOf(1) } // 0: 7 días, 1: 30 días, 2: 3 meses, 3: Todos

    val displayedList = when (selectedWindow) {
        0 -> state.futureExpenses7Days
        1 -> state.futureExpenses30Days
        2 -> state.futureExpenses3Months
        else -> state.futureExpenses
    }

    val totalRequired = displayedList.filter { it.status == "PENDIENTE" }.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gastos Futuros", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenQuickAdd) {
                Icon(Icons.Default.Add, contentDescription = "Programar gasto futuro")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("future_expenses_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Window filter tabs
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val tabs = listOf("Próximos 7 días", "Próximos 30 días", "Próximos 3 meses", "Todos")
                    items(tabs.indices.toList()) { idx ->
                        FilterChip(
                            selected = selectedWindow == idx,
                            onClick = { selectedWindow = idx },
                            label = { Text(tabs[idx]) }
                        )
                    }
                }
            }

            // Total required banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FinanceBlueLight.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Requerido para Cubrir Gastos", style = MaterialTheme.typography.labelMedium, color = FinanceBlueDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            FinanceRepository.formatCurrency(totalRequired, currency),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = FinanceBlueDark
                        )
                    }
                }
            }

            if (displayedList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay gastos programados en este período.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(displayedList, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (item.status == "PAGADO") FinanceGreenLight else FinanceAmberLight,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            item.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.status == "PAGADO") FinanceGreenDark else FinanceAmberDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    "Vence: ${FinanceViewModel.formatDate(item.dueDate)} • ${item.category} • Prioridad ${item.priority}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    FinanceRepository.formatCurrency(item.amount, currency),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (item.status == "PENDIENTE") {
                                    TextButton(onClick = { onMarkPaid(item) }) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pagar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. DEUDAS (SECTION 9)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    state: FinanceUiState,
    onAddDebt: (title: String, creditor: String, original: Double, remaining: Double, installment: Double, dueDate: Long, installments: Int) -> Unit,
    onRecordPayment: (debt: DebtEntity, amount: Double) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    onBack: (() -> Unit)? = null,
    autoOpenAddDialog: Boolean = false,
    onAddDialogDismissed: () -> Unit = {},
    onAddCardReminder: ((CreditCardReminderEntity) -> Unit)? = null,
    onUpdateCardReminder: ((CreditCardReminderEntity) -> Unit)? = null,
    onDeleteCardReminder: ((CreditCardReminderEntity) -> Unit)? = null,
    onPayCardFee: ((CreditCardReminderEntity) -> Unit)? = null,
    onTestNotification: ((card: CreditCardReminderEntity, isCutOff: Boolean, daysBefore: Int) -> Unit)? = null,
    initialTab: Int = 0
) {
    val currency = state.userSettings.currency
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showAddDialog by remember { mutableStateOf(autoOpenAddDialog) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CreditCardReminderEntity?>(null) }
    var testNotificationCard by remember { mutableStateOf<CreditCardReminderEntity?>(null) }
    var payingDebt by remember { mutableStateOf<DebtEntity?>(null) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            NotificationHelper.createNotificationChannel(context)
            NotificationHelper.scheduleAlarms(context, state.creditCardReminders)
        }
    }

    LaunchedEffect(autoOpenAddDialog) {
        if (autoOpenAddDialog) {
            showAddDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedTab == 0) "Deudas pendientes" else "Cuotas de manejo y Tarjetas",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (selectedTab == 0) showAddDialog = true else showAddCardDialog = true
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = if (selectedTab == 0) "Nueva deuda" else "Nueva tarjeta"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddDialog = true else showAddCardDialog = true
                },
                containerColor = Color.White,
                contentColor = ForestGreen,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = if (selectedTab == 0) "Nueva deuda" else "Nueva tarjeta"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = ForestGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Deudas y Créditos (${state.debts.count { it.remainingBalance > 0 }})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Cuotas y Tarjetas (${state.creditCardReminders.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("debts_screen"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
            // Header summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FinanceAmberLight.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Deudas Pendientes", style = MaterialTheme.typography.labelMedium, color = FinanceAmberDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            FinanceRepository.formatCurrency(state.totalPendingDebts, currency),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = FinanceAmberDark
                        )
                        Text(
                            "${state.debts.filter { it.remainingBalance > 0 }.size} créditos o compromisos activos",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (state.debts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFFEF2F2), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                "No tienes deudas registradas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                "Agrega tus compromisos, tarjetas o préstamos para llevar el control de pagos y cuotas.",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Crear una deuda nueva", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(state.debts, key = { it.id }) { debt ->
                    val progress = if (debt.originalAmount > 0) ((debt.originalAmount - debt.remainingBalance) / debt.originalAmount).toFloat() else 0f
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(debt.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Acreedor: ${debt.creditor} • Vence: ${FinanceViewModel.formatDate(debt.dueDate)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { onDeleteDebt(debt) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            SimpleProgressBar(
                                progress = progress.coerceIn(0f, 1f),
                                color = FinanceGreen,
                                height = 8.dp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Saldo: ${FinanceRepository.formatCurrency(debt.remainingBalance, currency)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = FinanceRed)
                                Text("Original: ${FinanceRepository.formatCurrency(debt.originalAmount, currency)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Cuota: ${FinanceRepository.formatCurrency(debt.installmentAmount, currency)} (${debt.remainingInstallments} restantes)", style = MaterialTheme.typography.labelSmall)
                                if (debt.remainingBalance > 0) {
                                    Button(onClick = { payingDebt = debt }) {
                                        Text("+ Registrar Pago")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
            // Tab 1: Cuotas de manejo y Tarjetas
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("card_reminders_tab"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header summary for card reminders
                    item {
                        val totalActiveFees = state.creditCardReminders
                            .filter { !it.isExonerated }
                            .sumOf { it.feeAmount }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Cuotas de Manejo y Recordatorios",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            FinanceRepository.formatCurrency(totalActiveFees, currency) + " / mes",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            "${state.creditCardReminders.size} tarjetas registradas con alertas programadas",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(Color(0xFFDCFCE7), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = ForestGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (!hasNotificationPermission) {
                                    Surface(
                                        color = Color(0xFFFEF2F2),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.NotificationsActive,
                                                    contentDescription = null,
                                                    tint = Color(0xFFDC2626),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Permiso de notificaciones necesario",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF991B1B)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Para recibir las alertas en tu celular cuando venza tu fecha de corte o pago, activa el permiso aquí:",
                                                fontSize = 11.sp,
                                                color = Color(0xFF7F1D1D)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Activar Notificaciones", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCFCE7)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = ForestGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Cronograma de notificaciones activas:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF14532D)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Te notificaremos automáticamente:\n• 7 días antes\n• 3 días antes\n• El día anterior\n• El mismo día de la fecha de pago y fecha de corte.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF166534),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.creditCardReminders.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(Color(0xFFEFF6FF), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Text("Sin tarjetas registradas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Registra tus tarjetas de crédito para que nunca olvides pagar la cuota de manejo, tu fecha de corte o tu fecha límite de pago.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(onClick = { showAddCardDialog = true }) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Agregar Tarjeta y Recordatorios")
                                    }
                                }
                            }
                        }
                    } else {
                        items(state.creditCardReminders, key = { it.id }) { card ->
                            CreditCardReminderItem(
                                card = card,
                                currency = currency,
                                onEdit = { editingCard = card },
                                onDelete = { onDeleteCardReminder?.invoke(card) },
                                onPayFee = { onPayCardFee?.invoke(card) },
                                onTestNotification = { testNotificationCard = card }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Add Debt
    if (showAddDialog) {
        AddDebtDialog(
            currency = currency,
            onDismiss = {
                showAddDialog = false
                onAddDialogDismissed()
            },
            onSave = { title, creditor, orig, rem, inst, date, numInst ->
                onAddDebt(title, creditor, orig, rem, inst, date, numInst)
                showAddDialog = false
                onAddDialogDismissed()
            }
        )
    }

    // Dialog Record Payment
    payingDebt?.let { debt ->
        var payAmountText by remember { mutableStateOf(NumberFormatHelper.formatWithThousands(debt.installmentAmount.toLong().toString())) }
        AlertDialog(
            onDismissRequest = { payingDebt = null },
            title = { Text("Registrar Pago a ${debt.title}") },
            text = {
                OutlinedTextField(
                    value = payAmountText,
                    onValueChange = { payAmountText = NumberFormatHelper.formatWithThousands(it) },
                    label = { Text("Monto pagado ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = NumberFormatHelper.parseToDouble(payAmountText)
                        if (amt > 0) {
                            onRecordPayment(debt, amt)
                            payingDebt = null
                        }
                    }
                ) {
                    Text("Confirmar Pago")
                }
            },
            dismissButton = {
                TextButton(onClick = { payingDebt = null }) { Text("Cancelar") }
            }
        )
    }

    // Dialog Add Card Reminder
    if (showAddCardDialog) {
        AddEditCreditCardReminderDialog(
            card = null,
            currency = currency,
            onDismiss = { showAddCardDialog = false },
            onSave = { newCard ->
                onAddCardReminder?.invoke(newCard)
                showAddCardDialog = false
            }
        )
    }

    // Dialog Edit Card Reminder
    editingCard?.let { cardToEdit ->
        AddEditCreditCardReminderDialog(
            card = cardToEdit,
            currency = currency,
            onDismiss = { editingCard = null },
            onSave = { updatedCard ->
                onUpdateCardReminder?.invoke(updatedCard)
                editingCard = null
            }
        )
    }

    // Dialog Test Notification Milestones (7d, 3d, 1d, hoy)
    testNotificationCard?.let { card ->
        TestNotificationDialog(
            card = card,
            onDismiss = { testNotificationCard = null },
            onTrigger = { isCutOff, daysBefore ->
                if (!hasNotificationPermission && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                if (onTestNotification != null) {
                    onTestNotification(card, isCutOff, daysBefore)
                } else {
                    NotificationHelper.triggerTestNotification(context, card, isCutOff, daysBefore)
                }
                testNotificationCard = null
            }
        )
    }
}

@Composable
fun CreditCardReminderItem(
    card: CreditCardReminderEntity,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPayFee: () -> Unit,
    onTestNotification: () -> Unit
) {
    val cutOffDaysLeft = remember(card.cutOffDay) { NotificationHelper.getDaysUntilDayOfMonth(card.cutOffDay) }
    val paymentDaysLeft = remember(card.paymentDay) { NotificationHelper.getDaysUntilDayOfMonth(card.paymentDay) }

    val cutOffCountdown = when (cutOffDaysLeft) {
        0 -> "¡Hoy es el corte!"
        1 -> "¡Mañana es el corte!"
        else -> "Faltan $cutOffDaysLeft días"
    }

    val paymentCountdown = when (paymentDaysLeft) {
        0 -> "¡Hoy es el pago!"
        1 -> "¡Mañana es el pago!"
        else -> "Faltan $paymentDaysLeft días"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Card Name, Bank & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(card.colorHex), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            card.cardName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            card.bankName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp), tint = Color(0xFF64748B))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", modifier = Modifier.size(18.dp), tint = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cuota de manejo row
            Surface(
                color = if (card.isExonerated) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (card.isExonerated) Color(0xFFA7F3D0) else Color(0xFFE2E8F0)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Cuota de manejo",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        if (card.isExonerated) {
                            Text(
                                "Exonerada ($ 0)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                            if (card.exonerationCondition.isNotBlank()) {
                                Text(
                                    card.exonerationCondition,
                                    fontSize = 11.sp,
                                    color = Color(0xFF059669)
                                )
                            }
                        } else {
                            Text(
                                "${FinanceRepository.formatCurrency(card.feeAmount, currency)} (${card.feeFrequency})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    if (!card.isExonerated) {
                        OutlinedButton(
                            onClick = onPayFee,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Pagar cuota", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2 Highlights: Fecha de Corte y Fecha de Pago
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Corte
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Corte: Día ${card.cutOffDay}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = if (cutOffDaysLeft in 0..3) Color(0xFFFEF3C7) else Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                cutOffCountdown,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (cutOffDaysLeft in 0..3) Color(0xFFB45309) else Color(0xFF0369A1),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Alertas: 7d • 3d • 1d • Hoy",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Pago
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pago: Día ${card.paymentDay}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = if (paymentDaysLeft in 0..3) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                paymentCountdown,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (paymentDaysLeft in 0..3) Color(0xFFB91C1C) else Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Alertas: 7d • 3d • 1d • Hoy",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action: Test Notification
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onTestNotification,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = ForestGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Probar notificación (7d, 3d, 1d, hoy)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestGreen
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditCreditCardReminderDialog(
    card: CreditCardReminderEntity?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (CreditCardReminderEntity) -> Unit
) {
    var cardName by remember { mutableStateOf(card?.cardName ?: "") }
    var bankName by remember { mutableStateOf(card?.bankName ?: "") }
    var isExonerated by remember { mutableStateOf(card?.isExonerated ?: false) }
    var feeAmountText by remember { mutableStateOf(if (card != null && card.feeAmount > 0) NumberFormatHelper.formatWithThousands(card.feeAmount.toLong().toString()) else "26500") }
    var exonerationCondition by remember { mutableStateOf(card?.exonerationCondition ?: "") }
    var feeFrequency by remember { mutableStateOf(card?.feeFrequency ?: "Mensual") }
    var cutOffDayText by remember { mutableStateOf(card?.cutOffDay?.toString() ?: "15") }
    var paymentDayText by remember { mutableStateOf(card?.paymentDay?.toString() ?: "5") }
    var notifyCutOff by remember { mutableStateOf(card?.notifyCutOff ?: true) }
    var notifyPayment by remember { mutableStateOf(card?.notifyPayment ?: true) }

    val frequencies = listOf("Mensual", "Trimestral", "Semestral", "Anual")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (card == null) "Agregar Tarjeta y Recordatorio" else "Editar Tarjeta y Recordatorio")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Nombre de la tarjeta (ej. Visa Clásica, Nu)") },
                    modifier = Modifier.fillMaxWidth()
                )

                ColombianBankDropdown(
                    selectedBank = bankName,
                    onBankSelected = { bankName = it },
                    label = "Banco / Entidad en Colombia",
                    modifier = Modifier.fillMaxWidth()
                )

                // Cutoff and Payment Days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = cutOffDayText,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() } && (it.toIntOrNull() ?: 0) <= 31) {
                                cutOffDayText = it
                            }
                        },
                        label = { Text("Día de corte (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = paymentDayText,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() } && (it.toIntOrNull() ?: 0) <= 31) {
                                paymentDayText = it
                            }
                        },
                        label = { Text("Día de pago (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Switch Exonerada
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("¿Exonerada de cuota de manejo?", fontSize = 13.sp)
                    Switch(
                        checked = isExonerated,
                        onCheckedChange = { isExonerated = it }
                    )
                }

                if (!isExonerated) {
                    OutlinedTextField(
                        value = feeAmountText,
                        onValueChange = { feeAmountText = NumberFormatHelper.formatWithThousands(it) },
                        label = { Text("Valor de cuota de manejo ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Frecuencia
                    Column {
                        Text("Frecuencia de cobro", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            frequencies.forEach { freq ->
                                val isSelected = feeFrequency == freq
                                Surface(
                                    onClick = { feeFrequency = freq },
                                    color = if (isSelected) ForestGreen else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        freq,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = exonerationCondition,
                        onValueChange = { exonerationCondition = it },
                        label = { Text("Condición de exoneración (ej. 3 compras al mes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Info: Milestones
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "Recordatorios automáticos incluidos:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Text(
                            "Se enviará notificación a tu celular 7 días antes, 3 días antes, el día anterior y el mismo día de cada fecha programada.",
                            fontSize = 11.sp,
                            color = Color(0xFF166534)
                        )
                    }
                }

                // Switches for cutoff and payment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notificar fecha de corte", fontSize = 13.sp)
                    Switch(checked = notifyCutOff, onCheckedChange = { notifyCutOff = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notificar fecha de pago", fontSize = 13.sp)
                    Switch(checked = notifyPayment, onCheckedChange = { notifyPayment = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cutOff = cutOffDayText.toIntOrNull()?.coerceIn(1, 31) ?: 15
                    val payment = paymentDayText.toIntOrNull()?.coerceIn(1, 31) ?: 5
                    val feeAmt = if (isExonerated) 0.0 else NumberFormatHelper.parseToDouble(feeAmountText)

                    val newOrUpdated = card?.copy(
                        cardName = cardName.ifBlank { "Tarjeta de Crédito" },
                        bankName = bankName.ifBlank { "Banco" },
                        feeAmount = feeAmt,
                        feeFrequency = feeFrequency,
                        cutOffDay = cutOff,
                        paymentDay = payment,
                        isExonerated = isExonerated,
                        exonerationCondition = exonerationCondition,
                        reminderEnabled = true,
                        notifyCutOff = notifyCutOff,
                        notifyPayment = notifyPayment
                    ) ?: CreditCardReminderEntity(
                        cardName = cardName.ifBlank { "Tarjeta de Crédito" },
                        bankName = bankName.ifBlank { "Banco" },
                        feeAmount = feeAmt,
                        feeFrequency = feeFrequency,
                        cutOffDay = cutOff,
                        paymentDay = payment,
                        isExonerated = isExonerated,
                        exonerationCondition = exonerationCondition,
                        reminderEnabled = true,
                        notifyCutOff = notifyCutOff,
                        notifyPayment = notifyPayment,
                        colorHex = 0xFF0D47A1
                    )

                    onSave(newOrUpdated)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun TestNotificationDialog(
    card: CreditCardReminderEntity,
    onDismiss: () -> Unit,
    onTrigger: (isCutOff: Boolean, daysBefore: Int) -> Unit
) {
    val milestones = listOf(
        Pair("7 días antes", 7),
        Pair("3 días antes", 3),
        Pair("El día anterior", 1),
        Pair("El mismo día (Hoy)", 0)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ForestGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Probar Notificaciones")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Selecciona qué notificación deseas probar para la tarjeta '${card.cardName}':",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )

                // Cutoff Options
                Text(
                    "Recordatorios de Fecha de Corte (Día ${card.cutOffDay}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0369A1)
                )
                milestones.forEach { (label, days) ->
                    OutlinedButton(
                        onClick = { onTrigger(true, days) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simular corte: $label", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Payment Options
                Text(
                    "Recordatorios de Fecha de Pago (Día ${card.paymentDay}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
                milestones.forEach { (label, days) ->
                    Button(
                        onClick = { onTrigger(false, days) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simular pago: $label", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun AddDebtDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (title: String, creditor: String, orig: Double, rem: Double, inst: Double, date: Long, numInst: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var creditor by remember { mutableStateOf("") }
    var origText by remember { mutableStateOf("") }
    var instText by remember { mutableStateOf("") }
    var numInstText by remember { mutableStateOf("12") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear una cuota / deuda") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Concepto de la cuota (ej. Tarjeta de crédito, Préstamo)") }, modifier = Modifier.fillMaxWidth())
                ColombianBankDropdown(
                    selectedBank = creditor,
                    onBankSelected = { creditor = it },
                    label = "Banco / Entidad en Colombia",
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = origText, onValueChange = { origText = NumberFormatHelper.formatWithThousands(it) }, label = { Text("Monto Total ($currency)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = instText, onValueChange = { instText = NumberFormatHelper.formatWithThousands(it) }, label = { Text("Valor de la Cuota ($currency)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = numInstText, onValueChange = { if (it.all { c -> c.isDigit() }) numInstText = it }, label = { Text("Cuotas restantes") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val orig = NumberFormatHelper.parseToDouble(origText)
                    val inst = NumberFormatHelper.parseToDouble(instText)
                    val num = numInstText.toIntOrNull() ?: 1
                    val dueDate = System.currentTimeMillis() + (30L * 24 * 3600 * 1000)
                    if (title.isNotBlank() && orig > 0) {
                        onSave(title, creditor, orig, orig, inst, dueDate, num)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ==========================================
// 5. CALENDARIO FINANCIERO (SECTION 11)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    state: FinanceUiState,
    onBack: () -> Unit
) {
    val currency = state.userSettings.currency
    var calendarMonthOffset by remember { mutableStateOf(0) } // 0 = current month
    val cal = Calendar.getInstance().apply { add(Calendar.MONTH, calendarMonthOffset) }
    val monthYearTitle = SimpleDateFormat("MMMM yyyy", Locale("es", "CO")).format(cal.time).replaceFirstChar { it.uppercase() }

    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday...
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    var selectedDayNumber by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    // Calculate transactions on selected day
    val selectedDayStart = Calendar.getInstance().apply {
        add(Calendar.MONTH, calendarMonthOffset)
        set(Calendar.DAY_OF_MONTH, selectedDayNumber)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis

    val selectedDayEnd = selectedDayStart + (24L * 3600 * 1000)

    val dayTransactions = state.transactions.filter { it.date in selectedDayStart until selectedDayEnd }
    val dayFutureExpenses = state.futureExpenses.filter { it.dueDate in selectedDayStart until selectedDayEnd }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario Financiero", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Month Header with Prev/Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { calendarMonthOffset-- }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
                }
                Text(monthYearTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { calendarMonthOffset++ }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
                }
            }

            // Days of week header
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("D", "L", "M", "M", "J", "V", "S").forEach { d ->
                    Text(
                        text = d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid
            val totalCells = (firstDayOfWeek - 1) + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNum = cellIndex - (firstDayOfWeek - 1) + 1
                            if (dayNum in 1..daysInMonth) {
                                val isSelected = dayNum == selectedDayNumber
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .clickable { selectedDayNumber = dayNum },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$dayNum",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Text("Movimientos del día $selectedDayNumber de $monthYearTitle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            if (dayTransactions.isEmpty() && dayFutureExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Sin movimientos registrados para este día.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dayTransactions) { tx ->
                        TransactionItemCard(transaction = tx, currency = currency, onClick = {}, onDelete = {})
                    }
                    items(dayFutureExpenses) { fe ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = FinanceBlueLight.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gasto Futuro: ${fe.title}", fontWeight = FontWeight.Bold)
                                Text(FinanceRepository.formatCurrency(fe.amount, currency), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. CATEGORÍAS (SECTION 5)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    state: FinanceUiState,
    onAddCategory: (name: String, icon: String, color: Long) -> Unit,
    onToggleCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva categoría")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.categories, key = { it.id }) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(38.dp).background(Color(cat.colorHex).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = IconHelper.getIconVector(cat.iconName), contentDescription = null, tint = Color(cat.colorHex), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(cat.name, fontWeight = FontWeight.SemiBold)
                                if (cat.isDefault) {
                                    Text("Predeterminada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (!cat.isDefault) {
                            IconButton(onClick = { onDeleteCategory(cat) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var selectedIcon by remember { mutableStateOf(IconHelper.availableIcons.first().first) }
        var selectedColor by remember { mutableStateOf(IconHelper.availableColors.first().first) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva Categoría") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre de categoría *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Selecciona un icono", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(IconHelper.availableIcons) { (iconKey, iconLabel) ->
                            FilterChip(
                                selected = selectedIcon == iconKey,
                                onClick = { selectedIcon = iconKey },
                                label = { Text(iconLabel) },
                                leadingIcon = { Icon(IconHelper.getIconVector(iconKey), contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                    Text("Selecciona un color", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(IconHelper.availableColors) { (hex, colorName) ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(hex), CircleShape)
                                    .border(if (selectedColor == hex) 3.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddCategory(name, selectedIcon, selectedColor)
                            showAddDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// ==========================================
// 7. INSIGHTS INTELIGENTES (SECTION 14)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    state: FinanceUiState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inteligencia Financiera", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("insights_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Análisis 100% basados en tus datos reales, sin adivinanzas.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (state.insights.isEmpty()) {
                item {
                    Text("Registra más gastos e ingresos para habilitar análisis automáticos de tus finanzas.")
                }
            } else {
                items(state.insights) { insight ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(insight.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(insight.description, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Fuente: ${insight.sourceData}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. AJUSTES Y PERFIL (SECTION 15 & 20)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    state: FinanceUiState,
    onUpdateSettings: (UserSettingsEntity) -> Unit,
    onLoadDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    onLockApp: () -> Unit,
    onBack: () -> Unit
) {
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes y Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("profile_settings_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // User Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(52.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.userSettings.userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(state.userSettings.userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Moneda: ${state.userSettings.currency}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { showNameDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar nombre")
                        }
                    }
                }
            }

            // Preferencias Financieras
            item {
                Text("Preferencias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Moneda Principal") },
                            supportingContent = { Text(state.userSettings.currency) },
                            leadingContent = { Icon(Icons.Default.CurrencyExchange, contentDescription = null) },
                            modifier = Modifier.clickable { showCurrencyDialog = true }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Modo Oscuro") },
                            leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                            trailingContent = {
                                Switch(
                                    checked = state.userSettings.isDarkMode == true,
                                    onCheckedChange = { onUpdateSettings(state.userSettings.copy(isDarkMode = it)) }
                                )
                            }
                        )
                    }
                }
            }

            // Seguridad
            item {
                Text("Seguridad y Bloqueo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Bloqueo por PIN (4 dígitos)") },
                            supportingContent = { Text(if (state.userSettings.isPinEnabled) "Activo" else "Desactivado") },
                            leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingContent = {
                                Switch(
                                    checked = state.userSettings.isPinEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled && state.userSettings.pinCode.isEmpty()) {
                                            showPinDialog = true
                                        } else {
                                            onUpdateSettings(state.userSettings.copy(isPinEnabled = enabled))
                                        }
                                    }
                                )
                            }
                        )
                        if (state.userSettings.isPinEnabled) {
                            HorizontalDivider()
                            ListItem(
                                headlineContent = { Text("Cambiar PIN de seguridad") },
                                leadingContent = { Icon(Icons.Default.Pin, contentDescription = null) },
                                modifier = Modifier.clickable { showPinDialog = true }
                            )
                            HorizontalDivider()
                            ListItem(
                                headlineContent = { Text("Bloquear aplicación ahora") },
                                leadingContent = { Icon(Icons.Default.Security, contentDescription = null) },
                                modifier = Modifier.clickable { onLockApp() }
                            )
                        }
                    }
                }
            }

            // Datos y Demostración
            item {
                Text("Gestión de Datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Cargar Datos de Demostración") },
                            supportingContent = { Text("Puebla la app con ejemplos de ingresos, gastos y metas.") },
                            leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                            modifier = Modifier.clickable { onLoadDemoData() }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Borrar todos los datos", color = MaterialTheme.colorScheme.error) },
                            supportingContent = { Text("Restablece todas las transacciones y presupuestos.") },
                            leadingContent = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            modifier = Modifier.clickable { onClearAllData() }
                        )
                    }
                }
            }
        }
    }

    // Currency selector dialog
    if (showCurrencyDialog) {
        val currencies = listOf("COP", "USD", "EUR", "MXN")
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Seleccionar Moneda") },
            text = {
                Column {
                    currencies.forEach { cur ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onUpdateSettings(state.userSettings.copy(currency = cur))
                                showCurrencyDialog = false
                            }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = state.userSettings.currency == cur, onClick = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(cur, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCurrencyDialog = false }) { Text("Cerrar") } }
        )
    }

    // Name dialog
    if (showNameDialog) {
        var nameInput by remember { mutableStateOf(state.userSettings.userName) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Tu Nombre") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            onUpdateSettings(state.userSettings.copy(userName = nameInput))
                            showNameDialog = false
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text("Cancelar") } }
        )
    }

    // PIN Dialog
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Configurar PIN (4 dígitos)") },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                    label = { Text("PIN de 4 dígitos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            onUpdateSettings(state.userSettings.copy(isPinEnabled = true, pinCode = pinInput))
                            showPinDialog = false
                        }
                    },
                    enabled = pinInput.length == 4
                ) { Text("Activar PIN") }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false }) { Text("Cancelar") } }
        )
    }
}

// ==========================================
// 9. ONBOARDING (SECTION 18)
// ==========================================
@Composable
fun OnboardingDialog(
    currency: String,
    onComplete: (approxIncome: Double, approxExpense: Double, savingsTarget: Double, hasDebt: Boolean, hasFuture: Boolean) -> Unit,
    onSkip: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var incomeText by remember { mutableStateOf("") }
    var expenseText by remember { mutableStateOf("") }
    var savingsTargetText by remember { mutableStateOf("") }
    var hasDebt by remember { mutableStateOf(false) }
    var hasFuture by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onSkip,
        title = {
            Text(
                text = when (step) {
                    0 -> "¡Bienvenido a Finara! 👋"
                    1 -> "Tus Ingresos Mensuales 💰"
                    2 -> "Tus Gastos Habituales 🛒"
                    3 -> "Tus Metas de Ahorro 🎯"
                    else -> "Compromisos Financieros 📅"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (step) {
                    0 -> {
                        Text("Configura tu perfil en 3 sencillos pasos para tener una visión clara de tus finanzas desde hoy.")
                    }
                    1 -> {
                        Text("¿Cuál es tu ingreso mensual aproximado?")
                        OutlinedTextField(
                            value = incomeText,
                            onValueChange = { if (it.all { c -> c.isDigit() }) incomeText = it },
                            label = { Text("Ingreso ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    2 -> {
                        Text("¿Cuánto calculas que gastas en un mes típico?")
                        OutlinedTextField(
                            value = expenseText,
                            onValueChange = { if (it.all { c -> c.isDigit() }) expenseText = it },
                            label = { Text("Gasto mensual ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    3 -> {
                        Text("¿Tienes alguna meta de ahorro en mente?")
                        OutlinedTextField(
                            value = savingsTargetText,
                            onValueChange = { if (it.all { c -> c.isDigit() }) savingsTargetText = it },
                            label = { Text("Monto meta de ahorro ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    4 -> {
                        Text("¿Tienes deudas activas o gastos grandes próximos?")
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tengo deudas o créditos")
                            Switch(checked = hasDebt, onCheckedChange = { hasDebt = it })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tengo pagos futuros programados")
                            Switch(checked = hasFuture, onCheckedChange = { hasFuture = it })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step < 4) {
                        step++
                    } else {
                        val inc = incomeText.toDoubleOrNull() ?: 0.0
                        val exp = expenseText.toDoubleOrNull() ?: 0.0
                        val sav = savingsTargetText.toDoubleOrNull() ?: 0.0
                        onComplete(inc, exp, sav, hasDebt, hasFuture)
                    }
                }
            ) {
                Text(if (step < 4) "Siguiente" else "Finalizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("Omitir")
            }
        }
    )
}

// ==========================================
// 10. BLOQUEO POR PIN (SECTION 20)
// ==========================================
@Composable
fun PinLockScreen(
    onUnlock: (pin: String) -> Boolean
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize().testTag("pin_lock_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ingresa tu PIN de seguridad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            // 4 Pin dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until 4) {
                    val isFilled = enteredPin.length > i
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                CircleShape
                            )
                    )
                }
            }

            if (isError) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("PIN incorrecto. Intenta de nuevo.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Number Pad Grid (1-9, 0, backspace)
            val pad = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
            pad.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(64.dp))
                        } else {
                            IconButton(
                                onClick = {
                                    if (key == "⌫") {
                                        if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                        isError = false
                                    } else if (enteredPin.length < 4) {
                                        enteredPin += key
                                        if (enteredPin.length == 4) {
                                            val ok = onUnlock(enteredPin)
                                            if (!ok) {
                                                isError = true
                                                enteredPin = ""
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Text(key, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
