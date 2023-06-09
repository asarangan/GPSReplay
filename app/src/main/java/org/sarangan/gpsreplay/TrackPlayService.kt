package org.sarangan.gpsreplay

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.TextView
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
        val gpsUpdateInterval: Int = 100 //Update every 100ms
        var ticks: Int   //This is the counter to indicate how many 100ms intervals have lapsed between each GPS data

        try {   //If initGPS crashes, that means this app has not been added to the mock gps permission list
            initGPS()
            trackPlayServiceIsRunning = true    //Enable the status flag
            val notification = TrackPlayServiceNotification().getNotification(
                "GPS Replay is Running",
                applicationContext
            )
            startForeground(1, notification)    //Start foreground with notification.
            Log.d(TAG, "Track Play Service has been started")
        } catch (e: SecurityException) {//if initGPS crashes because mock GPS has not been enabled, disable play and enable mockGPSEnabled.
            data.play = false
            data.mockGPSEnabled =
                false //This will trigger a message on the screen to enable mock GPS under updateTrackPosition in RunFragment
            val notification = TrackPlayServiceNotification().getNotification(
                "Mock GPS is not enabled",
                applicationContext
            )
            startForeground(1, notification)
            Log.d(TAG, "Track Play Service Security Exception")
        }

        Thread {    //This is the service thread
            //This is the main loop. It runs while play is active, and the keep incrementing the stack pointer until we reach the last data point.
            //If mock GPS was disabled, this loop won't run
            while ((data.play) && (data.currentPoint < data.numOfPoints-1)) {
                //This loop waits until the current time (plus the offset) catches up with the time stamp of the next GPS point
                //While we wait, we don't want the GPS data to show nothing. So we can keep transmitting the current GPS point every 100ms
                val nextWayPointTime: Long =
                    Date(data.trackPoints[data.currentPoint + 1].epoch).time + data.timeOffset
                ticks = 1
                while (nextWayPointTime > System.currentTimeMillis()) { //Wait loop
                    if ((nextWayPointTime - System.currentTimeMillis()) / gpsUpdateInterval > ticks) {
                        ticks++
                        mockGPSdata(data.trackPoints[data.currentPoint])
                    }
                }
                data.currentPoint++ //Increment the stack pointer
                mockGPSdata(data.trackPoints[data.currentPoint])    //This is where we send the data to the GPS mock
            }
            //The above loop ends when either the play becomes inactive (by user input via Seek Bar or Button), or we reach the end of the data
            //Stop the service. This means, onDestroy of the service will get called.
            //We also want to delete the location manager because leaving it hanging can cause problems with other apps
            //But delete it only if mockGPS was enabled. If not, deleteGPS will crash.
            if (data.mockGPSEnabled) {
                data.play = false
                deleteGPS()
                Log.d(TAG, "Track Play Service has been stopped")
                trackPlayServiceIsRunning = false
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


    private fun initGPS() {
        Log.d(TAG, "TrackPlayService initGPS")
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mockLocation = Location(LocationManager.GPS_PROVIDER)

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
