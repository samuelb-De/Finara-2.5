package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FinanceRepository
import com.example.ui.CategoryExpenseSummary
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed
import kotlin.math.roundToInt

@Composable
fun CategoryDonutChart(
    categories: List<CategoryExpenseSummary>,
    currency: String,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 26.dp
) {
    if (categories.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sin gastos registrados en el período",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val total = categories.sumOf { it.amount }
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "donut_anim"
    )

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(170.dp)) {
                var startAngle = -90f
                val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                val diameter = size.minDimension - strokeWidth.toPx()
                val topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)
                val arcSize = Size(diameter, diameter)

                categories.forEach { cat ->
                    val sweep = (cat.percentage / 100f) * 360f * animatedProgress
                    if (sweep > 0.5f) {
                        drawArc(
                            color = Color(cat.colorHex),
                            startAngle = startAngle,
                            sweepAngle = sweep - 2f, // slight gap
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke
                        )
                    }
                    startAngle += sweep
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = FinanceRepository.formatCurrency(total, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(5).forEach { cat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(cat.colorHex), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cat.category,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${cat.percentage.roundToInt()}% (${FinanceRepository.formatCurrency(cat.amount, currency)})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeVsExpenseBarChart(
    income: Double,
    expense: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    val maxVal = maxOf(income, expense, 1.0)
    val incomeFraction = (income / maxVal).toFloat()
    val expenseFraction = (expense / maxVal).toFloat()

    val animatedIncome by animateFloatAsState(
        targetValue = incomeFraction,
        animationSpec = tween(durationMillis = 700),
        label = "income_bar"
    )
    val animatedExpense by animateFloatAsState(
        targetValue = expenseFraction,
        animationSpec = tween(durationMillis = 700),
        label = "expense_bar"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Income Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingresos",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(70.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedIncome.coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .background(FinanceGreen, RoundedCornerShape(10.dp))
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = FinanceRepository.formatCurrency(income, currency),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = FinanceGreen
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expense Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gastos",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(70.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedExpense.coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .background(FinanceRed, RoundedCornerShape(10.dp))
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = FinanceRepository.formatCurrency(expense, currency),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = FinanceRed
            )
        }
    }
}

@Composable
fun SimpleProgressBar(
    progress: Float, // 0f..1f
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "prog_bar"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(height / 2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(color, RoundedCornerShape(height / 2))
        )
    }
}
