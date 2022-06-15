package com.example.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class GPSNotification() {

    val CHANNEL_ID = "GPS_CHANNEL_ID"
    val CHANNEL_NAME = "GPS_CHANNEL_NAME"

    fun getNotification(GPScontext: Context) {

    val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(GPScontext)

    val myNotification: Notification = NotificationCompat.Builder(GPScontext, CHANNEL_ID)
        .setContentTitle("GPS Replay")
        .setContentText("is running")
        .setSmallIcon(R.drawable.ic_notification_foreground)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            channel.lightColor = Color.GREEN
            channel.enableLights(true)
            (GPScontext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).apply {
                createNotificationChannel(channel)
            }
        } else {
            TODO("VERSION.SDK_INT < O")
        }
    }
}