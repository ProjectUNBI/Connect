package com.unbi.connect.uiclasses

import android.R
import android.R.drawable
import android.app.*
import android.support.v4.app.NotificationCompat
import android.content.Context.NOTIFICATION_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.os.Build
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.unbi.connect.NOTIFICATION_CHANNEL_ID
import com.unbi.connect.fragment.BaseFragMent
import kotlin.reflect.KClass


class OngoingNotificationBuilder {


    inline fun <reified T : Service> buildOngoingNotification(
        context: Context,
        kClass: KClass<T>,
        iconId: Int,
        title: String,
        body: String
    ): Notification? {
        val notificationIntent = Intent(context, kClass::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "My Background Service"
            val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)!!
            manager.createNotificationChannel(chan)

            val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            val notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(iconId)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
            return notification
        } else {
            val notification = Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(iconId)
                .setContentIntent(pendingIntent)
                .build()

            return notification
        }
    }


}