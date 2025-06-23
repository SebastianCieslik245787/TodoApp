package com.example.todoapp

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    var sharedPref: SharedPreferences? = context.getSharedPreferences("settings", MODE_PRIVATE)

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleNotification(task: Task) {
        cancelNotification(task)

        if (!areNotificationsEnabled()) {
            Log.d(
                "NotificationScheduler", "Powiadomienia globalnie wyłączone. ${task.id}"
            )
            return
        }

        val notificationTimeMillis = getNotificationTimeInMillis(task)

        if (notificationTimeMillis == null) {
            Log.d("NotificationScheduler", "Brak czasu powiadomienia dla taska ${task.id}")
            return
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTimeMillis,
                        pendingIntent
                    )
                    Log.d(
                        "NotificationScheduler", "Zaplanowano powiadomienie dla taska ${task.id} na $notificationTimeMillis"
                    )
                } else {
                    Log.d("NotificationScheduler", "Brak uprawnień SCHEDULE_EXACT_ALARM.")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTimeMillis,
                    pendingIntent
                )
                Log.d(
                    "NotificationScheduler", "Zaplanowano powiadomienie dla taska ${task.id} na $notificationTimeMillis"
                )
            }
        } catch (e: SecurityException) {
            Log.e("NotificationScheduler", "Brak uprawnień do ustawienia alarmu.", e)
        }
    }

    private fun getNotificationTimeInMillis(task: Task): Long? {
        if (task.notificationDate != null && task.notificationTime != null) {
            return convertDateAndTimeToMillis(task.notificationDate!!, task.notificationTime!!)
        }

        if (task.notificationDate == null && task.notificationTime == null) {
            val plannedTimeMillis = convertDateAndTimeToMillis(task.planedDate, task.planedTime)
            if(sharedPref == null) return null
            val hours: String = sharedPref!!.getString("hours", "0").toString()
            val minutes: String = sharedPref!!.getString("minutes", "1").toString()

            val offset = convertTimeToMillis(hours, minutes)
            if(plannedTimeMillis > 0 && offset > 0){
                return plannedTimeMillis - offset
            }
        }

        return null
    }

    fun cancelNotification(task: Task) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("NotificationScheduler", "Anulowano powiadomienie dla taska ${task.id}")
        }
    }

    private fun convertDateAndTimeToMillis(date: String, time: String): Long {
        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm")

        val dateTimeStr = "$date $time"
        val localDateTime = LocalDateTime.parse(dateTimeStr, formatter)
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun convertTimeToMillis(hours: String, minutes: String): Long {
        val hoursInMillis = hours.toLongOrNull()?.times(3600000L) ?: 0L
        val minutesInMillis = minutes.toLongOrNull()?.times(60000L) ?: 0L
        return hoursInMillis + minutesInMillis
    }

    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}