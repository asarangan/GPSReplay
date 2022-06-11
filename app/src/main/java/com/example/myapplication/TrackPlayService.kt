package com.example.myapplication

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat.stopForeground
import java.util.*


class TrackPlayService : Service() {

    var data1: Data = Data()
    val NOTIFICATION_ID: Int = 543
    var isServiceRunning: Boolean = false

    lateinit var locationManager: LocationManager
    lateinit var mockLocation: Location

    override fun onCreate() {
        Log.d(TAG, "TrackPlayService onCreate")
        super.onCreate()
        startTrackPlayService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "TrackPlayService onStartCommand")
//        if (intent != null && intent.getAction().equals("mytest")) {
//            startTrackPlayService()
//        } else {
//            stopTrackPlayService()
//        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "TrackPlayService onDestroy")
        isServiceRunning = false;
        super.onDestroy()
    }

    inner class TrackPlayServiceBinder : Binder() {
        fun getService(): TrackPlayService {
            Log.d(TAG, "TrackPlayService getService")
            return this@TrackPlayService
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.d(TAG, "TrackPlayService onBind")
        val trackPlayServiceBinder: TrackPlayServiceBinder = TrackPlayServiceBinder()
        return trackPlayServiceBinder
    }

    fun startTrackPlayService() {
        Log.d(TAG, "TrackPlayService startTrackPlayService")
        if (isServiceRunning) {
            return
        } else {
            isServiceRunning = true;
        }
        initGPS()

        Thread {
            while (true) {
                if ((data1.play) && (data1.currentPoint < data1.numOfPoints)) {
                    while (Date(data1.trackpoints[data1.currentPoint + 1].epoch).time + data1.deltaTime > System.currentTimeMillis()) {
                    }
                    Log.d(TAG, "${data1.currentPoint.toString()} ${System.currentTimeMillis()}")
                    data1.currentPoint++
                    mockGPSdata(data1.trackpoints[data1.currentPoint])
                }
            }
        }.start()
    }


    fun stopTrackPlayService() {
        Log.d(TAG, "TrackPlayService stopTrackPlayService")
        stopForeground(true)
        stopSelf()
        isServiceRunning = false
    }



    fun initGPS() {
        Log.d(TAG, "TrackPlayService initGPS")
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mockLocation = Location(LocationManager.GPS_PROVIDER)

        try {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        } catch (e: Exception) {
            e.printStackTrace()
        }
        locationManager.addTestProvider(
            LocationManager.GPS_PROVIDER,
            false,
            false,
            false,
            false,
            true,
            true,
            true,
            ProviderProperties.POWER_USAGE_HIGH,
            ProviderProperties.ACCURACY_FINE
        )

        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
        mockLocation.setElapsedRealtimeNanos(System.nanoTime())
        mockLocation.setAccuracy(5.0F)
    }

    fun mockGPSdata(trackpoint: Trackpoint) {
        mockLocation.setLatitude(trackpoint.lat)
        mockLocation.setLongitude(trackpoint.lon)
        mockLocation.setAltitude(trackpoint.altitude)
        mockLocation.setSpeed(trackpoint.speed)
        mockLocation.setBearing(trackpoint.bearing)
        mockLocation.setTime(trackpoint.epoch + data1.deltaTime)
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation)
    }


    fun setData(data: Data) {
        Log.d(TAG, "TrackPlayService setData")
        this.data1 = data.clone()
    }

    fun deleteGPS() {
        Log.d(TAG, "TrackPlayService deleteGPS")
        //val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (LocationManager.GPS_PROVIDER != null) {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        }
    }


}

//if (play && (numOfPoints > 0) && (index < numOfPoints - 1)) {
//    while (play && (Date(trackpoints!![index + 1].epoch).time + deltaTime > System.currentTimeMillis())) {
//    }
//    if (play) {//We need to check play again because it might have changed during the above idle loop
//        index += 1
//        runOnUiThread() {
//            updateDatafields()
//            mockGPSdata(trackpoints!![index])
//            if ((index*50.0/numOfPoints).toInt() > ((index-1)*50.0/numOfPoints).toInt()) {
//                seekBar.setProgress((index * 50.0 / numOfPoints).toInt())
//            }
//        }