package com.codekage.explorify.core.notification

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.codekage.explorify.R
import java.util.*

/**
 * Created by abhisheksimac on 24/03/19.
 */

class NotificationHandler {



    companion object {

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
            val dismissAction = NotificationCompat.Action.Builder(R.drawable.icon, "DISMISS", dismissNotificationPendingIntent).build()

            val openAppIntent = Intent(context, ShowActivityReceiver::class.java)
            closeIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
            var openAppPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, openAppIntent, 0)
            val drinkWaterAction = NotificationCompat.Action.Builder(R.drawable.icon, "DRINK WATER", openAppPendingIntent).build()

            createChannel(context)
            var builder = NotificationCompat.Builder(context, CHANNELID)
                    .setSmallIcon(R.drawable.icon)
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
            Log.d("NOTIFICATION", "About to show notification")
            notificationManager.notify(NOTIFICATION_ID, builder)
        }



        @TargetApi(Build.VERSION_CODES.O)
        fun setNotification(context: Context, content: String, withDelay: Int) {
            val closeIntent = Intent(context, AutoDismissReceiver::class.java)
            closeIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
            var dismissNotificationPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, closeIntent, 0)
            val dismissAction = NotificationCompat.Action.Builder(R.drawable.icon, "DISMISS", dismissNotificationPendingIntent).build()

            val openAppIntent = Intent(context, ShowActivityReceiver::class.java)
            closeIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
            var openAppPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, openAppIntent, 0)
            val drinkWaterAction = NotificationCompat.Action.Builder(R.drawable.icon, "DRINK WATER", openAppPendingIntent).build()

            createChannel(context)
            var builder = NotificationCompat.Builder(context, CHANNELID)
                    .setSmallIcon(R.drawable.icon)
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
            Log.d("NOTIFICATION", "About to show notification")
            object:CountDownTimer(withDelay.toLong(), withDelay.toLong()){
                override fun onFinish() {
                    notificationManager.notify(NOTIFICATION_ID, builder)
                }
                override fun onTick(p0: Long) {}
            }.start()
        }


        fun setReminderToDrinkWaterEveryCoupleHours(context: Context) {
            var reminderIntent = Intent(context, ReminderBroadcastReceiver::class.java)
            var reminderPendingIntent = PendingIntent.getBroadcast(context, 0, reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            var alarmManager = context.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
            var timeToFireAlarm = getTimeAfterCoupleHours()
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeToFireAlarm, reminderPendingIntent)
        }


        private fun getTimeAfterCoupleHours(): Long {
            var calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.HOUR, 2)
            var date = calendar.time
            return date.time
        }




        fun closeNotification(context: Context){
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }
    }




}