package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettingsEntity
import com.example.ui.FinanceUiState
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: FinanceUiState,
    onBack: (() -> Unit)? = null,
    onNavigateToLogin: (() -> Unit)? = null,
    onRegisterComplete: (
        name: String,
        email: String,
        handle: String,
        currency: String,
        password: String,
        pin: String,
        initialBalance: Double
    ) -> Unit
) {
    val initialName = if (state.userSettings.userName == "Usuario" || state.userSettings.userName == "Sofía García") "" else state.userSettings.userName
    val initialEmail = if (state.userSettings.userEmail == "usuario@correo.com" || state.userSettings.userEmail == "sofia.garcia@email.com") "" else state.userSettings.userEmail
    val initialHandle = if (state.userSettings.userHandle == "usuario" || state.userSettings.userHandle == "sofiagarcia") "" else state.userSettings.userHandle

    var name by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf(initialEmail) }
    var handle by remember { mutableStateOf(initialHandle) }
    var selectedCurrency by remember { mutableStateOf(state.userSettings.currency.ifBlank { "COP" }) }
    var initialBalanceText by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currencies = listOf("COP" to "Peso Colombiano ($)", "USD" to "Dólar Estadounidense ($)", "EUR" to "Euro (€)", "MXN" to "Peso Mexicano ($)")

    Scaffold(
        containerColor = Color(0xFFF7F9FA),
        modifier = Modifier
            .fillMaxSize()
            .testTag("register_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onBack != null) {
                    CircularBackButton(onClick = onBack)
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Text(
                    text = "Crear Cuenta",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
                if (onNavigateToLogin != null) {
                    TextButton(onClick = onNavigateToLogin) {
                        Text("Iniciar Sesión", color = ForestGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Welcome Card
            Surface(
                shape = RoundedCornerShape(24.dp),
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
                            .size(54.dp)
                            .background(ForestGreen, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Crear tu cuenta",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Comienza a organizar tus finanzas con claridad y control diario.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form container
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nombre completo
                    Column {
                        Text(
                            text = "Nombre completo *",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                errorMessage = null
                            },
                            placeholder = { Text("Ej: Carlos Rodríguez", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = ForestGreen)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // Correo electrónico
                    Column {
                        Text(
                            text = "Correo electrónico *",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            placeholder = { Text("ejemplo@correo.com", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = ForestGreen)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // Usuario / Handle
                    Column {
                        Text(
                            text = "Nombre de usuario (opcional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = handle,
                            onValueChange = { handle = it },
                            placeholder = { Text("@usuario", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = ForestGreen)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // Moneda principal
                    Column {
                        Text(
                            text = "Moneda principal *",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currencies.forEach { (code, _) ->
                                val isSelected = selectedCurrency == code
                                Surface(
                                    onClick = { selectedCurrency = code },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) ForestGreen else Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) ForestGreen else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = code,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isSelected) Color.White else Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Saldo inicial disponible
                    Column {
                        Text(
                            text = "Saldo inicial disponible (opcional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = initialBalanceText,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) initialBalanceText = it },
                            placeholder = { Text("0.00", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = ForestGreen)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // Contraseña o clave de acceso obligatoria
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Contraseña o PIN de acceso *",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (showPassword) "Ocultar" else "Mostrar",
                                fontSize = 12.sp,
                                color = ForestGreen,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { showPassword = !showPassword }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            placeholder = { Text("Mínimo 4 caracteres o dígitos", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = ForestGreen)
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // Confirmar Contraseña
                    Column {
                        Text(
                            text = "Confirmar contraseña *",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            placeholder = { Text("Repite tu contraseña", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.LockReset, contentDescription = null, tint = ForestGreen)
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_confirm_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // Acepto términos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { acceptTerms = !acceptTerms },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = acceptTerms,
                            onCheckedChange = { acceptTerms = it },
                            colors = CheckboxDefaults.colors(checkedColor = ForestGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Acepto los términos de uso y la política de privacidad",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    // Error display
                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFDECEB),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFBD3A3A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color(0xFFBD3A3A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Por favor ingresa tu nombre completo"
                                return@Button
                            }
                            if (email.isBlank() || !email.contains("@")) {
                                errorMessage = "Por favor ingresa un correo electrónico válido"
                                return@Button
                            }
                            if (password.length < 4) {
                                errorMessage = "La contraseña o clave debe tener al menos 4 caracteres"
                                return@Button
                            }
                            if (password != confirmPassword) {
                                errorMessage = "Las contraseñas no coinciden"
                                return@Button
                            }
                            if (!acceptTerms) {
                                errorMessage = "Debes aceptar los términos y condiciones"
                                return@Button
                            }

                            val initBal = initialBalanceText.toDoubleOrNull() ?: 0.0
                            val pinValue = if (password.all { it.isDigit() }) password else ""
                            onRegisterComplete(
                                name.trim(),
                                email.trim(),
                                if (handle.isNotBlank()) handle.trim() else name.lowercase().replace(" ", ""),
                                selectedCurrency,
                                password.trim(),
                                pinValue,
                                initBal
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("register_submit_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Crear mi cuenta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation to Login
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDF2EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Ya tienes una cuenta?",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        modifier = Modifier
                            .clickable { onNavigateToLogin?.invoke() }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
