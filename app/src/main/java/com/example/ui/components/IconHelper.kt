package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    fun getIconVector(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "restaurant", "food", "comida", "almuerzo" -> Icons.Default.Restaurant
            "directions_bus", "transporte", "bus", "car" -> Icons.Default.DirectionsBus
            "home", "vivienda", "casa" -> Icons.Default.Home
            "school", "educacion", "estudio" -> Icons.Default.School
            "local_hospital", "salud", "medicina" -> Icons.Default.LocalHospital
            "sports_esports", "entretenimiento", "juegos" -> Icons.Default.SportsEsports
            "memory", "tecnologia", "tech" -> Icons.Default.Memory
            "checkroom", "ropa", "vestimenta" -> Icons.Default.Checkroom
            "electrical_services", "servicios", "luz" -> Icons.Default.ElectricalServices
            "credit_card", "deudas", "tarjeta" -> Icons.Default.CreditCard
            "savings", "ahorro", "cerdito" -> Icons.Default.Savings
            "attach_money", "salario", "ingreso", "money" -> Icons.Default.AttachMoney
            "receipt", "honorarios", "freelance" -> Icons.Default.Receipt
            "store", "negocio", "ventas" -> Icons.Default.Store
            "trending_up", "inversion", "inversiones", "inversión" -> Icons.Default.TrendingUp
            "card_giftcard", "regalo", "regalos" -> Icons.Default.CardGiftcard
            "payments", "otros ingresos", "pago" -> Icons.Default.Payments
            "work", "trabajo", "empleo" -> Icons.Default.Work
            "account_balance", "banco", "dividendos" -> Icons.Default.AccountBalance
            "laptop", "computador" -> Icons.Default.Laptop
            "shield", "emergencia" -> Icons.Default.Shield
            "coffee", "cafe" -> Icons.Default.Coffee
            "shopping_cart", "compras" -> Icons.Default.ShoppingCart
            "flight", "viajes" -> Icons.Default.Flight
            "fitness_center", "gym" -> Icons.Default.FitnessCenter
            "pets", "mascotas" -> Icons.Default.Pets
            "build", "mantenimiento" -> Icons.Default.Build
            else -> Icons.Default.Category
        }
    }

    val availableIcons = listOf(
        "restaurant" to "Alimentación",
        "directions_bus" to "Transporte",
        "home" to "Vivienda",
        "school" to "Educación",
        "local_hospital" to "Salud",
        "sports_esports" to "Entretenimiento",
        "memory" to "Tecnología",
        "checkroom" to "Ropa",
        "electrical_services" to "Servicios",
        "credit_card" to "Deudas",
        "savings" to "Ahorro",
        "attach_money" to "Salario / Ingresos",
        "receipt" to "Honorarios / Freelance",
        "store" to "Negocio / Ventas",
        "trending_up" to "Inversiones",
        "card_giftcard" to "Regalos",
        "payments" to "Pagos recibidos",
        "work" to "Trabajo",
        "account_balance" to "Banco / Dividendos",
        "coffee" to "Café / Snacks",
        "shopping_cart" to "Compras",
        "flight" to "Viajes",
        "fitness_center" to "Deporte",
        "pets" to "Mascotas",
        "build" to "Mantenimiento",
        "category" to "Otros"
    )

    val availableColors = listOf(
        0xFF10B981 to "Verde",
        0xFF0D9488 to "Teal",
        0xFF3B82F6 to "Azul",
        0xFF6366F1 to "Índigo",
        0xFF8B5CF6 to "Púrpura",
        0xFFEC4899 to "Rosa",
        0xFFEF4444 to "Rojo",
        0xFFF97316 to "Naranja",
        0xFFF59E0B to "Ámbar",
        0xFF84CC16 to "Lima",
        0xFF64748B to "Gris",
        0xFF78350F to "Café"
    )
}

@Composable
fun CategoryIcon(
    iconName: String,
    color: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Icon(
        imageVector = IconHelper.getIconVector(iconName),
        contentDescription = contentDescription,
        tint = color,
        modifier = modifier
    )
}
