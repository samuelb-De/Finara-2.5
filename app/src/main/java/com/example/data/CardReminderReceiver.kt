package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ui.components.NotificationHelper

class CardReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val cardId = intent.getLongExtra("card_id", 0L)
        val cardName = intent.getStringExtra("card_name") ?: "Tarjeta"
        val isCutOff = intent.getBooleanExtra("is_cut_off", true)
        val daysBefore = intent.getIntExtra("days_before", 0)
        val targetDay = intent.getIntExtra("target_day", 15)
        val feeAmount = intent.getDoubleExtra("fee_amount", 0.0)
        val isExonerated = intent.getBooleanExtra("is_exonerated", false)

        val typeLabel = if (isCutOff) "fecha de corte (Día $targetDay)" else "fecha de pago (Día $targetDay)"
        val feeNotice = if (!isCutOff && !isExonerated && feeAmount > 0) " Recuerda pagar la cuota de manejo." else ""

        val (title, content) = when (daysBefore) {
            7 -> Pair(
                "⏰ 7 días antes para $typeLabel: $cardName",
                "Faltan 7 días para tu $typeLabel.$feeNotice Revisa tus movimientos en Finara."
            )
            3 -> Pair(
                "⏰ 3 días antes para $typeLabel: $cardName",
                "En 3 días vence tu $typeLabel.$feeNotice Prepara tus cuentas."
            )
            1 -> Pair(
                "⚠️ ¡Mañana es la $typeLabel!: $cardName",
                "Mañana es tu $typeLabel.$feeNotice Evita intereses o retrasos."
            )
            else -> Pair(
                "🚨 ¡HOY es tu $typeLabel!: $cardName",
                "¡Hoy es el día límite de tu $typeLabel!$feeNotice Cumple con tu pago a tiempo."
            )
        }

        val notificationId = (cardId * 1000 + (if (isCutOff) 100 else 200) + daysBefore).toInt()
        NotificationHelper.sendNotification(context, notificationId, title, content)
    }
}
