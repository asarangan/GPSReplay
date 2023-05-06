package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*

class TrackPlayService : Service() {

    lateinit var locationManager: LocationManager
    lateinit var mockLocation: Location

    override fun onCreate() {
        Log.d(TAG, "TrackPlayService onCreate")
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "TrackPlayService onStartCommand")
        val notification = TrackPlayServiceNotification().getNotification(applicationContext)
        startForeground(1, notification)
        Thread {
            while (data.play) {
                data.currentPoint++
                Thread.sleep(100)
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
        }.start()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "TrackPlayService onDestroy")
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun startTrackPlayService() {
        Log.d(TAG, "TrackPlayService startTrackPlayService")
        initGPS()

        Thread {
            while (true) {
                if ((data.play) && (data.currentPoint < data.numOfPoints)) {
                    while (Date(data.trackPoints[data.currentPoint + 1].epoch).time + data.deltaTime > System.currentTimeMillis()) {
                    }
                    Log.d(TAG, "${data.currentPoint.toString()} ${System.currentTimeMillis()}")
                    data.currentPoint++
                    mockGPSdata(data.trackPoints[data.currentPoint])
                }
            }
        }.start()
    }


    private fun initGPS() {
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

    fun mockGPSdata(trackpoint: TrackPoint) {
        mockLocation.setLatitude(trackpoint.lat)
        mockLocation.setLongitude(trackpoint.lon)
        mockLocation.setAltitude(trackpoint.altitude)
        mockLocation.setSpeed(trackpoint.speed)
        mockLocation.setBearing(trackpoint.trueCourse)
        mockLocation.setTime(trackpoint.epoch + data.deltaTime)
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation)
    }


    fun setData(data: Data) {
        Log.d(TAG, "TrackPlayService setData")
        //data = data.clone()
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