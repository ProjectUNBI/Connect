package com.unbi.connect.uiclasses

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.unbi.connect.NOTIFICATION_CHANNEL_ID


class OngoingNotificationBuilder {
    fun buildOngoingNotification(
        context: Context,
        iconId: Int,
        title: String,
        body: String,
        isAutoCancel: Boolean = false,
        deletIntent: PendingIntent?=null,
        clickIntent:PendingIntent?=null
    ): Notification? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "My Background Service"
            val chan = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager =
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)
            if (manager != null) {
                manager.createNotificationChannel(chan)

                val notificationbuilder =
                    NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setOngoing(true)
                        .setSmallIcon(iconId)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationManager.IMPORTANCE_MIN)
                        .setCategory(Notification.CATEGORY_SERVICE)
                if (isAutoCancel) {
                    notificationbuilder.setAutoCancel(true)
                    notificationbuilder.setContentIntent(clickIntent)
                }
                if (deletIntent!=null){
                    notificationbuilder.setDeleteIntent(deletIntent)
                }
                    return notificationbuilder.build()
            }

        } else {
            val notificationbuilder = Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(iconId)
            if (isAutoCancel) {
                notificationbuilder.setAutoCancel(true)
                notificationbuilder.setContentIntent(clickIntent)
            }
            if (deletIntent!=null){
                notificationbuilder.setDeleteIntent(deletIntent)
            }
            return notificationbuilder.build()
        }
        return null
    }


}