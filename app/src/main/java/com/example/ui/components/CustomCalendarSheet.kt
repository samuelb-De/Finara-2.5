package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintLight
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCalendarSheet(
    initialDate: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val calendar = remember {
        Calendar.getInstance().apply { timeInMillis = initialDate }
    }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDayInMillis by remember { mutableStateOf(initialDate) }

    val todayCal = remember { Calendar.getInstance() }
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayYear = todayCal.get(Calendar.YEAR)

    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFFCBD5E1), CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Month Header with arrows: < Mayo 2024 >
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentMonth == 0) {
                            currentMonth = 11
                            currentYear--
                        } else {
                            currentMonth--
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Mes anterior",
                        tint = Color(0xFF1E293B)
                    )
                }

                Text(
                    text = "${monthNames[currentMonth]} $currentYear",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                IconButton(
                    onClick = {
                        if (currentMonth == 11) {
                            currentMonth = 0
                            currentYear++
                        } else {
                            currentMonth++
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Mes siguiente",
                        tint = Color(0xFF1E293B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day labels: L M M J V S D
            val weekDayLabels = listOf("L", "M", "M", "J", "V", "S", "D")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Days Grid
            val daysCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            // Java Calendar Sunday = 1, Monday = 2
            val firstDayOfWeek = daysCal.get(Calendar.DAY_OF_WEEK)
            val offset = (firstDayOfWeek + 5) % 7 // Monday = 0, Sunday = 6
            val daysInMonth = daysCal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val totalSlots = ((offset + daysInMonth + 6) / 7) * 7
            val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDayInMillis }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (week in 0 until (totalSlots / 7)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (dayOfWeek in 0 until 7) {
                            val slotIndex = week * 7 + dayOfWeek
                            val dayNum = slotIndex - offset + 1

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNum in 1..daysInMonth) {
                                    val isSelected = selectedCal.get(Calendar.DAY_OF_MONTH) == dayNum &&
                                            selectedCal.get(Calendar.MONTH) == currentMonth &&
                                            selectedCal.get(Calendar.YEAR) == currentYear

                                    val isToday = todayDay == dayNum &&
                                            todayMonth == currentMonth &&
                                            todayYear == currentYear

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .then(
                                                when {
                                                    isSelected -> Modifier.background(ForestGreen, CircleShape)
                                                    isToday -> Modifier.border(1.5.dp, ForestGreen, CircleShape)
                                                    else -> Modifier
                                                }
                                            )
                                            .clip(CircleShape)
                                            .clickable {
                                                val pickCal = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, currentYear)
                                                    set(Calendar.MONTH, currentMonth)
                                                    set(Calendar.DAY_OF_MONTH, dayNum)
                                                }
                                                selectedDayInMillis = pickCal.timeInMillis
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> Color.White
                                                isToday -> ForestGreen
                                                else -> Color(0xFF1E293B)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Hoy chip, Cancelar, Confirmar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Hoy" chip
                Surface(
                    onClick = {
                        val now = Calendar.getInstance()
                        currentYear = now.get(Calendar.YEAR)
                        currentMonth = now.get(Calendar.MONTH)
                        selectedDayInMillis = now.timeInMillis
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MintLight
                ) {
                    Text(
                        text = "Hoy",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B))
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onDateSelected(selectedDayInMillis)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Confirmar", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
