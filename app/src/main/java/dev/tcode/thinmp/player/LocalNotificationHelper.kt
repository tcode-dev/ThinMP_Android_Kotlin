package dev.tcode.thinmp.player

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.session.MediaStyleNotificationHelper
import dev.tcode.thinmp.R

object LocalNotificationHelper {

    private const val CHANNEL_ID = "your_channel_id"
    private const val CHANNEL_NAME = "Your Channel Name"
    private const val CHANNEL_DESCRIPTION = "Your Channel Description"

    fun showNotification(context: Context, mediaStyle:  MediaStyleNotificationHelper.MediaStyle, title: String, message: String, albumArtBitmap: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.round_play_arrow_24)) {
        createNotificationChannel(context)

        val b = BitmapFactory.decodeResource(context.resources, R.drawable.round_play_arrow_24)
        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.round_favorite_24)
                .setStyle(mediaStyle)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(albumArtBitmap)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        val notificationManager = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
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
