package com.bloombook.screens.reminder.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.bloombook.R
import com.bloombook.screens.reminder.editReminder.EditReminderState
import java.time.ZoneId

class NotificationScheduler(
    private val context: Context
) {
    private val manager = context.getSystemService(AlarmManager::class.java)


    fun schedule(item: EditReminderState, docID: String, name: String) {
        Log.d("hash1", docID.hashCode().toString())

        val dateTime = item.date.atTime(item.time)
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("REMINDER_NAME", name)
            putExtra("REMINDER_MESSAGE", item.message)
        }
           if (item.interval == "Once") {
             manager.setExact(
                AlarmManager.RTC_WAKEUP,
                dateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
                PendingIntent.getBroadcast(
                    context,
                    docID.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
             )
        }
        else {
            // default is daily so just use the regular time value: 24*60*60*1000
            var time: Long = 24*60*60*1000
            if (item.interval == "Weekly")
                time *= 7
            else if (item.interval == "Monthly")
                time *= 30

            manager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                dateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
                time,
                PendingIntent.getBroadcast(
                    context,
                    docID.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }

    fun cancel(docID: String, message: String, name: String){
        Log.d("hash2", docID.hashCode().toString())
        manager.cancel(
            PendingIntent.getBroadcast(
                context,
                docID.hashCode(),
                Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("REMINDER_NAME", name)
                    putExtra("REMINDER_MESSAGE", message)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val name = intent?.getStringExtra("REMINDER_NAME")?: return
        val message = intent?.getStringExtra("REMINDER_MESSAGE")?: return

        context?.let { ctx ->
            val notificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(ctx, "Reminder_Data")
                .setSmallIcon(R.drawable.baseline_local_florist_24)
                .setContentTitle(name)
                .setContentText(message)
                .setColor(Color(0xFF8DE39E).toArgb())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                //.addAction(0, "Done", intent)
            notificationManager.notify(1, builder.build())
        }
    }
}
