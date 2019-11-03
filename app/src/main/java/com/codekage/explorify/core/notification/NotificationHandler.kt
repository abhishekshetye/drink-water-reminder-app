package com.codekage.explorify.core.notification

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.codekage.explorify.R
import java.util.*

/**
 * Created by abhisheksimac on 24/03/19.
 */

class NotificationHandler {



    companion object {

        private const val CHANNEL_ID = "1"
        private const val CHANNEL_NAME = "com.codekage.explorify"
        private const val NOTIFICATION_ID = 1
        private const val FIRST_TIME = "FIRST_TIME"
        private const val MORNING_TIME = 7


        @RequiresApi(Build.VERSION_CODES.O)
        private fun createChannel(context: Context, clannelID: String, channelName: String) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(clannelID, channelName, importance)
            notificationChannel.enableVibration(true)
            notificationChannel.setShowBadge(true)
            //notificationChannel.enableLights(true)
            //notificationChannel.lightColor = Color.parseColor("#e8334a")
            notificationChannel.description = "Water Consumption Notification Channel"
            //notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(notificationChannel)
        }


        fun setNotification(context: Context, title: String, contentInfo: String, content: String) {
            val closeIntent = Intent(context, AutoDismissReceiver::class.java)
            closeIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
            var dismissNotificationPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, closeIntent, 0)
            val dismissAction = NotificationCompat.Action.Builder(R.drawable.icon, "DISMISS", dismissNotificationPendingIntent).build()

            val openAppIntent = Intent(context, ShowActivityReceiver::class.java)
            closeIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
            var openAppPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, openAppIntent, 0)
            val drinkWaterAction = NotificationCompat.Action.Builder(R.drawable.icon, "DRINK WATER", openAppPendingIntent).build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createChannel(context, CHANNEL_ID, CHANNEL_NAME)
            var builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.watermeter_icon)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .addAction(drinkWaterAction)
                    .addAction(dismissAction)
                    .setContentIntent(openAppPendingIntent)
                    .setContentTitle(title) //context.getString(R.string.notification_title)
                    .setContentInfo(contentInfo) //context.getString(R.string.notification_subtext)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            Log.d("NOTIFICATION", "About to show notification")
            notificationManager.notify(NOTIFICATION_ID, builder)
        }



        fun setNotificationWithDelay(context: Context, title: String, contentInfo: String, content: String, withDelay: Int) {
            val closeIntent = Intent(context, AutoDismissReceiver::class.java)
            closeIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
            var dismissNotificationPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, closeIntent, 0)
            val dismissAction = NotificationCompat.Action.Builder(R.drawable.icon, "DISMISS", dismissNotificationPendingIntent).build()

            val openAppIntent = Intent(context, ShowActivityReceiver::class.java)
            closeIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
            var openAppPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, openAppIntent, 0)
            val drinkWaterAction = NotificationCompat.Action.Builder(R.drawable.icon, "DRINK WATER", openAppPendingIntent).build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createChannel(context, CHANNEL_ID, CHANNEL_NAME)
            var builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.watermeter_icon)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .addAction(drinkWaterAction)
                    .addAction(dismissAction)
                    .setContentIntent(openAppPendingIntent)
                    .setContentTitle(title)
                    .setContentInfo(contentInfo)
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


        fun setReminderToDrinkWaterEveryMorning(context: Context) {
            if(!isFirstTime(context))
                return
            var reminderIntent = Intent(context, ReminderBroadcastReceiver::class.java)
            var reminderPendingIntent = PendingIntent.getBroadcast(context, 0, reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            var alarmManager = context.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
            var timeToFireAlarm = getTimeForMorning()
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeToFireAlarm, AlarmManager.INTERVAL_DAY, reminderPendingIntent)
        }

        private fun isFirstTime(context: Context): Boolean {
            val sharedPrefs = context.getSharedPreferences(context.resources.getString(R.string.prefs_name), Context.MODE_PRIVATE)
            val firstTime = sharedPrefs.getBoolean(FIRST_TIME, true)
            Log.d("FIRST_TIME", firstTime.toString())
            val editor = sharedPrefs.edit()
            editor.putBoolean(FIRST_TIME, false)
            return firstTime
        }


        private fun getTimeForMorning(): Long{
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, MORNING_TIME)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            return calendar.timeInMillis
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