package com.example.ui.components

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.CardReminderReceiver
import com.example.data.CreditCardReminderEntity
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "card_fee_reminders_channel"
    const val CHANNEL_NAME = "Recordatorios de Tarjetas y Cuotas"
    const val CHANNEL_DESC = "Alertas para cuotas de manejo, fechas de corte y pago (7d, 3d, 1d y mismo día)"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun sendNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_card)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted yet on Android 13+
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Calculates the remaining calendar days between today and target day of the month (1..31).
     * If the day already passed this month, rolls to next month.
     */
    fun getDaysUntilDayOfMonth(dayOfMonth: Int): Int {
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)

        val target = Calendar.getInstance()
        target.set(Calendar.HOUR_OF_DAY, 0)
        target.set(Calendar.MINUTE, 0)
        target.set(Calendar.SECOND, 0)
        target.set(Calendar.MILLISECOND, 0)

        val maxDayThisMonth = target.getActualMaximum(Calendar.DAY_OF_MONTH)
        target.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(maxDayThisMonth))

        if (target.timeInMillis < now.timeInMillis) {
            target.add(Calendar.MONTH, 1)
            val maxDayNextMonth = target.getActualMaximum(Calendar.DAY_OF_MONTH)
            target.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(maxDayNextMonth))
        }

        val diff = target.timeInMillis - now.timeInMillis
        return (diff / (24 * 3600 * 1000L)).toInt()
    }

    /**
     * Checks if today matches 7 days, 3 days, 1 day (yesterday) or same day (0)
     * for cut-off date and payment date of the user's cards, and sends notifications.
     */
    fun checkAndNotifyCards(context: Context, cards: List<CreditCardReminderEntity>) {
        cards.filter { it.reminderEnabled }.forEach { card ->
            // Check Cut-off
            if (card.notifyCutOff) {
                val cutDays = getDaysUntilDayOfMonth(card.cutOffDay)
                when (cutDays) {
                    7 -> sendNotification(
                        context,
                        (card.id * 100 + 7).toInt(),
                        "⏰ Fecha de corte en 7 días: ${card.cardName}",
                        "Faltan 7 días para el corte de tu tarjeta (Día ${card.cutOffDay}). Revisa tus consumos antes del cierre del ciclo."
                    )
                    3 -> sendNotification(
                        context,
                        (card.id * 100 + 3).toInt(),
                        "⏰ Fecha de corte en 3 días: ${card.cardName}",
                        "En 3 días cerrará el extracto de tu tarjeta ${card.cardName} (Día ${card.cutOffDay})."
                    )
                    1 -> sendNotification(
                        context,
                        (card.id * 100 + 1).toInt(),
                        "⚠️ ¡Mañana es tu fecha de corte!: ${card.cardName}",
                        "Mañana (Día ${card.cutOffDay}) es el corte de tu tarjeta. A partir de pasado mañana podrás comprar con hasta 45-50 días de financiamiento gratis."
                    )
                    0 -> sendNotification(
                        context,
                        (card.id * 100 + 0).toInt(),
                        "🔔 ¡Hoy es tu fecha de corte!: ${card.cardName}",
                        "Hoy cierra la facturación de tu ${card.cardName}. Recuerda que a partir de mañana es el mejor momento para realizar compras a 1 cuota."
                    )
                }
            }

            // Check Payment
            if (card.notifyPayment) {
                val payDays = getDaysUntilDayOfMonth(card.paymentDay)
                val feeInfo = if (!card.isExonerated && card.feeAmount > 0) " Recuerda cubrir la cuota de manejo." else ""
                when (payDays) {
                    7 -> sendNotification(
                        context,
                        (card.id * 100 + 27).toInt(),
                        "💳 Fecha de pago en 7 días: ${card.cardName}",
                        "Faltan 7 días para la fecha límite de pago (Día ${card.paymentDay}).$feeInfo"
                    )
                    3 -> sendNotification(
                        context,
                        (card.id * 100 + 23).toInt(),
                        "💳 Fecha de pago en 3 días: ${card.cardName}",
                        "En 3 días vence el pago de tu tarjeta ${card.cardName} (Día ${card.paymentDay}). Prepara tus fondos para pagar a tiempo.$feeInfo"
                    )
                    1 -> sendNotification(
                        context,
                        (card.id * 100 + 21).toInt(),
                        "⚠️ ¡Mañana vence el pago de tu tarjeta!: ${card.cardName}",
                        "Mañana (Día ${card.paymentDay}) es el último día para pagar tu ${card.cardName} sin generar intereses por mora.$feeInfo"
                    )
                    0 -> sendNotification(
                        context,
                        (card.id * 100 + 20).toInt(),
                        "🚨 ¡HOY es la fecha límite de pago!: ${card.cardName}",
                        "¡Hoy es el último día para pagar tu ${card.cardName}! Paga antes de las 11:59 PM para evitar cobros adicionales de intereses moratorios.$feeInfo"
                    )
                }
            }
        }
    }

    /**
     * Schedules alarms for exact upcoming milestones (7d, 3d, 1d, 0d) using AlarmManager
     */
    fun scheduleAlarms(context: Context, cards: List<CreditCardReminderEntity>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val milestones = listOf(7, 3, 1, 0)

        cards.filter { it.reminderEnabled }.forEach { card ->
            milestones.forEach { daysBefore ->
                if (card.notifyCutOff) {
                    scheduleMilestoneAlarm(context, alarmManager, card, isCutOff = true, daysBefore = daysBefore)
                }
                if (card.notifyPayment) {
                    scheduleMilestoneAlarm(context, alarmManager, card, isCutOff = false, daysBefore = daysBefore)
                }
            }
        }
    }

    private fun scheduleMilestoneAlarm(
        context: Context,
        alarmManager: AlarmManager,
        card: CreditCardReminderEntity,
        isCutOff: Boolean,
        daysBefore: Int
    ) {
        val targetDay = if (isCutOff) card.cutOffDay else card.paymentDay
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, targetDay.coerceAtMost(maxDay))
            add(Calendar.DAY_OF_MONTH, -daysBefore)
        }

        if (cal.timeInMillis < System.currentTimeMillis()) {
            cal.add(Calendar.MONTH, 1)
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, targetDay.coerceAtMost(maxDay))
            cal.add(Calendar.DAY_OF_MONTH, -daysBefore)
        }

        val requestCode = (card.id * 1000 + (if (isCutOff) 100 else 200) + daysBefore).toInt()
        val intent = Intent(context, CardReminderReceiver::class.java).apply {
            putExtra("card_id", card.id)
            putExtra("card_name", card.cardName)
            putExtra("is_cut_off", isCutOff)
            putExtra("days_before", daysBefore)
            putExtra("target_day", targetDay)
            putExtra("fee_amount", card.feeAmount)
            putExtra("is_exonerated", card.isExonerated)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    /**
     * Sends an instant test notification for any requested milestone
     */
    fun triggerTestNotification(
        context: Context,
        card: CreditCardReminderEntity,
        isCutOff: Boolean,
        daysBefore: Int
    ) {
        val typeLabel = if (isCutOff) "fecha de corte (Día ${card.cutOffDay})" else "fecha de pago (Día ${card.paymentDay})"
        val feeNotice = if (!isCutOff && !card.isExonerated && card.feeAmount > 0) " Recuerda pagar la cuota de manejo." else ""

        val (title, content) = when (daysBefore) {
            7 -> Pair(
                "⏰ 7 días antes para $typeLabel - ${card.cardName}",
                "Faltan 7 días para la $typeLabel de tu tarjeta ${card.cardName}.$feeNotice"
            )
            3 -> Pair(
                "⏰ 3 días antes para $typeLabel - ${card.cardName}",
                "Faltan solo 3 días para la $typeLabel de tu tarjeta ${card.cardName}.$feeNotice"
            )
            1 -> Pair(
                "⚠️ ¡Mañana es la $typeLabel! - ${card.cardName}",
                "Atención: Mañana vence la $typeLabel de tu tarjeta ${card.cardName}.$feeNotice"
            )
            else -> Pair(
                "🚨 ¡HOY es la $typeLabel! - ${card.cardName}",
                "¡Hoy es el día clave de la $typeLabel de tu tarjeta ${card.cardName}! No olvides verificar tus estados y pagos.$feeNotice"
            )
        }

        val id = (System.currentTimeMillis() % 100000).toInt()
        sendNotification(context, id, title, content)
    }
}
