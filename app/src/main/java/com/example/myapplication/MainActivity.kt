package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

const val TAG:String = "GPS"
var data: Data = Data()   //This a global variable (currently empty) that can be read by the fragments and the service

class MainActivity : AppCompatActivity() {

    //lateinit var trackPlayServiceIntent:Intent //Need this in whole class because the service is called in onCreateView and stopped in onDestroyView
    //lateinit var trackPlayService:TrackPlayService   //Need this in whole class because GPS LOCATION needs to be deleted on exit, and that is inside the service class.
    //lateinit var serviceConnection: ServiceConnection //Need this to delete the GPS LOCATION before exiting, which is in the onStop


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"MainActivity OnCreate")
        setContentView(R.layout.activity_main)              //This inflates the layout
//        var trackPlayService:TrackPlayService = TrackPlayService()

//        val serviceConnection: ServiceConnection = object: ServiceConnection {
//            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
//                Log.d(TAG,"onServiceConnected")
//                val trackPlayService:TrackPlayService = (p1 as TrackPlayService.TrackPlayServiceBinder).getService()
//            }
//
//            override fun onServiceDisconnected(p0: ComponentName?) {
//                Log.d(TAG,"onServiceDisconnected")
//            }
//        }

//        val trackPlayServiceIntent:Intent = Intent(this,TrackPlayService::class.java)
//        bindService(trackPlayServiceIntent,serviceConnection, Context.BIND_AUTO_CREATE)

        //while (trackPlayService == null){}


//        val data:Data = trackPlayService.dataPacket
        val fileFragment:FileFragment = FileFragment()  //File fragment will read the file and load the content into data
        val runFragment:RunFragment = RunFragment()     //Run fragment will move through the data file and perform the mock GPS function

        //Next we will display the appropriate fragment depending on the bottom navigation selection by the user
        val qq = supportFragmentManager.beginTransaction()
        qq.add(R.id.frameLayout, fileFragment)      //Adding the frames does not call onCreateView of the fragment. onCreateView of the fragment will be called after the onCreate (of the Main Activity) exits.
        qq.add(R.id.frameLayout, runFragment)
        qq.hide(runFragment)        //We apply both fragments (they will be on top of each other), and then hide one.
        qq.commit()


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        //The listener runs in an infinite loop even after the onCreate has exited. We can only modify things. It won't call the onCreateView of the fragment again.
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.itemFile -> {
                    setFragment(runFragment, fileFragment)
                }
                R.id.itemRun -> {
                    if (data.numOfPoints > 0) {  //If points are zero, then most likely no file has been read. Calling trackpoints will crash because trackpoints would be uninitiated.
                        updateRunFragmentDisplay(data,findViewById<SeekBar>(R.id.seekBar),findViewById<TextView>(R.id.tvPoint),findViewById<TextView>(R.id.tvAltitude),findViewById<TextView>(R.id.tvSpeed))
                        runFragment.playPauseButtonColor()
                    }
                    else {
                        updateRunFragmentDisplay(data,findViewById<SeekBar>(R.id.seekBar),findViewById<TextView>(R.id.tvPoint),findViewById<TextView>(R.id.tvAltitude),findViewById<TextView>(R.id.tvSpeed))
                        runFragment.playPauseButtonColor()
                    }
                        runFragment.newTrackPlot() //Just like with numOfPoints, the plot needs to be created only once after the file has been read, but here we are doing it every time
                        //runFragment is called. The current point along the track has to be updated, and this is being done by recreating the whole plot.
                    setFragment(fileFragment,runFragment)
                }
            }
            true
        }
    }

    fun updateRunFragmentDisplay(data:Data, seekBar:SeekBar, tvPoint:TextView, tvAltitude:TextView, tvSpeed:TextView){
//        if (data.numOfPoints > 0){
//            seekBar.max = data.numOfPoints-1
//            seekBar.progress = data.currentPoint
//            tvPoint.text = data.currentPoint.toString()
//            tvSpeed.text = data.trackpoints[data.currentPoint].speed.toMph().toString()
//            tvAltitude.text = data.trackpoints[data.currentPoint].altitude.toFt().toString()
//        }
//        else{
//            seekBar.max = 0
//            seekBar.progress = 0
//            tvPoint.text = "-"
//            tvSpeed.text = "-"
//            tvAltitude.text = "-"
//        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Main Activity onDestroy")
//        runFragment.trackPlayService.deleteGPS()
//        unbindService(runFragment.serviceConnection)
//        stopService(runFragment.trackPlayServiceIntent)
        super.onDestroy()
    }



    private fun setFragment(fragment1: Fragment, fragment2: Fragment){
        supportFragmentManager.beginTransaction().apply {
            hide(fragment1)
            show(fragment2)
            //replace(R.id.frameLayout,fragment)
            commit()
        }
    }
}