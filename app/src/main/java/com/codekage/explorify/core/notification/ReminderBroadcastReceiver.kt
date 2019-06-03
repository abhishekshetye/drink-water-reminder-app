package com.codekage.explorify.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.codekage.explorify.R

/**
 * Created by abhisheksimac on 25/03/19.
 */
class ReminderBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NOTIFICATION", "Reminder! Drink water")
        val sharedPrefs = context?.getSharedPreferences(context.resources.getString(R.string.prefs_name), Context.MODE_PRIVATE)
        var featureEnabled = sharedPrefs?.getBoolean(context.getString(R.string.reminderEnabled), false)
        if(featureEnabled!!) {
            context?.let { NotificationHandler.setReminderToDrinkWaterEveryCoupleHours(it) }
            context?.let { NotificationHandler.setNotification(it, "Water Drinking Reminder") }
        }
    }

}