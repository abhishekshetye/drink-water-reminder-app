package com.codekage.explorify.core.notification

import android.app.AlarmManager
import android.app.AlarmManager.INTERVAL_HOUR
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import java.util.*


//delete this class
class HourlyReminder {


    companion  object {
        private const val DAILY_REMINDER_REQUEST_CODE: Int = 430
        private const val NOTIFICATION_ID: Int = 432
        private const val CHANNEL_ID = "323"
        private const val CHANNEL_NAME = "HourlyReminderChannel"
        private const val TAG = "ALARMFIX"


        fun setReminder(context: Context, cls: Class<*>, hour: Int, min: Int) {
            val calendar = Calendar.getInstance()
            val timeToFirmAlarm = Calendar.getInstance()
            timeToFirmAlarm.set(Calendar.HOUR_OF_DAY, hour)
            timeToFirmAlarm.set(Calendar.MINUTE, min)
            timeToFirmAlarm.set(Calendar.SECOND, 0)
            // cancel already scheduled reminders
            cancelReminder(context, cls)

            if (timeToFirmAlarm.before(calendar)) {
                timeToFirmAlarm.add(Calendar.DATE, 1)
                Log.d(TAG, "Time is set before the current instance")
            }

            // Enable a receiver
            val receiver = ComponentName(context, cls)
            val pm = context.packageManager
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)

            val intent1 = Intent(context, cls)
            val pendingIntent = PendingIntent.getBroadcast(context,
                    DAILY_REMINDER_REQUEST_CODE, intent1,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeToFirmAlarm.timeInMillis,
                    2 *AlarmManager.INTERVAL_HOUR,
                    pendingIntent)
            Log.d(TAG, "Reminder set for " + timeToFirmAlarm.time )
        }

        fun setReminderInstantly(context: Context, cls: Class<*>) {
            val timeToFirmAlarm = Calendar.getInstance()
            // cancel already scheduled reminders
            cancelReminder(context, cls)

            // Enable a receiver
            val receiver = ComponentName(context, cls)
            val pm = context.packageManager
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)

            val intent1 = Intent(context, cls)
            val pendingIntent = PendingIntent.getBroadcast(context,
                    DAILY_REMINDER_REQUEST_CODE, intent1,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeToFirmAlarm.timeInMillis + 1 * INTERVAL_HOUR,
                    2 * INTERVAL_HOUR,
                    pendingIntent)
            Log.d(TAG, "Reminder set for " + timeToFirmAlarm.time )
        }

        fun cancelReminder(context: Context, cls: Class<*>) {
            // Disable a receiver
            val receiver = ComponentName(context, cls)
            val pm = context.packageManager
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP)

            val intent1 = Intent(context, cls)
            val pendingIntent = PendingIntent.getBroadcast(context,
                    DAILY_REMINDER_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
            val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
            am.cancel(pendingIntent)
            pendingIntent.cancel()
        }

    }


}