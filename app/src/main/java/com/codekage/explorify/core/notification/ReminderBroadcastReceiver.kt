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

        if (context != null) {
            NotificationHandler.setNotification(context, context.getString(R.string.notification_title), context.getString(R.string.notification_subtext),"Reminder : Drink Water")
        } else {
            Log.d("ALARMFIX", "Context is null")
        }

        Log.d("ALARMFIX", "Notified")
    }

}