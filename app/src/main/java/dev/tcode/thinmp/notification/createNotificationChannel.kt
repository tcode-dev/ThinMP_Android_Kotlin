package dev.tcode.thinmp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

fun createNotificationChannel(context: Context) {
    val name = "channelName"
    val descriptionText = "channelDescription"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
        description = descriptionText
    }
    val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.createNotificationChannel(channel)
}