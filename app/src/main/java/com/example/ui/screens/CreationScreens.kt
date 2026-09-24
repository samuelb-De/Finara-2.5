package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.ui.components.CustomCalendarSheet
import com.example.ui.components.IconHelper
import com.example.ui.components.NumberFormatHelper
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintLight
import java.text.SimpleDateFormat
import java.util.*

// Helper for circular back button as in screenshots
@Composable
fun CircularBackButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Regresar",
                tint = Color(0xFF1E293B),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ==========================================
// 1. NUEVO GASTO (Nuevo gasto.png)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoGastoScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onSave: (amount: Double, category: String, date: Long, paymentMethod: String, isAnt: Boolean, notes: String) -> Unit
) {
    val currency = state.userSettings.currency
    var amountText by remember { mutableStateOf("") }
    val expenseCategories = remember(state.categories) {
        val list = state.categories
            .filter { it.isActive && (it.categoryType == "EXPENSE" || (it.categoryType.isEmpty() && it.name != "Salario / Ingresos" && it.name != "Ahorro")) }
            .map { it.name }
        if (list.isNotEmpty()) list else listOf(
            "Alimentación", "Transporte", "Vivienda", "Servicios",
            "Salud", "Educación", "Entretenimiento", "Ropa",
            "Tecnología", "Deudas", "Otros"
        )
    }
    var selectedCategory by remember(expenseCategories) {
        mutableStateOf(expenseCategories.first())
    }
    var isCustomCategory by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var paymentMethod by remember { mutableStateOf("Efectivo") }
    var isAntExpense by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf("") }

    var showCalendar by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showPaymentMenu by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    if (showCalendar) {
        CustomCalendarSheet(
            initialDate = selectedDate,
            onDismiss = { showCalendar = false },
            onDateSelected = { selectedDate = it }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("nuevo_gasto_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Nuevo gasto",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campo: Valor
            Text("Valor", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$ ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = NumberFormatHelper.formatWithThousands(it) },
                        placeholder = { Text("0", fontSize = 22.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Campo: Categoría
            Text("Categoría", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showCategoryDialog = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth().testTag("select_expense_category")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val displayCat = if (isCustomCategory && customCategoryText.isNotBlank()) customCategoryText else selectedCategory
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MintLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconVector(displayCat),
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(displayCat, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    }
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                }
            }

            if (showCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showCategoryDialog = false },
                    title = { Text("Seleccionar categoría", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            expenseCategories.forEach { cat ->
                                val isSelected = !isCustomCategory && cat == selectedCategory
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCategory = cat
                                            isCustomCategory = false
                                            showCategoryDialog = false
                                        }
                                        .background(
                                            if (isSelected) MintLight else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (isSelected) ForestGreen.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = IconHelper.getIconVector(cat),
                                            contentDescription = null,
                                            tint = if (isSelected) ForestGreen else Color(0xFF64748B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = cat,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ForestGreen else Color(0xFF1E293B)
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.Check, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Text("O escribe otra categoría:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customCategoryText,
                                onValueChange = {
                                    customCategoryText = it
                                    if (it.isNotBlank()) isCustomCategory = true
                                },
                                placeholder = { Text("Ej: Mascotas") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showCategoryDialog = false }) {
                            Text("Aceptar", color = ForestGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Campo: Fecha
            Text("Fecha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showCalendar = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dateFormat.format(Date(selectedDate)), fontSize = 15.sp, color = Color(0xFF1E293B))
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Campo: Método de pago
            Text("Método de pago", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showPaymentMenu = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(paymentMethod, fontSize = 15.sp, color = Color(0xFF1E293B))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                }
            }
            DropdownMenu(
                expanded = showPaymentMenu,
                onDismissRequest = { showPaymentMenu = false }
            ) {
                listOf("Efectivo", "Tarjeta de débito", "Tarjeta de crédito", "Transferencia").forEach { pm ->
                    DropdownMenuItem(
                        text = { Text(pm) },
                        onClick = {
                            paymentMethod = pm
                            showPaymentMenu = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Checkbox: Marcar como gasto hormiga
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAntExpense = !isAntExpense }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = isAntExpense,
                    onCheckedChange = { isAntExpense = it },
                    colors = CheckboxDefaults.colors(checkedColor = ForestGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Marcar como gasto hormiga",
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Campo: Descripción opcional
            Text("Descripción opcional", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Escribe aquí...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón: Guardar gasto
            Button(
                onClick = {
                    val amount = NumberFormatHelper.parseToDouble(amountText)
                    val finalCat = if (isCustomCategory && customCategoryText.isNotBlank()) {
                        customCategoryText.trim()
                    } else {
                        selectedCategory
                    }
                    if (amount > 0 && finalCat.isNotBlank()) {
                        onSave(amount, finalCat, selectedDate, paymentMethod, isAntExpense, notesText)
                        onBack()
                    }
                },
                enabled = NumberFormatHelper.parseToDouble(amountText) > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Guardar gasto", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 2. NUEVO INGRESO (Nuevo ingreso.png)
// ==========================================
@Composable
fun NuevoIngresoScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onSave: (amount: Double, title: String, type: String, date: Long, frequency: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val incomeCategories = remember(state.categories) {
        val list = state.categories
            .filter { it.isActive && (it.categoryType == "INCOME" || it.name in listOf("Salario", "Salario / Ingresos", "Honorarios", "Honorarios / Freelance", "Negocio", "Negocio / Ventas", "Inversión", "Inversiones", "Regalo", "Regalos", "Ventas", "Freelance", "Otros ingresos")) }
            .map { it.name }
        if (list.isNotEmpty()) list else listOf("Salario", "Honorarios", "Negocio", "Inversión", "Regalo", "Otros")
    }
    var selectedCategory by remember(incomeCategories) {
        mutableStateOf(incomeCategories.first())
    }
    var isCustomCategory by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var frequency by remember { mutableStateOf("Único") }
    var notesText by remember { mutableStateOf("") }

    var showCalendar by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showFreqMenu by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    if (showCalendar) {
        CustomCalendarSheet(
            initialDate = selectedDate,
            onDismiss = { showCalendar = false },
            onDateSelected = { selectedDate = it }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("nuevo_ingreso_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Nuevo ingreso",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Valor", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$ ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = NumberFormatHelper.formatWithThousands(it) },
                        placeholder = { Text("0", fontSize = 22.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Tipo de ingreso / Categoría", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showCategoryDialog = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val displayCat = if (isCustomCategory && customCategoryText.isNotBlank()) customCategoryText else selectedCategory
                        val categoryObj = state.categories.find { it.name.equals(displayCat, ignoreCase = true) }
                        val iconVector = if (categoryObj != null) IconHelper.getIconVector(categoryObj.iconName) else IconHelper.getIconVector(displayCat)
                        val iconColor = if (categoryObj != null) Color(categoryObj.colorHex) else ForestGreen

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(iconColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(displayCat, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    }
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                }
            }

            if (showCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showCategoryDialog = false },
                    title = { Text("Seleccionar categoría de ingreso", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            incomeCategories.forEach { catName ->
                                val isSelected = !isCustomCategory && catName == selectedCategory
                                val catObj = state.categories.find { it.name.equals(catName, ignoreCase = true) }
                                val iconVector = if (catObj != null) IconHelper.getIconVector(catObj.iconName) else IconHelper.getIconVector(catName)
                                val iconColor = if (catObj != null) Color(catObj.colorHex) else ForestGreen

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCategory = catName
                                            isCustomCategory = false
                                            showCategoryDialog = false
                                        }
                                        .background(
                                            if (isSelected) MintLight else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (isSelected) iconColor.copy(alpha = 0.2f) else Color(0xFFF1F5F9),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = null,
                                            tint = if (isSelected) iconColor else Color(0xFF64748B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = catName,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ForestGreen else Color(0xFF1E293B)
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.Check, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Text("O escribe otra categoría:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customCategoryText,
                                onValueChange = {
                                    customCategoryText = it
                                    if (it.isNotBlank()) isCustomCategory = true
                                },
                                placeholder = { Text("Ej: Freelance") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (isCustomCategory && customCategoryText.isNotBlank()) {
                                    showCategoryDialog = false
                                } else {
                                    showCategoryDialog = false
                                }
                            }
                        ) {
                            Text("Aceptar", color = ForestGreen, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCategoryDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Fecha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showCalendar = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dateFormat.format(Date(selectedDate)), fontSize = 15.sp, color = Color(0xFF1E293B))
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Frecuencia", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showFreqMenu = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(frequency, fontSize = 15.sp, color = Color(0xFF1E293B))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                }
            }
            DropdownMenu(expanded = showFreqMenu, onDismissRequest = { showFreqMenu = false }) {
                listOf("Único", "Diario", "Semanal", "Quincenal", "Mensual").forEach { f ->
                    DropdownMenuItem(text = { Text(f) }, onClick = { frequency = f; showFreqMenu = false })
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Descripción opcional", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Escribe aquí...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val amount = NumberFormatHelper.parseToDouble(amountText)
                    if (amount > 0) {
                        val finalCategory = if (isCustomCategory && customCategoryText.isNotBlank()) customCategoryText.trim() else selectedCategory
                        onSave(amount, finalCategory, finalCategory, selectedDate, frequency, notesText)
                        onBack()
                    }
                },
                enabled = NumberFormatHelper.parseToDouble(amountText) > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("Guardar ingreso", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 3. AGREGAR AHORRO (Agregar ahorro.png)
// ==========================================
@Composable
fun AgregarAhorroScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onSave: (goalId: Long, goalTitle: String, targetAmount: Double, amount: Double, date: Long, note: String, targetDate: Long) -> Unit
) {
    var isCreatingNewGoal by remember {
        mutableStateOf(state.savingsGoals.isEmpty())
    }
    var selectedGoalId by remember {
        mutableStateOf(state.savingsGoals.firstOrNull()?.id ?: 0L)
    }
    var newGoalTitle by remember {
        mutableStateOf(state.savingsGoals.firstOrNull()?.title ?: "Fondo de emergencia")
    }
    var customTargetText by remember { mutableStateOf("1.000.000") }
    var isCustomAmountMode by remember { mutableStateOf(false) }

    // Plazo objetivo de tiempo para nueva meta
    var monthsAhead by remember { mutableStateOf<Int?>(6) }
    var isCustomDeadlineMode by remember { mutableStateOf(false) }
    var customMonthsText by remember { mutableStateOf("6") }
    var customExactTargetDate by remember { mutableStateOf(System.currentTimeMillis() + 180L * 24 * 3600 * 1000) }
    var useExactCalendarTargetDate by remember { mutableStateOf(false) }
    var showTargetCalendarPicker by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var noteText by remember { mutableStateOf("") }

    var showCalendar by remember { mutableStateOf(false) }
    var showGoalMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val targetDateFormat = remember { SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "CO")) }

    val suggestedTitles = listOf("Fondo de emergencia", "Vacaciones", "Auto / Moto", "Estudios", "Vivienda")
    val presetAmounts = listOf(
        500000.0 to "500.000",
        1000000.0 to "1.000.000",
        2000000.0 to "2.000.000",
        5000000.0 to "5.000.000"
    )

    val effectiveTargetDate = if (isCustomDeadlineMode && useExactCalendarTargetDate) {
        customExactTargetDate
    } else {
        val months = (if (isCustomDeadlineMode) customMonthsText.toIntOrNull() else monthsAhead) ?: 6
        selectedDate + (months.coerceAtLeast(1) * 30L * 24 * 3600 * 1000)
    }

    if (showCalendar) {
        CustomCalendarSheet(
            initialDate = selectedDate,
            onDismiss = { showCalendar = false },
            onDateSelected = { selectedDate = it }
        )
    }

    if (showTargetCalendarPicker) {
        CustomCalendarSheet(
            initialDate = customExactTargetDate,
            onDismiss = { showTargetCalendarPicker = false },
            onDateSelected = {
                customExactTargetDate = it
                useExactCalendarTargetDate = true
                showTargetCalendarPicker = false
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("agregar_ahorro_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Agregar ahorro",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Selector si ya existen metas: Meta existente vs Nueva meta
            if (state.savingsGoals.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isCreatingNewGoal,
                        onClick = { isCreatingNewGoal = false },
                        label = { Text("Meta existente") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isCreatingNewGoal,
                        onClick = { isCreatingNewGoal = true },
                        label = { Text("+ Nueva meta") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!isCreatingNewGoal && state.savingsGoals.isNotEmpty()) {
                Text("Meta de ahorro objetivo", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    onClick = { showGoalMenu = true },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentGoal = state.savingsGoals.find { it.id == selectedGoalId }
                        Column {
                            Text(currentGoal?.title ?: newGoalTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            if (currentGoal != null) {
                                Text(
                                    "Objetivo: ${NumberFormatHelper.formatCurrency(currentGoal.targetAmount, state.userSettings.currency)}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                    }
                }
                DropdownMenu(expanded = showGoalMenu, onDismissRequest = { showGoalMenu = false }) {
                    state.savingsGoals.forEach { g ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(g.title, fontWeight = FontWeight.SemiBold)
                                    Text("Meta: ${NumberFormatHelper.formatCurrency(g.targetAmount, state.userSettings.currency)}", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            },
                            onClick = {
                                selectedGoalId = g.id
                                newGoalTitle = g.title
                                showGoalMenu = false
                            }
                        )
                    }
                }
            } else {
                // Modo: Nueva meta de ahorro con valor personalizado
                Text("Nombre de la nueva meta *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(suggestedTitles) { idea ->
                        SuggestionChip(
                            onClick = { newGoalTitle = idea },
                            label = { Text(idea, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newGoalTitle,
                        onValueChange = { newGoalTitle = it },
                        placeholder = { Text("Ej. Fondo de emergencia", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Valor objetivo de la nueva meta (Personalizado)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Valor de la meta (Monto objetivo) *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                    if (isCustomAmountMode) {
                        Text("Personalizado", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetAmounts.forEach { (amt, label) ->
                        val isSelected = !isCustomAmountMode && NumberFormatHelper.parseToDouble(customTargetText) == amt
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                isCustomAmountMode = false
                                customTargetText = NumberFormatHelper.formatWithThousands(label)
                            },
                            label = { Text("$ $label", fontSize = 11.sp) }
                        )
                    }
                    FilterChip(
                        selected = isCustomAmountMode,
                        onClick = { isCustomAmountMode = true },
                        label = { Text("Personalizado", fontSize = 11.sp) }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$ ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                        OutlinedTextField(
                            value = customTargetText,
                            onValueChange = {
                                isCustomAmountMode = true
                                customTargetText = NumberFormatHelper.formatWithThousands(it)
                            },
                            placeholder = { Text("Valor personalizado de la meta", color = Color(0xFF94A3B8)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Plazo objetivo (tiempo) para la nueva meta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Plazo objetivo (tiempo) *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                    if (isCustomDeadlineMode) {
                        Text("Tiempo personalizado", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(3 to "3 meses", 6 to "6 meses", 12 to "1 año", 24 to "2 años").forEach { (m, label) ->
                        FilterChip(
                            selected = !isCustomDeadlineMode && monthsAhead == m,
                            onClick = {
                                isCustomDeadlineMode = false
                                useExactCalendarTargetDate = false
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = !useExactCalendarTargetDate,
                                    onClick = { useExactCalendarTargetDate = false },
                                    label = { Text("Por meses", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = useExactCalendarTargetDate,
                                    onClick = {
                                        useExactCalendarTargetDate = true
                                        showTargetCalendarPicker = true
                                    },
                                    label = { Text("Elegir fecha", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (!useExactCalendarTargetDate) {
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
                                    onClick = { showTargetCalendarPicker = true },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
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
                                                targetDateFormat.format(Date(customExactTargetDate)),
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

                Spacer(modifier = Modifier.height(6.dp))
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
                            text = "Meta proyectada para: ${targetDateFormat.format(Date(effectiveTargetDate))}",
                            fontSize = 11.sp,
                            color = ForestGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Monto a abonar hoy (${state.userSettings.currency}) *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$ ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = NumberFormatHelper.formatWithThousands(it) },
                        placeholder = { Text("0", fontSize = 22.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Fecha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showCalendar = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dateFormat.format(Date(selectedDate)), fontSize = 15.sp, color = Color(0xFF1E293B))
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Nota opcional", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Escribe aquí...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val currentAmount = NumberFormatHelper.parseToDouble(amountText)
            val parsedTarget = NumberFormatHelper.parseToDouble(customTargetText)
            val isValid = currentAmount > 0 && (!isCreatingNewGoal || (newGoalTitle.isNotBlank() && parsedTarget > 0))

            Button(
                onClick = {
                    if (isValid) {
                        val targetAmount = if (isCreatingNewGoal) parsedTarget else 0.0
                        val goalId = if (isCreatingNewGoal) 0L else selectedGoalId
                        onSave(goalId, newGoalTitle.trim(), targetAmount, currentAmount, selectedDate, noteText, effectiveTargetDate)
                        onBack()
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text(
                    text = if (isCreatingNewGoal) "Crear meta y guardar ahorro" else "Guardar ahorro",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// 4. NUEVO GASTO FUTURO (Nuevo gasto futuro.png)
// ==========================================
@Composable
fun NuevoGastoFuturoScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onSave: (title: String, amount: Double, dueDate: Long, category: String, priority: String, repeat: String) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis() + 30L * 24 * 3600 * 1000) }
    var selectedCategory by remember {
        mutableStateOf(state.categories.firstOrNull { it.isActive }?.name ?: "Vivienda")
    }
    var priority by remember { mutableStateOf("Media") }
    var repeat by remember { mutableStateOf("No se repite") }

    var showCalendar by remember { mutableStateOf(false) }
    var showCatMenu by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    var showRepeatMenu by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    if (showCalendar) {
        CustomCalendarSheet(
            initialDate = selectedDate,
            onDismiss = { showCalendar = false },
            onDateSelected = { selectedDate = it }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("nuevo_gasto_futuro_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Nuevo gasto futuro",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Nombre", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    placeholder = { Text("Ej. Seguro del auto", color = Color(0xFF94A3B8)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Valor estimado", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$ ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = NumberFormatHelper.formatWithThousands(it) },
                        placeholder = { Text("0", fontSize = 22.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Fecha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showCalendar = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dateFormat.format(Date(selectedDate)), fontSize = 15.sp, color = Color(0xFF1E293B))
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Categoría", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showCatMenu = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedCategory, fontSize = 15.sp, color = Color(0xFF1E293B))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                }
            }
            DropdownMenu(expanded = showCatMenu, onDismissRequest = { showCatMenu = false }) {
                state.categories.filter { it.isActive }.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategory = cat.name; showCatMenu = false })
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Prioridad", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showPriorityMenu = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(priority, fontSize = 15.sp, color = Color(0xFF1E293B))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                }
            }
            DropdownMenu(expanded = showPriorityMenu, onDismissRequest = { showPriorityMenu = false }) {
                listOf("Alta", "Media", "Baja").forEach { p ->
                    DropdownMenuItem(text = { Text(p) }, onClick = { priority = p; showPriorityMenu = false })
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Repetición", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showRepeatMenu = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(repeat, fontSize = 15.sp, color = Color(0xFF1E293B))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                }
            }
            DropdownMenu(expanded = showRepeatMenu, onDismissRequest = { showRepeatMenu = false }) {
                listOf("No se repite", "Mensual", "Anual").forEach { r ->
                    DropdownMenuItem(text = { Text(r) }, onClick = { repeat = r; showRepeatMenu = false })
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val amount = NumberFormatHelper.parseToDouble(amountText)
                    val finalPriority = when (priority) {
                        "Alta" -> "ALTA"
                        "Baja" -> "BAJA"
                        else -> "MEDIA"
                    }
                    val finalRepeat = when (repeat) {
                        "Mensual" -> "Mensual"
                        "Anual" -> "Anual"
                        else -> "Único"
                    }
                    if (nameText.isNotBlank() && amount > 0) {
                        onSave(nameText, amount, selectedDate, selectedCategory, finalPriority, finalRepeat)
                        onBack()
                    }
                },
                enabled = nameText.isNotBlank() && NumberFormatHelper.parseToDouble(amountText) > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("Guardar gasto futuro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 5. REGISTRAR PAGO (Registrar pago.png)
// ==========================================
@Composable
fun RegistrarPagoScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onOpenCreateDebt: () -> Unit = {},
    onSave: (debtId: Long, debtTitle: String, amount: Double, date: Long, note: String) -> Unit
) {
    var selectedDebtId by remember {
        mutableStateOf(state.debts.firstOrNull()?.id ?: 0L)
    }
    var debtTitle by remember {
        mutableStateOf(state.debts.firstOrNull()?.title ?: "")
    }
    var amountText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var noteText by remember { mutableStateOf("") }

    var showCalendar by remember { mutableStateOf(false) }
    var showDebtMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    if (showCalendar) {
        CustomCalendarSheet(
            initialDate = selectedDate,
            onDismiss = { showCalendar = false },
            onDateSelected = { selectedDate = it }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("registrar_pago_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Registrar pago",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.debts.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            "No tienes deudas registradas",
                            fontWeight = FontWeight.Bold,
                            color = CrimsonRed,
                            fontSize = 16.sp
                        )
                        Text(
                            "Para registrar un abono o pago debes crear una deuda primero.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onOpenCreateDebt,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Crear una deuda nueva", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text("Deuda", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            if (state.debts.isNotEmpty()) {
                Surface(
                    onClick = { showDebtMenu = true },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(debtTitle, fontSize = 15.sp, color = Color(0xFF1E293B))
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                    }
                }
                DropdownMenu(expanded = showDebtMenu, onDismissRequest = { showDebtMenu = false }) {
                    state.debts.forEach { d ->
                        DropdownMenuItem(
                            text = { Text("${d.title} (Saldo: $${d.remainingBalance.toLong()})") },
                            onClick = {
                                selectedDebtId = d.id
                                debtTitle = d.title
                                amountText = NumberFormatHelper.formatWithThousands(d.installmentAmount.toLong().toString())
                                showDebtMenu = false
                            }
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = debtTitle,
                        onValueChange = { debtTitle = it },
                        placeholder = { Text("Ej. Crédito de libre inversión", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Valor del pago", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$ ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = NumberFormatHelper.formatWithThousands(it) },
                        placeholder = { Text("0", fontSize = 22.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Fecha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showCalendar = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dateFormat.format(Date(selectedDate)), fontSize = 15.sp, color = Color(0xFF1E293B))
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Nota opcional", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Escribe aquí...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val amount = NumberFormatHelper.parseToDouble(amountText)
                    if (amount > 0) {
                        onSave(selectedDebtId, debtTitle, amount, selectedDate, noteText)
                        onBack()
                    }
                },
                enabled = NumberFormatHelper.parseToDouble(amountText) > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("Registrar pago", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
