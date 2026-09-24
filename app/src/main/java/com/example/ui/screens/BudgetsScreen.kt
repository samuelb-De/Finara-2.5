package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BudgetEntity
import com.example.data.FinanceRepository
import com.example.ui.BudgetStatus
import com.example.ui.FinanceUiState
import com.example.ui.components.CategoryIcon
import com.example.ui.components.IconHelper
import com.example.ui.components.SimpleProgressBar
import com.example.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    state: FinanceUiState,
    onSetBudget: (category: String, monthlyLimit: Double) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit
) {
    val currency = state.userSettings.currency
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetEntity?>(null) }
    var deletingBudget by remember { mutableStateOf<BudgetEntity?>(null) }

    val totalBudget = state.budgetStatuses.sumOf { it.budget.monthlyLimit }
    val totalSpent = state.budgetStatuses.sumOf { it.spent }
    val totalRemaining = (totalBudget - totalSpent).coerceAtLeast(0.0)
    val overallPercent = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Presupuesto", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo presupuesto")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear presupuesto")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("budgets_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Budget Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Presupuesto General del Mes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${overallPercent.roundToInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (overallPercent > 100f) FinanceRed else if (overallPercent >= 80f) FinanceAmberDark else FinanceGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        SimpleProgressBar(
                            progress = overallPercent / 100f,
                            color = if (overallPercent > 100f) FinanceRed else if (overallPercent >= 80f) FinanceAmberDark else FinanceGreen,
                            height = 10.dp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Límite Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FinanceRepository.formatCurrency(totalBudget, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Gastado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FinanceRepository.formatCurrency(totalSpent, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = FinanceRed)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Disponible", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FinanceRepository.formatCurrency(totalRemaining, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = FinanceGreen)
                            }
                        }
                    }
                }
            }

            // Category Budgets Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Presupuestos por Categoría",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.budgetStatuses.size} categorías",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (state.budgetStatuses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No tienes presupuestos configurados.", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Define límites mensuales para controlar tus gastos y recibir alertas automáticas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { showAddDialog = true }) {
                                Text("+ Agregar presupuesto")
                            }
                        }
                    }
                }
            } else {
                items(state.budgetStatuses, key = { it.budget.id }) { item ->
                    BudgetCategoryCard(
                        budgetStatus = item,
                        currency = currency,
                        onEdit = { editingBudget = item.budget },
                        onDelete = { deletingBudget = item.budget }
                    )
                }
            }
        }
    }

    // Add / Edit Budget Dialog
    if (showAddDialog || editingBudget != null) {
        val isEditing = editingBudget != null
        val availableCategories = state.categories
            .filter { it.isActive && it.name != "Salario / Ingresos" && it.name != "Ahorro" }
            .map { it.name }
            .ifEmpty {
                listOf("Alimentación", "Transporte", "Vivienda", "Servicios", "Salud", "Educación", "Entretenimiento", "Ropa", "Tecnología", "Deudas", "Otros")
            }
        val initialCat = editingBudget?.category ?: availableCategories.first()
        val initialLimit = editingBudget?.monthlyLimit?.toLong()?.toString() ?: ""

        AddEditBudgetDialog(
            isEditing = isEditing,
            categories = state.categories,
            initialCategory = initialCat,
            initialLimit = initialLimit,
            currency = currency,
            onDismiss = {
                showAddDialog = false
                editingBudget = null
            },
            onSave = { cat, limit ->
                onSetBudget(cat, limit)
                showAddDialog = false
                editingBudget = null
            }
        )
    }

    // Delete confirmation
    deletingBudget?.let { b ->
        AlertDialog(
            onDismissRequest = { deletingBudget = null },
            title = { Text("Eliminar presupuesto") },
            text = { Text("¿Deseas eliminar el límite de presupuesto para ${b.category}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBudget(b)
                        deletingBudget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingBudget = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun BudgetCategoryCard(
    budgetStatus: BudgetStatus,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val b = budgetStatus.budget
    val percent = budgetStatus.percentage
    val isExceeded = budgetStatus.isExceeded
    val isNearLimit = budgetStatus.isNearLimit

    val statusColor = when {
        isExceeded -> FinanceRed
        isNearLimit -> FinanceAmberDark
        else -> FinanceGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(statusColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconVector(b.category),
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = b.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            SimpleProgressBar(
                progress = percent / 100f,
                color = statusColor,
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gastado: ${FinanceRepository.formatCurrency(budgetStatus.spent, currency)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Límite: ${FinanceRepository.formatCurrency(b.monthlyLimit, currency)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Alert banner if >=80% or >100%
            if (isExceeded) {
                Surface(
                    color = FinanceRedLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = FinanceRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Has superado tu presupuesto en ${FinanceRepository.formatCurrency(budgetStatus.spent - b.monthlyLimit, currency)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FinanceRedDark
                        )
                    }
                }
            } else if (isNearLimit) {
                Surface(
                    color = FinanceAmberLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = FinanceAmberDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Has utilizado la mayor parte de tu presupuesto (${percent.roundToInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FinanceAmberDark
                        )
                    }
                }
            } else {
                Text(
                    text = "Disponible: ${FinanceRepository.formatCurrency(budgetStatus.remaining, currency)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FinanceGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AddEditBudgetDialog(
    isEditing: Boolean,
    categories: List<com.example.data.CategoryEntity>,
    initialCategory: String,
    initialLimit: String,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (category: String, limit: Double) -> Unit
) {
    val expenseCategories = remember(categories) {
        val list = categories
            .filter { it.isActive && it.name != "Salario / Ingresos" && it.name != "Ahorro" }
            .map { it.name }
        if (list.isNotEmpty()) list else listOf(
            "Alimentación", "Transporte", "Vivienda", "Servicios",
            "Salud", "Educación", "Entretenimiento", "Ropa",
            "Tecnología", "Deudas", "Otros"
        )
    }

    var selectedCat by remember {
        mutableStateOf(if (initialCategory in expenseCategories) initialCategory else expenseCategories.first())
    }
    var limitText by remember { mutableStateOf(initialLimit) }
    var isCustomCat by remember { mutableStateOf(false) }
    var customCatText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar Presupuesto" else "Nuevo Presupuesto", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (!isEditing) {
                    Text("Seleccionar categoría", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isCustomCat) "Otra categoría..." else selectedCat)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            expenseCategories.forEach { catName ->
                                DropdownMenuItem(
                                    text = { Text(catName) },
                                    onClick = {
                                        selectedCat = catName
                                        isCustomCat = false
                                        expanded = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("+ Escribir otra categoría...") },
                                onClick = {
                                    isCustomCat = true
                                    expanded = false
                                }
                            )
                        }
                    }

                    if (isCustomCat) {
                        OutlinedTextField(
                            value = customCatText,
                            onValueChange = { customCatText = it },
                            label = { Text("Nombre de la categoría") },
                            placeholder = { Text("Ej: Mascotas") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text("Categoría: $selectedCat", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) limitText = it },
                    label = { Text("Límite mensual ($currency) *") },
                    placeholder = { Text("Ej: 500000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull() ?: 0.0
                    val finalCategory = if (isCustomCat && customCatText.isNotBlank()) {
                        customCatText.trim()
                    } else {
                        selectedCat
                    }
                    if (limit > 0 && finalCategory.isNotBlank()) {
                        onSave(finalCategory, limit)
                    }
                },
                enabled = (limitText.toDoubleOrNull() ?: 0.0) > 0 && (!isCustomCat || customCatText.isNotBlank())
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
