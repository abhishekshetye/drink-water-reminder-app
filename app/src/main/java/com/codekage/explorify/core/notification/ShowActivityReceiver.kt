package com.codekage.explorify.core.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.codekage.explorify.MainActivity

/**
 * Created by abhisheksimac on 24/03/19.
 */
class ShowActivityReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        val notificationId = p1?.getIntExtra("NOTIFICATION_ID", 1)
        Log.d("NOTIFICATION", "Notif id is $notificationId")
        var startActivityIntent = Intent(p0, MainActivity::class.java)
        startActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        p0?.startActivity(startActivityIntent)
    }

}