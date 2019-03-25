package com.codekage.explorify.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.codekage.explorify.MainActivity
import com.codekage.explorify.core.notification.NotificationHandler.Companion.setNotification

/**
 * Created by abhisheksimac on 24/03/19.
 */
class ShowActivityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getIntExtra("NOTIFICATION_ID", 1)
        Log.d("NOTIFICATION", "Notif id is $notificationId")
        var startActivityIntent = Intent(context, MainActivity::class.java)
        startActivityIntent.putExtra("FROM_NOTIFICATION", true)
        startActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context?.startActivity(startActivityIntent)
    }

}