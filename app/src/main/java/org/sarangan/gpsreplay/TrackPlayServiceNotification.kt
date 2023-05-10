package org.sarangan.gpsreplay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class TrackPlayServiceNotification() {

    private val channelID = "TRACKPLAYSERVICE_CHANNEL_ID"
    private val channelName = "TRACKPLAYSERVICE_CHANNEL_NAME"


    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotification(message:String, trackPlayContext: Context): Notification {

        (trackPlayContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        )

        val aa: NotificationCompat.Builder =
            NotificationCompat.Builder(trackPlayContext, channelID)
        aa.setContentTitle(message)
        //aa.setContentText("Mock GPS is Running")
        aa.setSmallIcon(R.drawable.gpsreplayservice)
        aa.priority = NotificationCompat.PRIORITY_HIGH

        return aa.build()
    }
}