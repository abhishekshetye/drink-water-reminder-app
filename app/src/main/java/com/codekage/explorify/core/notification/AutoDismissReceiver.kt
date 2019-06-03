package com.codekage.explorify.core.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created by abhisheksimac on 24/03/19.
 */

class AutoDismissReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        val notificationId = p1?.getIntExtra("NOTIFICATION_ID", 1)
        Log.d("NOTIFICATION", "Notif id is $notificationId")
        val manager = p0?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId!!)
    }

}