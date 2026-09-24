package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FinanceRepository
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.ReportPeriod
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.IncomeVsExpenseBarChart
import com.example.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    state: FinanceUiState,
    onSelectPeriod: (ReportPeriod) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val currency = state.userSettings.currency
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes Financieros", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar y compartir")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("reports_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ReportPeriod.values()) { period ->
                        FilterChip(
                            selected = state.selectedReportPeriod == period,
                            onClick = { onSelectPeriod(period) },
                            label = { Text(period.label) }
                        )
                    }
                }
            }

            // Financial Summary Card
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
                                text = "Resumen del Período",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = if (state.reportBalance >= 0) FinanceGreenLight else FinanceRedLight,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Balance: ${FinanceRepository.formatCurrency(state.reportBalance, currency)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.reportBalance >= 0) FinanceGreenDark else FinanceRedDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2x3 Grid of KPIs
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ReportMetricCell(
                                title = "Ingresos",
                                amount = FinanceRepository.formatCurrency(state.reportIncome, currency),
                                color = FinanceGreen,
                                modifier = Modifier.weight(1f)
                            )
                            ReportMetricCell(
                                title = "Gastos",
                                amount = FinanceRepository.formatCurrency(state.reportExpense, currency),
                                color = FinanceRed,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ReportMetricCell(
                                title = "Ahorros",
                                amount = FinanceRepository.formatCurrency(state.reportSavings, currency),
                                color = FinanceTeal,
                                modifier = Modifier.weight(1f)
                            )
                            ReportMetricCell(
                                title = "Deudas pagadas",
                                amount = FinanceRepository.formatCurrency(state.reportDebtsPaid, currency),
                                color = FinanceAmberDark,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            ReportMetricCell(
                                title = "Gastos hormiga del período",
                                amount = FinanceRepository.formatCurrency(state.reportAntExpenses, currency),
                                color = FinanceIndigo,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Period Comparison Card
            state.periodComparison?.let { comp ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Comparativa: ${comp.currentPeriodLabel} vs. ${comp.previousPeriodLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ComparisonBadge(
                                    label = "Gastos",
                                    percent = comp.expenseChangePercent,
                                    isGoodWhenLower = true
                                )
                                ComparisonBadge(
                                    label = "Ingresos",
                                    percent = comp.incomeChangePercent,
                                    isGoodWhenLower = false
                                )
                                ComparisonBadge(
                                    label = "Ahorros",
                                    percent = comp.savingsChangePercent,
                                    isGoodWhenLower = false
                                )
                            }
                        }
                    }
                }
            }

            // Ingresos vs. Gastos Bar Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Comparativa Gráfica (${state.selectedReportPeriod.label})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        IncomeVsExpenseBarChart(
                            income = state.reportIncome,
                            expense = state.reportExpense,
                            currency = currency
                        )
                    }
                }
            }

            // Gastos por Categoría Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Distribución de Gastos por Categoría",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CategoryDonutChart(
                            categories = state.reportCategoryExpenses,
                            currency = currency
                        )
                    }
                }
            }

            // Quick Export Action Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Exportar y Compartir", fontWeight = FontWeight.Bold)
                            Text("Genera tu reporte en texto o archivo CSV para Excel.", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { showExportDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exportar")
                        }
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Reporte Financiero") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Selecciona cómo deseas compartir tus datos:")
                    OutlinedButton(
                        onClick = {
                            val text = FinanceViewModel.generateTextReport(state)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Reporte Financiero Personal")
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir Reporte"))
                            showExportDialog = false
                            onShowSnackbar("Abriendo opciones para compartir...")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compartir Reporte de Texto")
                    }

                    OutlinedButton(
                        onClick = {
                            val csv = FinanceViewModel.generateCsvReport(state)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "Transacciones_Finara.csv")
                                putExtra(Intent.EXTRA_TEXT, csv)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir CSV / Excel"))
                            showExportDialog = false
                            onShowSnackbar("Abriendo opciones para compartir CSV...")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar como CSV (Excel)")
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Reporte Financiero", FinanceViewModel.generateTextReport(state))
                            clipboard.setPrimaryClip(clip)
                            showExportDialog = false
                            onShowSnackbar("Reporte copiado al portapapeles.")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copiar al portapapeles")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun ReportMetricCell(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = amount, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun ComparisonBadge(
    label: String,
    percent: Double,
    isGoodWhenLower: Boolean
) {
    val isPositive = percent > 0
    val isGood = if (isGoodWhenLower) percent <= 0 else percent >= 0
    val badgeColor = if (isGood) FinanceGreen else FinanceRed
    val badgeBg = if (isGood) FinanceGreenLight else FinanceRedLight
    val sign = if (percent > 0) "+" else ""

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = badgeBg,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "$sign${percent.roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
