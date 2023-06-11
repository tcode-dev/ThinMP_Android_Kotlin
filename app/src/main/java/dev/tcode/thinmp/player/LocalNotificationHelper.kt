package dev.tcode.thinmp.player

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.session.MediaStyleNotificationHelper
import dev.tcode.thinmp.R

object LocalNotificationHelper {

    private const val CHANNEL_ID = "your_channel_id"
    private const val CHANNEL_NAME = "Your Channel Name"
    private const val CHANNEL_DESCRIPTION = "Your Channel Description"

    fun showNotification(context: Context, mediaStyle: MediaStyleNotificationHelper.MediaStyle, title: String, message: String, albumArtBitmap: Bitmap?) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.round_audiotrack_24).setStyle(mediaStyle).setContentTitle(title).setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true)
        if (albumArtBitmap != null) {
            builder.setLargeIcon(albumArtBitmap)
        }

        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.notify(0, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = CHANNEL_DESCRIPTION

        val notificationManager = context.getSystemService(NotificationManager::class.java)

        notificationManager.createNotificationChannel(channel)
    }
}
