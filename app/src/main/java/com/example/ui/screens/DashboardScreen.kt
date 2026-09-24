package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.example.ui.FinanceUiState
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.IncomeVsExpenseBarChart
import com.example.ui.components.SimpleProgressBar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    state: FinanceUiState,
    onNavigateToProfile: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToAntExpenses: () -> Unit,
    onNavigateToSavings: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToFutureExpenses: () -> Unit,
    onOpenQuickAdd: () -> Unit
) {
    val currency = state.userSettings.currency
    var balancesHidden by remember { mutableStateOf(state.userSettings.hideBalancesDefault) }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Buenos días,"
        in 12..18 -> "Buenas tardes,"
        else -> "Buenas noches,"
    }

    val initials = state.userSettings.userName
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifEmpty { "SG" }

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val currentTime = remember { timeFormat.format(Date()).lowercase() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .padding(horizontal = 20.dp)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header: Greeting + Avatar to Profile (Dashboard con acceso al perfil.png)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = state.userSettings.userName.ifEmpty { "Usuario" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                // Circular avatar button -> opens Mi Perfil
                Surface(
                    onClick = onNavigateToProfile,
                    shape = CircleShape,
                    color = MintLight,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MintMedium),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Hero Card: Saldo Disponible (Dashboard.png & Dashboard saldos ocultos.png)
        item {
            val isNegativeBalance = state.availableBalance < 0.0
            val heroCardColor = if (isNegativeBalance) Color(0xFFDC2626) else ForestGreen
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = heroCardColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card")
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Saldo disponible",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        IconButton(
                            onClick = { balancesHidden = !balancesHidden },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                if (balancesHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (balancesHidden) "Mostrar saldos" else "Ocultar saldos",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (balancesHidden) {
                        Text(
                            text = "● ● ● ● ● ● ● ●",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 3.sp,
                            modifier = Modifier.testTag("dashboard_available_balance_hidden")
                        )
                    } else {
                        Text(
                            text = FinanceRepository.formatCurrency(state.availableBalance, currency),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("dashboard_available_balance")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (balancesHidden)
                            "Oculto por tu configuración · hoy, $currentTime"
                        else
                            "Actualizado hoy, $currentTime",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Resumen del mes (Ingresos / Gastos cards)
        item {
            Column {
                Text(
                    text = "Resumen del mes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Ingresos Card
                    Surface(
                        onClick = onNavigateToTransactions,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MintLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Ingresos",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (balancesHidden) "••••••" else FinanceRepository.formatCurrency(state.monthIncome, currency),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    // Gastos Card
                    Surface(
                        onClick = onNavigateToTransactions,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CrimsonLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Gastos",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (balancesHidden) "••••••" else FinanceRepository.formatCurrency(state.monthExpense, currency),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        }

        // Actividad reciente (Recent Transactions matching Dashboard.png)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Actividad reciente",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text(
                            text = "Ver todo",
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay movimientos registrados",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Column {
                            state.transactions.take(4).forEachIndexed { index, tx ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToTransactions() }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isExpense = tx.type == "EXPENSE"
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(if (isExpense) CrimsonLight else MintLight, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (isExpense) Icons.Default.ShoppingCart else Icons.Default.AttachMoney,
                                            contentDescription = null,
                                            tint = if (isExpense) CrimsonRed else ForestGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tx.title.ifEmpty { tx.category },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = tx.category,
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Text(
                                        text = if (balancesHidden) "••••" else {
                                            val sign = if (isExpense) "-" else "+"
                                            "$sign${FinanceRepository.formatCurrency(tx.amount, currency)}"
                                        },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExpense) CrimsonRed else ForestGreen
                                    )
                                }
                                if (index < state.transactions.take(4).lastIndex) {
                                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Resumen de hoy (Gastos de hoy + Gastos hormiga)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(MintLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Today, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Resumen de hoy",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Text(
                            text = if (balancesHidden) "••••" else FinanceRepository.formatCurrency(state.todaySpent, currency),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonRed
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAntExpenses() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gastos hormiga hoy", fontSize = 13.sp, color = Color(0xFF64748B))
                        }
                        Text(
                            text = if (balancesHidden) "••••" else FinanceRepository.formatCurrency(state.todayAntExpenses, currency),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }
        }

        // Metas de Ahorro
        if (state.savingsGoalDetails.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Metas de ahorro",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            TextButton(onClick = onNavigateToSavings) {
                                Text("Ver todas", color = ForestGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        state.savingsGoalDetails.take(2).forEach { detail ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(detail.goal.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                    Text("${detail.progressPercent.roundToInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                SimpleProgressBar(
                                    progress = detail.progressPercent / 100f,
                                    color = ForestGreen,
                                    height = 7.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Metas de ahorro Card / Link
        item {
            Surface(
                onClick = onNavigateToSavings,
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MintLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = ForestGreen)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Metas de ahorro", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Define objetivos con valor personalizado y ahorra", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

