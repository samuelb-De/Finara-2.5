package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FinanceRepository
import com.example.data.TransactionEntity
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.CategoryIcon
import com.example.ui.components.IconHelper
import com.example.ui.theme.*
import java.util.*

enum class TimeFilter(val label: String) {
    TODOS("Todos"),
    HOY("Hoy"),
    SEMANA("Esta semana"),
    MES("Este mes")
}

enum class TypeFilter(val label: String) {
    TODOS("Todos"),
    GASTOS("Gastos"),
    INGRESOS("Ingresos")
}

enum class SortOrder(val label: String) {
    FECHA_DESC("Más reciente"),
    FECHA_ASC("Más antiguo"),
    MONTO_DESC("Mayor monto"),
    MONTO_ASC("Menor monto")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    state: FinanceUiState,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onUpdateTransaction: (TransactionEntity) -> Unit,
    onOpenQuickAdd: () -> Unit = {},
    onAddExpense: () -> Unit = onOpenQuickAdd,
    onAddIncome: () -> Unit = onOpenQuickAdd
) {
    val currency = state.userSettings.currency
    var searchQuery by remember { mutableStateOf("") }
    var selectedTimeFilter by remember { mutableStateOf(TimeFilter.TODOS) }
    var selectedTypeFilter by remember { mutableStateOf(TypeFilter.TODOS) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var sortOrder by remember { mutableStateOf(SortOrder.FECHA_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var deletingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Filter logic
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis

    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
    val weekStart = calendar.timeInMillis

    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val monthStart = calendar.timeInMillis

    val filteredTransactions = state.transactions.filter { tx ->
        // Type filter
        val matchesType = when (selectedTypeFilter) {
            TypeFilter.TODOS -> true
            TypeFilter.GASTOS -> tx.type == "EXPENSE"
            TypeFilter.INGRESOS -> tx.type == "INCOME"
        }
        // Time filter
        val matchesTime = when (selectedTimeFilter) {
            TimeFilter.TODOS -> true
            TimeFilter.HOY -> tx.date >= todayStart
            TimeFilter.SEMANA -> tx.date >= weekStart
            TimeFilter.MES -> tx.date >= monthStart
        }
        // Category filter
        val matchesCategory = selectedCategoryFilter == null || tx.category.equals(selectedCategoryFilter, ignoreCase = true)
        // Search query
        val matchesSearch = searchQuery.isBlank() ||
                tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.notes.contains(searchQuery, ignoreCase = true)

        matchesType && matchesTime && matchesCategory && matchesSearch
    }.let { list ->
        when (sortOrder) {
            SortOrder.FECHA_DESC -> list.sortedByDescending { it.date }
            SortOrder.FECHA_ASC -> list.sortedBy { it.date }
            SortOrder.MONTO_DESC -> list.sortedByDescending { it.amount }
            SortOrder.MONTO_ASC -> list.sortedBy { it.amount }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Movimientos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = onAddExpense,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFFDECEB),
                                contentColor = Color(0xFFBD3A3A)
                            ),
                            modifier = Modifier.height(34.dp).testTag("quick_add_gasto_btn")
                        ) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gasto", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        FilledTonalButton(
                            onClick = onAddIncome,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFE8F3EE),
                                contentColor = Color(0xFF265341)
                            ),
                            modifier = Modifier.height(34.dp).testTag("quick_add_ingreso_btn")
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ingreso", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box {
                            IconButton(onClick = { showSortMenu = true }, modifier = Modifier.size(34.dp)) {
                                Icon(Icons.Default.Sort, contentDescription = "Ordenar")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortOrder.values().forEach { order ->
                                    DropdownMenuItem(
                                        text = { Text(order.label) },
                                        onClick = {
                                            sortOrder = order
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre, categoría o nota...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("search_transactions_input")
                )

                // Type Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TypeFilter.values().forEach { type ->
                        FilterChip(
                            selected = selectedTypeFilter == type,
                            onClick = { selectedTypeFilter = type },
                            label = { Text(type.label) }
                        )
                    }
                }

                // Time Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(TimeFilter.values()) { time ->
                        FilterChip(
                            selected = selectedTimeFilter == time,
                            onClick = { selectedTimeFilter = time },
                            label = { Text(time.label) }
                        )
                    }
                    if (selectedCategoryFilter != null) {
                        item {
                            SuggestionChip(
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("Cat: $selectedCategoryFilter ✕") }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No se encontraron movimientos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    when (selectedTypeFilter) {
                        TypeFilter.INGRESOS -> {
                            Button(
                                onClick = onAddIncome,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF265341)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Registrar ingreso")
                            }
                        }
                        TypeFilter.GASTOS -> {
                            Button(
                                onClick = onAddExpense,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD3A3A)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Registrar gasto")
                            }
                        }
                        TypeFilter.TODOS -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = onAddExpense,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD3A3A)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gasto")
                                }
                                Button(
                                    onClick = onAddIncome,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF265341)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ingreso")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .testTag("transactions_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        currency = currency,
                        onClick = { editingTransaction = tx },
                        onDelete = { deletingTransaction = tx }
                    )
                }
            }
        }
    }

    // Edit Transaction Dialog
    editingTransaction?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            currency = currency,
            categories = state.categories,
            onDismiss = { editingTransaction = null },
            onSave = { updated ->
                onUpdateTransaction(updated)
                editingTransaction = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingTransaction?.let { tx ->
        AlertDialog(
            onDismissRequest = { deletingTransaction = null },
            title = { Text("Eliminar movimiento") },
            text = { Text("¿Estás seguro de que deseas eliminar '${tx.title}' por ${FinanceRepository.formatCurrency(tx.amount, currency)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx)
                        deletingTransaction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTransaction = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    currency: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isExpense = transaction.type == "EXPENSE"
    val amountColor = if (isExpense) FinanceRed else FinanceGreen
    val amountPrefix = if (isExpense) "-" else "+"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Color(transaction.categoryColor).copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CategoryIcon(
                        iconName = transaction.categoryIcon,
                        color = Color(transaction.categoryColor),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (transaction.isAntExpense) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🐜",
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        text = "${transaction.category} • ${FinanceViewModel.formatDate(transaction.date)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$amountPrefix${FinanceRepository.formatCurrency(transaction.amount, currency)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    currency: String,
    categories: List<com.example.data.CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    var title by remember { mutableStateOf(transaction.title) }
    var amountText by remember { mutableStateOf(transaction.amount.toLong().toString()) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var isAnt by remember { mutableStateOf(transaction.isAntExpense) }
    var notes by remember { mutableStateOf(transaction.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Movimiento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                    label = { Text("Valor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (transaction.type == "EXPENSE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("¿Gasto hormiga?", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = isAnt, onCheckedChange = { isAnt = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amountText.toDoubleOrNull() ?: transaction.amount
                    onSave(
                        transaction.copy(
                            title = title,
                            amount = amountVal,
                            category = selectedCategory,
                            isAntExpense = isAnt,
                            notes = notes
                        )
                    )
                }
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
