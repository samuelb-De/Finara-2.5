package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreen

object ColombianBanks {
    val list = listOf(
        "Bancolombia",
        "Banco de Bogotá",
        "Davivienda",
        "BBVA Colombia",
        "Banco de Occidente",
        "Banco Popular",
        "Banco AV Villas",
        "Scotiabank Colpatria",
        "Banco Itaú Colombia",
        "Banco Caja Social",
        "Banco Agrario de Colombia",
        "Banco Falabella",
        "Banco Pichincha",
        "Banco Santander Colombia",
        "Bancoomeva",
        "Banco Cooperativo Coopcentral",
        "Banco Serfinanza",
        "Banco Finandina",
        "Bancamía",
        "Banco Mundo Mujer",
        "Banco W",
        "Banco Unión",
        "Lulo Bank",
        "Nu Colombia (Nubank)",
        "Pibank",
        "Ualá",
        "Dale!",
        "RappiPay / RappiCard",
        "Tuya (Tarjeta Éxito / Carulla / Alkosto)",
        "Iris (Financiera Dann Regional)",
        "Coltefinanciera",
        "Financiera Juriscoop",
        "Confiar Cooperativa Financiera",
        "Cotrafa",
        "Banco BTG Pactual Colombia",
        "Banco J.P. Morgan Colombia",
        "Cootrapecur",
        "Otro banco o entidad"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColombianBankDropdown(
    selectedBank: String,
    onBankSelected: (String) -> Unit,
    label: String = "Banco / Entidad emisora",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isCustomBank by remember(selectedBank) {
        mutableStateOf(selectedBank.isNotBlank() && selectedBank !in ColombianBanks.list && selectedBank != "Otro banco o entidad")
    }
    var customBankName by remember(selectedBank) {
        mutableStateOf(if (isCustomBank) selectedBank else "")
    }

    val filteredBanks = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ColombianBanks.list
        } else {
            ColombianBanks.list.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = if (isCustomBank) (if (customBankName.isNotBlank()) customBankName else "Otro (Personalizado)") else selectedBank,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                placeholder = { Text("Selecciona un banco en Colombia") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = ForestGreen
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("colombian_bank_dropdown_field")
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    searchQuery = ""
                },
                modifier = Modifier
                    .heightIn(max = 280.dp)
                    .fillMaxWidth()
            ) {
                // Campo de búsqueda rápida
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar banco...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                filteredBanks.forEach { bank ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = bank,
                                fontWeight = if (bank == selectedBank) FontWeight.Bold else FontWeight.Normal,
                                color = if (bank == selectedBank) ForestGreen else Color.Unspecified
                            )
                        },
                        onClick = {
                            if (bank == "Otro banco o entidad") {
                                isCustomBank = true
                                onBankSelected(customBankName.ifBlank { "Otro" })
                            } else {
                                isCustomBank = false
                                onBankSelected(bank)
                            }
                            expanded = false
                            searchQuery = ""
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        if (isCustomBank) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customBankName,
                onValueChange = {
                    customBankName = it
                    onBankSelected(it)
                },
                label = { Text("Escribe el nombre del banco / entidad") },
                placeholder = { Text("Ej: Cooperativa Financiera...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_bank_input")
            )
        }
    }
}
