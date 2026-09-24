package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.IconHelper
import com.example.ui.theme.*

// Helper for row navigation card item
@Composable
fun SettingsMenuItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MintLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ==========================================
// 1. MENÚ MÁS (Menú Más.png)
// ==========================================
@Composable
fun MoreMenuScreen(
    state: FinanceUiState,
    onNavigateToProfile: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToMyData: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAppInfo: () -> Unit,
    onNavigateToAntExpenses: () -> Unit,
    onNavigateToSavings: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToFutureExpenses: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToRegister: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .padding(horizontal = 20.dp)
            .testTag("more_menu_screen"),
        contentPadding = PaddingValues(top = 24.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Más",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        }

        // Card: Registrarse / Nueva cuenta - Solo visible si la cuenta no ha sido creada aún
        if (!state.userSettings.hasCompletedOnboarding) {
            item {
                Surface(
                    onClick = onNavigateToRegister,
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                    modifier = Modifier.fillMaxWidth().testTag("register_menu_card")
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color(0xFFE8F3EE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Registrarse / Nueva cuenta",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Configura tu nombre, correo, moneda y PIN",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Card: Perfil y configuración
        item {
            Surface(
                onClick = onNavigateToProfile,
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
                            .size(52.dp)
                            .background(MintLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = state.userSettings.userName
                            .split(" ")
                            .filter { it.isNotEmpty() }
                            .take(2)
                            .joinToString("") { it.take(1).uppercase() }
                            .ifEmpty { "U" }
                        Text(
                            text = initials,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Perfil y configuración",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Cuenta, Mis datos, preferencias y seguridad",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Section: Finanzas
        item {
            Text(
                text = "Finanzas",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Tune, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Categorías y límites",
                        subtitle = "Personaliza tus categorías de gastos",
                        onClick = onNavigateToCategories
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.BugReport, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Gastos hormiga",
                        subtitle = "Control de fugas financieras",
                        onClick = onNavigateToAntExpenses
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Savings, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Metas de ahorro",
                        subtitle = "Seguimiento de objetivos",
                        onClick = onNavigateToSavings
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Deudas y créditos",
                        subtitle = "Saldos y planes de pago",
                        onClick = onNavigateToDebts
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Event, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Gastos futuros",
                        subtitle = "Compromisos programados",
                        onClick = onNavigateToFutureExpenses
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Calendario financiero",
                        subtitle = "Vista mensual de pagos y vencimientos",
                        onClick = onNavigateToCalendar
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Reportes y comparativas",
                        subtitle = "Análisis por período y flujo",
                        onClick = onNavigateToReports
                    )
                }
            }
        }

        // Section: Soporte
        item {
            Text(
                text = "Soporte",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.HelpOutline, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Ayuda",
                        subtitle = "Preguntas frecuentes y soporte",
                        onClick = onNavigateToHelp
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Información de la aplicación",
                        subtitle = "Versión y licencias",
                        onClick = onNavigateToAppInfo
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. MI PERFIL (Mi perfil.png)
// ==========================================
@Composable
fun MyProfileScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToMyData: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAppInfo: () -> Unit,
    onNavigateToRegister: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .padding(horizontal = 20.dp)
            .testTag("my_profile_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top row with circular back button and title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Mi perfil",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
        }

        // Profile Card with Avatar, Name, Email, Handle, Button "Editar perfil"
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MintLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = state.userSettings.userName
                            .split(" ")
                            .filter { it.isNotEmpty() }
                            .take(2)
                            .joinToString("") { it.take(1).uppercase() }
                            .ifEmpty { "U" }
                        Text(
                            text = initials,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = state.userSettings.userName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = state.userSettings.userEmail,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "@${state.userSettings.userHandle}",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Miembro desde el ${state.userSettings.memberSince}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToEditProfile,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Editar perfil", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    if (!state.userSettings.hasCompletedOnboarding) {
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onNavigateToRegister,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Registrarse con otra cuenta", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = ForestGreen)
                        }
                    }
                }
            }
        }

        // Resumen Financiero Card (dark green)
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = ForestGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resumen financiero",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "● ● ● ● ● ● ● ●",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.userSettings.hideBalancesDefault)
                            "Tus saldos están ocultos por defecto"
                        else
                            "Saldos protegidos por privacidad",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Section: Tu experiencia
        item {
            Text(
                text = "Tu experiencia",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Palette, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Preferencias",
                        subtitle = "Tema, moneda y fechas",
                        onClick = onNavigateToPreferences
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Security, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Privacidad y seguridad",
                        subtitle = "PIN, biometría y sesiones",
                        onClick = onNavigateToPrivacy
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Notificaciones",
                        subtitle = "Avisos y recordatorios",
                        onClick = onNavigateToNotifications
                    )
                }
            }
        }

        // Section: Organización y soporte
        item {
            Text(
                text = "Organización y soporte",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Tune, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Categorías y límites",
                        subtitle = "Personaliza tus gastos",
                        onClick = onNavigateToCategories
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Storage, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Mis datos",
                        subtitle = "Exportar, respaldar y eliminar",
                        onClick = onNavigateToMyData
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.HelpOutline, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Ayuda",
                        subtitle = "Preguntas frecuentes y soporte",
                        onClick = onNavigateToHelp
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Información de la aplicación",
                        subtitle = "Versión y licencias",
                        onClick = onNavigateToAppInfo
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. EDITAR PERFIL (Editar perfil.png)
// ==========================================
@Composable
fun EditProfileScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onSave: (name: String, email: String, handle: String) -> Unit,
    onSaveInitialSetup: (initialIncome: Double, initialExpense: Double, savingsTarget: Double) -> Unit
) {
    var nameInput by remember { mutableStateOf(state.userSettings.userName) }
    var emailInput by remember { mutableStateOf(state.userSettings.userEmail) }
    var handleInput by remember { mutableStateOf(state.userSettings.userHandle) }

    // Initial setup fields (fulfilling prompt: "hacer lo mismo que hace en el apartado iniciar pero con el diseño actual")
    var incomeInput by remember { mutableStateOf("") }
    var expenseInput by remember { mutableStateOf("") }
    var savingsInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("edit_profile_screen")
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
                    text = "Editar perfil",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar + "Cambiar foto"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(MintLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = nameInput
                        .split(" ")
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .joinToString("") { it.take(1).uppercase() }
                        .ifEmpty { "SG" }
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Cambiar foto",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "JPG o PNG, máximo 5 MB",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campo: Nombre completo
            Text("Nombre completo", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    if (nameInput.isNotBlank()) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Válido",
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Nombre correcto", fontSize = 12.sp, color = ForestGreen, modifier = Modifier.padding(start = 4.dp))

            Spacer(modifier = Modifier.height(18.dp))

            // Campo: Correo
            Text("Correo", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Usaremos este correo para avisos importantes",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Campo: Usuario
            Text("Usuario", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = handleInput,
                        onValueChange = { handleInput = it.filter { c -> c.isLetterOrDigit() || c == '_' } },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    if (handleInput.isNotBlank()) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Disponible",
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Disponible", fontSize = 12.sp, color = ForestGreen, modifier = Modifier.padding(start = 4.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Initial / Fast Financial Setup Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = ForestGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ajuste Financiero Inicial",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Configura tus estimaciones de ingresos, gastos y ahorro si deseas recalibrar tu punto de partida.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = incomeInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) incomeInput = it },
                        label = { Text("Ingreso mensual (${state.userSettings.currency})") },
                        placeholder = { Text("Ej. 2480000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expenseInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) expenseInput = it },
                        label = { Text("Gasto habitual (${state.userSettings.currency})") },
                        placeholder = { Text("Ej. 1230000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = savingsInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) savingsInput = it },
                        label = { Text("Meta de ahorro (${state.userSettings.currency})") },
                        placeholder = { Text("Ej. 500000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onSave(nameInput, emailInput, handleInput)
                        val inc = incomeInput.toDoubleOrNull() ?: 0.0
                        val exp = expenseInput.toDoubleOrNull() ?: 0.0
                        val sav = savingsInput.toDoubleOrNull() ?: 0.0
                        if (inc > 0 || exp > 0 || sav > 0) {
                            onSaveInitialSetup(inc, exp, sav)
                        }
                        onBack()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("Guardar cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 4. PREFERENCIAS (Preferencias.png)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onUpdateTheme: (isDark: Boolean?) -> Unit,
    onUpdateCurrency: (currency: String) -> Unit,
    onUpdateDateFormat: (format: String) -> Unit,
    onUpdateFirstDay: (day: Int) -> Unit
) {
    var showThemeSheet by remember { mutableStateOf(false) }
    var showCurrencySheet by remember { mutableStateOf(false) }
    var showDateFormatSheet by remember { mutableStateOf(false) }

    // Bottom Sheet: Selección de tema (Selección de tema.png)
    if (showThemeSheet) {
        var tempTheme by remember { mutableStateOf(state.userSettings.isDarkMode) }
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Seleccionar tema",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    Triple(false as Boolean?, "Claro", "Interfaz luminosa y de alto contraste"),
                    Triple(true as Boolean?, "Oscuro", "Reduce el brillo para ambientes tenues"),
                    Triple(null as Boolean?, "Automático", "Sigue la configuración de tu sistema")
                ).forEach { (mode, label, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempTheme = mode }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempTheme == mode,
                            onClick = { tempTheme = mode },
                            colors = RadioButtonDefaults.colors(selectedColor = ForestGreen)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            Text(desc, fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onUpdateTheme(tempTheme)
                        showThemeSheet = false
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Aplicar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // Bottom Sheet: Selección de moneda (Selección de moneda.png)
    if (showCurrencySheet) {
        var tempCurrency by remember { mutableStateOf(state.userSettings.currency) }
        val currencies = listOf(
            "COP" to "Peso colombiano (COP)",
            "USD" to "Dólar estadounidense (USD)",
            "EUR" to "Euro (EUR)",
            "MXN" to "Peso mexicano (MXN)"
        )
        ModalBottomSheet(
            onDismissRequest = { showCurrencySheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Seleccionar moneda",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(16.dp))

                currencies.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempCurrency = code }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempCurrency == code,
                            onClick = { tempCurrency = code },
                            colors = RadioButtonDefaults.colors(selectedColor = ForestGreen)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1E293B))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onUpdateCurrency(tempCurrency)
                        showCurrencySheet = false
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Aplicar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // Bottom Sheet: Selección de formato de fecha (Selección de formato de fecha.png)
    if (showDateFormatSheet) {
        var tempFormat by remember { mutableStateOf(state.userSettings.dateFormat) }
        val formats = listOf(
            "DD/MM/AAAA" to "21/09/2026 (Día primero)",
            "MM/DD/AAAA" to "09/21/2026 (Mes primero)",
            "AAAA-MM-DD" to "2026-09-21 (Estándar ISO)"
        )
        ModalBottomSheet(
            onDismissRequest = { showDateFormatSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Formato de fecha",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(16.dp))

                formats.forEach { (code, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempFormat = code }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempFormat == code,
                            onClick = { tempFormat = code },
                            colors = RadioButtonDefaults.colors(selectedColor = ForestGreen)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(code, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1E293B))
                            Text(desc, fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onUpdateDateFormat(tempFormat)
                        showDateFormatSheet = false
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Aplicar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("preferences_screen")
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
                    text = "Preferencias",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Personaliza la forma en que ves y organizas tu información financiera.",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.DarkMode, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Tema",
                        subtitle = when (state.userSettings.isDarkMode) {
                            true -> "Oscuro"
                            false -> "Claro"
                            null -> "Automático"
                        },
                        onClick = { showThemeSheet = true }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Moneda",
                        subtitle = "${state.userSettings.currency} · ${
                            when (state.userSettings.currency) {
                                "COP" -> "Peso colombiano"
                                "USD" -> "Dólar estadounidense"
                                "EUR" -> "Euro"
                                "MXN" -> "Peso mexicano"
                                else -> state.userSettings.currency
                            }
                        }",
                        onClick = { showCurrencySheet = true }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Formato de fecha",
                        subtitle = state.userSettings.dateFormat,
                        onClick = { showDateFormatSheet = true }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Primer día del período financiero",
                        subtitle = "Día ${state.userSettings.firstDayOfPeriod} de cada mes",
                        onClick = {
                            val nextDay = if (state.userSettings.firstDayOfPeriod == 1) 15 else 1
                            onUpdateFirstDay(nextDay)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card "Vista previa"
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Vista previa",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$ 1.250.000 ${state.userSettings.currency}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "21/09/2026 · Período desde el día ${state.userSettings.firstDayOfPeriod}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. PRIVACIDAD Y SEGURIDAD (Privacidad y seguridad.png)
// ==========================================
@Composable
fun PrivacySecurityScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onUpdateSecurity: (hideBalances: Boolean, biometrics: Boolean, pin: String, pinEnabled: Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    var hideBalances by remember { mutableStateOf(state.userSettings.hideBalancesDefault) }
    var biometrics by remember { mutableStateOf(state.userSettings.biometricsEnabled) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }

    // Dialog: Confirmación de cerrar sesión (Confirmación de cerrar sesión.png)
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = {
                Box(
                    modifier = Modifier.size(52.dp).background(MintLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(24.dp))
                }
            },
            title = {
                Text("¿Cerrar sesión?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    "Saldrás de este dispositivo. Tus movimientos, presupuestos y demás datos de la cuenta no se eliminarán.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White)
                ) {
                    Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSignOutDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog: PIN setup
    if (showPinSetupDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinSetupDialog = false },
            title = { Text("Configurar PIN de 4 dígitos", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Este PIN protegerá el acceso a tus datos financieros.", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("PIN de 4 números") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            onUpdateSecurity(hideBalances, biometrics, pinInput, true)
                            showPinSetupDialog = false
                        }
                    },
                    enabled = pinInput.length == 4,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("Guardar PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinSetupDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("privacy_security_screen")
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
                    text = "Privacidad y seguridad",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Ocultar saldos por defecto",
                        subtitle = "Protege tu pantalla en lugares públicos",
                        trailing = {
                            Switch(
                                checked = hideBalances,
                                onCheckedChange = {
                                    hideBalances = it
                                    onUpdateSecurity(it, biometrics, state.userSettings.pinCode, state.userSettings.isPinEnabled)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = MintMedium)
                            )
                        },
                        onClick = {
                            hideBalances = !hideBalances
                            onUpdateSecurity(hideBalances, biometrics, state.userSettings.pinCode, state.userSettings.isPinEnabled)
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Pin, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "PIN",
                        subtitle = if (state.userSettings.isPinEnabled && state.userSettings.pinCode.isNotEmpty())
                            "Activo · 4 dígitos"
                        else
                            "Desactivado",
                        onClick = { showPinSetupDialog = true }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Biometría",
                        subtitle = "Desbloqueo rápido con huella o rostro",
                        trailing = {
                            Switch(
                                checked = biometrics,
                                onCheckedChange = {
                                    biometrics = it
                                    onUpdateSecurity(hideBalances, it, state.userSettings.pinCode, state.userSettings.isPinEnabled)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = MintMedium)
                            )
                        },
                        onClick = {
                            biometrics = !biometrics
                            onUpdateSecurity(hideBalances, biometrics, state.userSettings.pinCode, state.userSettings.isPinEnabled)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Cambiar contraseña / PIN",
                        subtitle = "Actualiza tu clave de acceso",
                        onClick = { showPinSetupDialog = true }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Devices, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Gestionar sesiones activas",
                        subtitle = "Este dispositivo móvil conectado",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { showSignOutDialog = true },
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Cerrar sesión", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

// ==========================================
// 6. NOTIFICACIONES (Notificaciones.png)
// ==========================================
@Composable
fun NotificationsScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onUpdateSettings: (UserSettingsEntity) -> Unit
) {
    var rExpenses by remember { mutableStateOf(state.userSettings.reminderExpenses) }
    var rUpcoming by remember { mutableStateOf(state.userSettings.reminderUpcomingPayments) }
    var rDebts by remember { mutableStateOf(state.userSettings.reminderDebtDue) }
    var rBudgets by remember { mutableStateOf(state.userSettings.reminderBudgetLimit) }
    var rSavings by remember { mutableStateOf(state.userSettings.reminderSavingsGoals) }
    var rWeekly by remember { mutableStateOf(state.userSettings.reminderWeeklySummary) }

    fun sync(newSettings: UserSettingsEntity) {
        onUpdateSettings(newSettings)
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("notifications_screen")
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
                    text = "Notificaciones",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Recordatorios de gastos",
                        subtitle = "Aviso diario para registrar tus movimientos",
                        trailing = {
                            Switch(
                                checked = rExpenses,
                                onCheckedChange = {
                                    rExpenses = it
                                    sync(state.userSettings.copy(reminderExpenses = it))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = MintMedium)
                            )
                        },
                        onClick = {
                            rExpenses = !rExpenses
                            sync(state.userSettings.copy(reminderExpenses = rExpenses))
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Event, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Próximos pagos",
                        subtitle = "Avisos de gastos futuros programados",
                        trailing = {
                            Switch(
                                checked = rUpcoming,
                                onCheckedChange = {
                                    rUpcoming = it
                                    sync(state.userSettings.copy(reminderUpcomingPayments = it))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = MintMedium)
                            )
                        },
                        onClick = {
                            rUpcoming = !rUpcoming
                            sync(state.userSettings.copy(reminderUpcomingPayments = rUpcoming))
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Vencimiento de deudas",
                        subtitle = "Alertas antes del día límite de tu cuota",
                        trailing = {
                            Switch(
                                checked = rDebts,
                                onCheckedChange = {
                                    rDebts = it
                                    sync(state.userSettings.copy(reminderDebtDue = it))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = MintMedium)
                            )
                        },
                        onClick = {
                            rDebts = !rDebts
                            sync(state.userSettings.copy(reminderDebtDue = rDebts))
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.PieChart, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Presupuesto próximo al límite",
                        subtitle = "Alerta al alcanzar el 80% de una categoría",
                        trailing = {
                            Switch(
                                checked = rBudgets,
                                onCheckedChange = {
                                    rBudgets = it
                                    sync(state.userSettings.copy(reminderBudgetLimit = it))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = MintMedium)
                            )
                        },
                        onClick = {
                            rBudgets = !rBudgets
                            sync(state.userSettings.copy(reminderBudgetLimit = rBudgets))
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Savings, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Objetivos de ahorro",
                        subtitle = "Felicitaciones y metas alcanzadas",
                        trailing = {
                            Switch(
                                checked = rSavings,
                                onCheckedChange = {
                                    rSavings = it
                                    sync(state.userSettings.copy(reminderSavingsGoals = it))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = MintMedium)
                            )
                        },
                        onClick = {
                            rSavings = !rSavings
                            sync(state.userSettings.copy(reminderSavingsGoals = rSavings))
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Summarize, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Resumen semanal",
                        subtitle = "Balance dominical de tu actividad",
                        trailing = {
                            Switch(
                                checked = rWeekly,
                                onCheckedChange = {
                                    rWeekly = it
                                    sync(state.userSettings.copy(reminderWeeklySummary = it))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = MintMedium)
                            )
                        },
                        onClick = {
                            rWeekly = !rWeekly
                            sync(state.userSettings.copy(reminderWeeklySummary = rWeekly))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom reminder banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MintLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ForestGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Para recibir avisos, permite las notificaciones en tu dispositivo.",
                        fontSize = 13.sp,
                        color = ForestGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==========================================
// 7. CATEGORÍAS Y LÍMITES (Categorías y límites.png)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesLimitsScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onToggleCategory: (category: CategoryEntity) -> Unit,
    onAddCategory: (name: String, icon: String, color: Long, type: String) -> Unit,
    onUpdateCategory: (category: CategoryEntity, oldName: String) -> Unit,
    onDeleteCategory: (category: CategoryEntity) -> Unit,
    onUpdateAntLimit: (limit: Double) -> Unit
) {
    var selectedTab by remember { mutableStateOf("GASTOS") } // "GASTOS" or "INGRESOS"
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var deletingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showAntLimitDialog by remember { mutableStateOf(false) }

    val expenseCategories = remember(state.categories) {
        state.categories.filter {
            it.categoryType == "EXPENSE" || (it.categoryType.isEmpty() && it.name != "Salario / Ingresos" && it.name != "Ahorro")
        }
    }
    val incomeCategories = remember(state.categories) {
        state.categories.filter {
            it.categoryType == "INCOME" || it.name in listOf("Salario", "Salario / Ingresos", "Honorarios", "Honorarios / Freelance", "Negocio", "Negocio / Ventas", "Inversión", "Inversiones", "Regalo", "Regalos", "Ventas", "Freelance", "Otros ingresos")
        }
    }

    // Dialog: Crear nueva categoría (Gastos o Ingresos)
    if (showAddDialog) {
        var catName by remember { mutableStateOf("") }
        var catType by remember { mutableStateOf(if (selectedTab == "INGRESOS") "INCOME" else "EXPENSE") }
        var selectedIcon by remember(catType) {
            mutableStateOf(if (catType == "INCOME") "attach_money" else "category")
        }
        var selectedColor by remember(catType) {
            mutableStateOf(if (catType == "INCOME") 0xFF10B981 else 0xFF3B82F6)
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva categoría", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Nombre de la categoría") },
                        placeholder = { Text(if (catType == "INCOME") "Ej: Freelance, Arriendos..." else "Ej: Mascotas, Gimnasio...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tipo: Gasto o Ingreso
                    Text("Tipo de categoría", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (catType == "EXPENSE") Color.White else Color.Transparent)
                                .clickable { catType = "EXPENSE" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Gasto", fontWeight = if (catType == "EXPENSE") FontWeight.Bold else FontWeight.Normal, color = if (catType == "EXPENSE") ForestGreen else Color(0xFF64748B))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (catType == "INCOME") Color.White else Color.Transparent)
                                .clickable { catType = "INCOME" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ingreso", fontWeight = if (catType == "INCOME") FontWeight.Bold else FontWeight.Normal, color = if (catType == "INCOME") ForestGreen else Color(0xFF64748B))
                        }
                    }

                    // Selector de ícono
                    Text("Selecciona un icono", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    val relevantIcons = remember(catType) {
                        if (catType == "INCOME") {
                            listOf("attach_money", "receipt", "store", "trending_up", "card_giftcard", "payments", "work", "account_balance", "laptop", "category")
                        } else {
                            listOf("restaurant", "directions_bus", "home", "school", "local_hospital", "sports_esports", "memory", "checkroom", "electrical_services", "coffee", "shopping_cart", "pets", "flight", "fitness_center", "category")
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        relevantIcons.forEach { iconKey ->
                            val isSel = selectedIcon == iconKey
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) Color(selectedColor).copy(alpha = 0.2f) else Color(0xFFF1F5F9))
                                    .border(if (isSel) 2.dp else 0.dp, if (isSel) Color(selectedColor) else Color.Transparent, CircleShape)
                                    .clickable { selectedIcon = iconKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconVector(iconKey),
                                    contentDescription = iconKey,
                                    tint = if (isSel) Color(selectedColor) else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Selector de color
                    Text("Selecciona un color", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconHelper.availableColors.take(6).forEach { (colorHex, _) ->
                            val isSel = selectedColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(if (isSel) 3.dp else 0.dp, if (isSel) Color.DarkGray else Color.Transparent, CircleShape)
                                    .clickable { selectedColor = colorHex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            onAddCategory(catName.trim(), selectedIcon, selectedColor, catType)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Dialog: Editar categoría existente
    editingCategory?.let { originalCat ->
        var editName by remember(originalCat) { mutableStateOf(originalCat.name) }
        var editType by remember(originalCat) { mutableStateOf(originalCat.categoryType) }
        var editIcon by remember(originalCat) { mutableStateOf(originalCat.iconName) }
        var editColor by remember(originalCat) { mutableStateOf(originalCat.colorHex) }
        var editActive by remember(originalCat) { mutableStateOf(originalCat.isActive) }

        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Editar categoría", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre de la categoría") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tipo: Gasto o Ingreso
                    Text("Tipo de categoría", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (editType == "EXPENSE") Color.White else Color.Transparent)
                                .clickable { editType = "EXPENSE" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Gasto", fontWeight = if (editType == "EXPENSE") FontWeight.Bold else FontWeight.Normal, color = if (editType == "EXPENSE") ForestGreen else Color(0xFF64748B))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (editType == "INCOME") Color.White else Color.Transparent)
                                .clickable { editType = "INCOME" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ingreso", fontWeight = if (editType == "INCOME") FontWeight.Bold else FontWeight.Normal, color = if (editType == "INCOME") ForestGreen else Color(0xFF64748B))
                        }
                    }

                    // Icon selector
                    Text("Icono", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    val relevantIcons = remember(editType) {
                        if (editType == "INCOME") {
                            listOf("attach_money", "receipt", "store", "trending_up", "card_giftcard", "payments", "work", "account_balance", "laptop", "category")
                        } else {
                            listOf("restaurant", "directions_bus", "home", "school", "local_hospital", "sports_esports", "memory", "checkroom", "electrical_services", "coffee", "shopping_cart", "pets", "flight", "fitness_center", "category")
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        relevantIcons.forEach { iconKey ->
                            val isSel = editIcon == iconKey
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) Color(editColor).copy(alpha = 0.2f) else Color(0xFFF1F5F9))
                                    .border(if (isSel) 2.dp else 0.dp, if (isSel) Color(editColor) else Color.Transparent, CircleShape)
                                    .clickable { editIcon = iconKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconVector(iconKey),
                                    contentDescription = iconKey,
                                    tint = if (isSel) Color(editColor) else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Color selector
                    Text("Color", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconHelper.availableColors.take(6).forEach { (colorHex, _) ->
                            val isSel = editColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(if (isSel) 3.dp else 0.dp, if (isSel) Color.DarkGray else Color.Transparent, CircleShape)
                                    .clickable { editColor = colorHex }
                            )
                        }
                    }

                    // Estado activo / inactivo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Categoría activa", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Switch(checked = editActive, onCheckedChange = { editActive = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            val updatedCat = originalCat.copy(
                                name = editName.trim(),
                                iconName = editIcon,
                                colorHex = editColor,
                                categoryType = editType,
                                isActive = editActive
                            )
                            onUpdateCategory(updatedCat, originalCat.name)
                            editingCategory = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("Guardar cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) { Text("Cancelar") }
            }
        )
    }

    // Dialog: Confirmar eliminación
    deletingCategory?.let { catToDelete ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text("Eliminar categoría", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar la categoría '${catToDelete.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(catToDelete)
                        deletingCategory = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) { Text("Cancelar") }
            }
        )
    }

    if (showAntLimitDialog) {
        var limitInput by remember { mutableStateOf(state.userSettings.antExpenseLimit.toLong().toString()) }
        AlertDialog(
            onDismissRequest = { showAntLimitDialog = false },
            title = { Text("Límite de gastos hormiga", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Gastos menores o iguales a este monto serán catalogados como hormiga.", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) limitInput = it },
                        label = { Text("Monto límite (${state.userSettings.currency})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = limitInput.toDoubleOrNull() ?: 5000.0
                        onUpdateAntLimit(num)
                        showAntLimitDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAntLimitDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("categories_limits_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularBackButton(onClick = onBack)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Categorías y límites",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            // Pestañas Gastos vs Ingresos
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(3.dp)
                ) {
                    val isGastos = selectedTab == "GASTOS"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isGastos) Color.White else Color.Transparent)
                            .clickable { selectedTab = "GASTOS" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gastos (${expenseCategories.size})",
                            fontWeight = if (isGastos) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isGastos) ForestGreen else Color(0xFF64748B)
                        )
                    }
                    val isIngresos = selectedTab == "INGRESOS"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isIngresos) Color.White else Color.Transparent)
                            .clickable { selectedTab = "INGRESOS" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ingresos (${incomeCategories.size})",
                            fontWeight = if (isIngresos) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isIngresos) ForestGreen else Color(0xFF64748B)
                        )
                    }
                }
            }

            // Header con botón Crear
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTab == "GASTOS") "Categorías de gasto" else "Categorías de ingreso",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Surface(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MintLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Crear", color = ForestGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Categories card
            val currentList = if (selectedTab == "GASTOS") expenseCategories else incomeCategories
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (currentList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay categorías registradas.",
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    } else {
                        Column {
                            currentList.forEachIndexed { index, cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(cat.colorHex).copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = IconHelper.getIconVector(cat.iconName),
                                            contentDescription = cat.name,
                                            tint = Color(cat.colorHex),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cat.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = if (cat.isActive) "Activa" else "Desactivada",
                                            fontSize = 12.sp,
                                            color = if (cat.isActive) ForestGreen else Color(0xFF94A3B8)
                                        )
                                    }

                                    // Acciones: Editar, Alternar estado, Eliminar
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { editingCategory = cat },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar categoría",
                                                tint = Color(0xFF475569),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onToggleCategory(cat) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                if (cat.isActive) Icons.Default.PauseCircleOutline else Icons.Default.PlayCircleOutline,
                                                contentDescription = "Alternar estado",
                                                tint = if (cat.isActive) Color(0xFF64748B) else ForestGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        if (!cat.isDefault) {
                                            IconButton(
                                                onClick = { deletingCategory = cat },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteOutline,
                                                    contentDescription = "Eliminar categoría",
                                                    tint = CrimsonRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (index < currentList.lastIndex) {
                                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }

            // Límite de gastos hormiga Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAntLimitDialog = true }
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MintLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = ForestGreen)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Límite de gastos hormiga",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Considerar como gasto hormiga los gastos inferiores a $${state.userSettings.antExpenseLimit.toLong()}",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        IconButton(onClick = { showAntLimitDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. MIS DATOS (Mis datos.png + Eliminar cuenta 3-steps)
// ==========================================
@Composable
fun MyDataScreen(
    state: FinanceUiState,
    onBack: () -> Unit,
    onExportCsv: () -> Unit,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit,
    onToggleBackup: (Boolean) -> Unit,
    onClearDataConfirmed: () -> Unit
) {
    var deleteStep by remember { mutableStateOf(0) } // 0: None, 1: Advertencia, 2: Confirmación ELIMINAR, 3: Reautenticación
    var deleteInputText by remember { mutableStateOf("") }
    var understandChecked by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    // Step 1: Eliminar cuenta · advertencia.png
    if (deleteStep == 1) {
        AlertDialog(
            onDismissRequest = { deleteStep = 0 },
            icon = {
                Box(
                    modifier = Modifier.size(52.dp).background(CrimsonLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(26.dp))
                }
            },
            title = {
                Text("Eliminar tu cuenta", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    "Esta acción eliminará permanentemente tu perfil, movimientos, presupuestos, objetivos y copias de seguridad. No podrás recuperar tus datos.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { deleteStep = 2 },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White)
                ) {
                    Text("Entiendo, continuar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteStep = 0 }, shape = RoundedCornerShape(12.dp)) {
                    Text("Conservar mi cuenta")
                }
            }
        )
    }

    // Step 2: Eliminar cuenta · confirmación.png
    if (deleteStep == 2) {
        AlertDialog(
            onDismissRequest = { deleteStep = 0 },
            title = {
                Text("Confirmación explícita", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Para continuar, escribe ELIMINAR en el campo de texto a continuación:",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = deleteInputText,
                        onValueChange = { deleteInputText = it },
                        placeholder = { Text("Escribe ELIMINAR") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { understandChecked = !understandChecked }
                    ) {
                        Checkbox(
                            checked = understandChecked,
                            onCheckedChange = { understandChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = CrimsonRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Comprendo que la eliminación es permanente.", fontSize = 13.sp, color = Color(0xFF1E293B))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { deleteStep = 3 },
                    enabled = deleteInputText.trim().equals("ELIMINAR", ignoreCase = false) && understandChecked,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("Continuar a reautenticación")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteStep = 0 }) { Text("Cancelar") }
            }
        )
    }

    // Step 3: Eliminar cuenta · reautenticación.png
    if (deleteStep == 3) {
        AlertDialog(
            onDismissRequest = { deleteStep = 0 },
            icon = {
                Box(
                    modifier = Modifier.size(52.dp).background(CrimsonLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(26.dp))
                }
            },
            title = {
                Text("Reautenticación requerida", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Confirma con tu PIN o biometría para ejecutar la eliminación definitiva.",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("Ingresa tu PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteStep = 0
                        onClearDataConfirmed()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("Eliminar mi cuenta definitivamente", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteStep = 0 }) { Text("Volver") }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("my_data_screen")
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
                    text = "Mis datos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card: Exportar información
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Exportar información", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Descarga tus movimientos y categorías en archivos estándar.", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onExportCsv,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MintLight, contentColor = ForestGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CSV", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onExportExcel,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MintLight, contentColor = ForestGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Excel", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generar reporte en PDF", color = ForestGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card: Copia de seguridad
            Surface(
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
                        modifier = Modifier.size(44.dp).background(MintLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = ForestGreen)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Copia de seguridad", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Sincronizado hoy a las 9:38", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Switch(
                        checked = state.userSettings.backupEnabled,
                        onCheckedChange = onToggleBackup,
                        colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = MintMedium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card: Eliminar datos (Crimson tint)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CrimsonLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = CrimsonRed)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Eliminar datos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CrimsonRed)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Borra movimientos y presupuestos, o elimina tu cuenta de manera permanente.",
                        fontSize = 13.sp,
                        color = Color(0xFF4A5568)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Esta acción requiere confirmación y no se puede deshacer.",
                        fontSize = 12.sp,
                        color = CrimsonRed,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { deleteStep = 1 },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eliminar datos o cuenta", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. AYUDA (Ayuda.png)
// ==========================================
@Composable
fun HelpScreen(
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("help_screen")
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
                    text = "Ayuda",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Preguntas frecuentes",
                        subtitle = "Respuestas rápidas a dudas comunes",
                        onClick = {}
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Cómo utilizar la aplicación",
                        subtitle = "Guía paso a paso de todas las funciones",
                        onClick = {}
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Calculate, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Cálculos financieros",
                        subtitle = "Explicación de balances, ahorro y amortización",
                        onClick = {}
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Contacto o soporte",
                        subtitle = "Respuesta habitual en 24 horas",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Description, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Términos y condiciones",
                        onClick = {}
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Policy, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Política de privacidad",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card "¿Aún necesitas ayuda?"
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MintLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = ForestGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "¿Aún necesitas ayuda?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ForestGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Escríbenos directamente a Samuelb1824@gmail.com para resolver cualquier duda sobre tu cuenta o el funcionamiento.",
                        fontSize = 13.sp,
                        color = Color(0xFF265341)
                    )
                }
            }
        }
    }
}

// ==========================================
// 10. INFORMACIÓN DE LA APLICACIÓN (Información de la aplicación.png)
// ==========================================
@Composable
fun AppInfoScreen(
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier.fillMaxSize().testTag("app_info_screen")
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
                    text = "Información",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main App Badge Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(com.example.R.drawable.finara_logo_icon_1790086862734),
                        contentDescription = "Logo Finara",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Finara",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Tu dinero, con claridad",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Versión instalada", fontSize = 13.sp, color = Color(0xFF64748B))
                        Text("2.5.0", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Versión API", fontSize = 13.sp, color = Color(0xFF64748B))
                        Text("v1.0", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Security, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Política de privacidad",
                        onClick = {}
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsMenuItem(
                        icon = { Icon(Icons.Default.Code, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp)) },
                        title = "Licencias de código abierto",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "© 2026 Finara · Hecho en Colombia",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
