package com.codekage.explorify.core.notification

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.codekage.explorify.R

/**
 * Created by abhisheksimac on 24/03/19.
 */

class NotificationHandler {


    private val CHANNELID = "1"
    private val CHANNEL_NAME = "com.codekage.explorify"
    private val NOTIFICATION_ID = 1



    @TargetApi(26)
    private fun createChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(CHANNELID, CHANNEL_NAME, importance)
        notificationChannel.enableVibration(true)
        notificationChannel.setShowBadge(true)
        //notificationChannel.enableLights(true)
        //notificationChannel.lightColor = Color.parseColor("#e8334a")
        notificationChannel.description = "Water Consumption Notification Channel"
        //notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(notificationChannel)
    }


    @TargetApi(Build.VERSION_CODES.O)
    fun setNotification(context: Context, content: String) {
        val closeIntent = Intent(context, AutoDismissReceiver::class.java)
        closeIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
        var dismissNotificationPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, closeIntent, 0)
        val dismissAction = NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "DISMISS", dismissNotificationPendingIntent).build()

        val openAppIntent = Intent(context, ShowActivityReceiver::class.java)
        closeIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
        var openAppPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, openAppIntent, 0)
        val drinkWaterAction = NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "DRINK WATER", openAppPendingIntent).build()

        createChannel(context)
        var builder = NotificationCompat.Builder(context, CHANNELID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setAutoCancel(false)
                .addAction(drinkWaterAction)
                .addAction(dismissAction)
                .setContentIntent(openAppPendingIntent)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentInfo(context.getString(R.string.notification_subtext))
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder)
    }

    fun closeNotification(context: Context){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }


}