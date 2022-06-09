package com.example.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import androidx.annotation.MainThread
import com.google.android.material.internal.ContextUtils.getActivity
import java.util.*

class TrackPlayService : Service() {

    lateinit var data1: Data

    val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    val mockLocation: Location = Location(LocationManager.GPS_PROVIDER)

    fun initGPS() {

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

    fun mockGPSdata(trackpoint:Trackpoint){
        mockLocation.setLatitude(trackpoint.lat)
        mockLocation.setLongitude(trackpoint.lon)
        mockLocation.setAltitude(trackpoint.altitude)
        mockLocation.setSpeed(trackpoint.speed)
        mockLocation.setBearing(trackpoint.bearing)
        mockLocation.setTime(trackpoint.epoch+data1.deltaTime)
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation)
    }


    inner class TrackPlayServiceBinder : Binder() {
        fun getService(): TrackPlayService {
            return this@TrackPlayService
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        val trackPlayServiceBinder: TrackPlayServiceBinder = TrackPlayServiceBinder()
        return trackPlayServiceBinder
    }



    fun setData(data:Data){
        this.data1 = data
    }


//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        //return super.onStartCommand(intent, flags, startId)
//        //val tvPoint: TextView = runFragment.runFragmentView.findViewById<TextView>(R.id.tvPoint)
//
//        Thread(Runnable() {
//            while (true) {
//                if ((play)) {
//                    Log.d("GPS", (System.currentTimeMillis() / 1000).toString())
//                    Thread.sleep(1000)
////                    runFragment.currentPoint++
////                    runFragment.updatePointIndex(runFragment.currentPoint)
//                }
//            }
//        }).start()
//
//        return START_STICKY
//    }

    fun startPlayLoop() {










        Thread {
                while (true) {
                    if ((data1.play)&&(data1.currentPoint < data1.numOfPoints)) {
                        while (Date(data1.trackpoints[data1.currentPoint+1].epoch).time + data1.deltaTime > System.currentTimeMillis()){}
                        Log.d(TAG, "${data1.currentPoint.toString()} ${System.currentTimeMillis()}")
                        //Thread.sleep(1000)
                        data1.currentPoint++
                    }
                }
            }.start()
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