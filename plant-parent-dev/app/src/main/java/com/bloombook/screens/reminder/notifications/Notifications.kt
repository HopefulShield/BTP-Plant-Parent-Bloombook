package com.bloombook.screens.reminder.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.app.TaskInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker.Result.success
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bloombook.R
import com.bloombook.screens.MainActivity

class Notifications: Application() {

    override fun onCreate() {
        super.onCreate()
        //createNotificationChannel()
    }
/*
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NotifService.REMINDER_CHANNEL_ID,
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Reminder For Users Plants"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Reminder For Users Plants"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }
}*/
    companion object{
        const val REMINDER_CHANNEL_ID = "Reminder_Data"
    }
}
