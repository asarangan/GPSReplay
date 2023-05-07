package org.sarangan.gpsreplay

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
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
        val gpsUpdateInterval:Int = 100 //Update every 100ms
        var ticks:Int   //This is the counter to indicate how many 100ms intervals have lapsed between each GPS data
        startForeground(1, notification)
        Thread {    //This is the service thread
            trackPlayServiceIsRunning = true    //Enable the status flag
            Log.d(TAG, "Track Play Service has been started")
            initGPS()
            //This is the main loop. It runs while play is active, and the keep incrementing the stack pointer until we reach the last data point.
            while ((data.play) && (data.currentPoint < data.numOfPoints)) {
                //This loop waits until the current time (plus the offset) catches up with the time stamp of the next GPS point
                //While we wait, we don't want the GPS data to show nothing. So we can keep transmitting the current GPS point every 100ms
                val nextWayPointTime:Long = Date(data.trackPoints[data.currentPoint + 1].epoch).time + data.timeOffset
                ticks = 1
                while (nextWayPointTime > System.currentTimeMillis()) { //Wait loop
                    if ((nextWayPointTime-System.currentTimeMillis())/gpsUpdateInterval > ticks){
                        ticks++
                        Log.d(TAG, "Point number: ${data.currentPoint}. System time: ${System.currentTimeMillis()}")
                        mockGPSdata(data.trackPoints[data.currentPoint])
                    }
                }
                Log.d(TAG, "Point number: ${data.currentPoint}. System time: ${System.currentTimeMillis()}")
                data.currentPoint++ //Increment the stack pointer
                mockGPSdata(data.trackPoints[data.currentPoint])    //This is where we send the data to the GPS mock
            }
            //The above loop ends when either the play becomes inactive (by user input via Seek Bar or Button), or we reach the end of the data
            //Stop the service. This means, onDestroy of the service will get called.
            stopForeground(STOP_FOREGROUND_REMOVE)
            //We also want to delete the location manager because leaving it hanging can cause problems with other apps
            deleteGPS()
            Log.d(TAG, "Track Play Service has been stopped")
            trackPlayServiceIsRunning = false
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

    private fun mockGPSdata(trackpoint: TrackPoint) {
        mockLocation.setLatitude(trackpoint.lat)
        mockLocation.setLongitude(trackpoint.lon)
        mockLocation.setAltitude(trackpoint.altitude)
        mockLocation.setSpeed(trackpoint.speed)
        mockLocation.setBearing(trackpoint.trueCourse)
        mockLocation.setTime(trackpoint.epoch + data.timeOffset)
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation)
    }


    private fun deleteGPS() {
        Log.d(TAG, "TrackPlayService deleteGPS")
        //val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (LocationManager.GPS_PROVIDER != null) {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        }
    }
}
