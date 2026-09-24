package com.example.ui.components

import java.text.NumberFormat
import java.util.Locale

object NumberFormatHelper {
    private val integerFormat = NumberFormat.getIntegerInstance(Locale("es", "CO"))

    /**
     * Formats numeric string with automatic thousand separators (dots) e.g. "1.000.000".
     * Supports optional comma as decimal separator.
     */
    fun formatWithThousands(input: String): String {
        val clean = input.filter { it.isDigit() || it == ',' }
        if (clean.isEmpty()) return ""

        return if (clean.contains(',')) {
            val parts = clean.split(',', limit = 2)
            val intDigits = parts[0].filter { it.isDigit() }
            val decDigits = parts.getOrNull(1)?.filter { it.isDigit() } ?: ""
            val formattedInt = if (intDigits.isEmpty()) {
                "0"
            } else {
                val num = intDigits.toLongOrNull() ?: return clean
                integerFormat.format(num)
            }
            if (decDigits.isNotEmpty()) "$formattedInt,$decDigits" else "$formattedInt,"
        } else {
            val intDigits = clean.filter { it.isDigit() }
            if (intDigits.isEmpty()) return ""
            val num = intDigits.toLongOrNull() ?: return clean
            integerFormat.format(num)
        }
    }

    /**
     * Formats a Double amount as currency string, e.g. "$ 1.500.000 COP".
     */
    fun formatCurrency(amount: Double, currency: String = "COP"): String {
        val formatted = integerFormat.format(amount.toLong())
        return "$ $formatted $currency"
    }

    /**
     * Parses formatted currency string (e.g. "1.500.000" or "1.500,50") to Double.
     */
    fun parseToDouble(formatted: String): Double {
        if (formatted.isBlank()) return 0.0
        val normalized = if (formatted.contains(',')) {
            formatted.replace(".", "").replace(",", ".")
        } else {
            formatted.replace(".", "")
        }
        return normalized.toDoubleOrNull() ?: 0.0
    }
}
