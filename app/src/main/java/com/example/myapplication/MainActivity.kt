package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView



val TAG:String = "GPS"

class MainActivity : AppCompatActivity() {                  //Main Activity is a class, not a variable. It is instantiated by the system.

    private val fileFragment:FileFragment = FileFragment()  //Only the variables in the main section of the fragment would be instantiated. The gpxDataCallBack will be uninitialized. OnCreate would not run.
    private val runFragment:RunFragment = RunFragment()     //RunFragment has no variables in the main section, so nothing inside the fragment will be instantiated.


    override fun onCreate(savedInstanceState: Bundle?) {    //The onCreate of the main Activity runs only once. But it will run again when brought into focus from a minimized state.
        super.onCreate(savedInstanceState)
        Log.d(TAG,"MainActivity OnCreate")
        setContentView(R.layout.activity_main)              //This inflates the layout

        var data:Data = Data()


        supportFragmentManager.beginTransaction().apply {
            add(R.id.frameLayout, fileFragment)             //Adding the frames does not call onCreateView of the fragment. onCreateView will be called after the onCreate (of the Main Activity) exits.
            add(R.id.frameLayout, runFragment)
            hide(runFragment)
            commit()
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        //The listener runs in an infinite loop even after the onCreate has exited. We can only modify things. It won't call the onCreateView of the fragment again.
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.itemFile -> {
                    fileFragment.play = runFragment.play    //Store the play state in fileFragment in case a new file is read (which would make it false)
                    fileFragment.currentPoint = runFragment.currentPoint
                    setFragment(runFragment, fileFragment)
                }
                R.id.itemRun -> {
                    //number of points in the gpx file would become valid only after a file is read (which occurs through the onClickListener inside the fileFragment).
                    // So during the onCreate of the Main Activity this would still be zero. This value need to be passed to the runFragment after the file is read.
                    //It has to be passed only once, but I don't see a way to do this one time, so we will do it every time the runFragment is brought into view.
                    runFragment.numOfPoints = fileFragment.gpxDataCallBack.numOfPoints  //fileFragment would have already run onCreateView so gpxDataCallBack would be instantiated.
                    runFragment.play = fileFragment.play    //if a new file was read, then play would have been set to false.
                    runFragment.playPauseButtonColor()
                    runFragment.currentPoint = fileFragment.currentPoint
                    if (runFragment.numOfPoints > 0) {  //If points are zero, then most likely no file has been read. Calling trackpoints will crash because trackpoints would be uninitiated.
                        runFragment.trackpoints = fileFragment.gpxDataCallBack.trackpoints
                        findViewById<SeekBar>(R.id.seekBar).max = runFragment.numOfPoints - 1
                    }
                    else {
                        findViewById<SeekBar>(R.id.seekBar).max = 0
                        findViewById<TextView>(R.id.tvPoint).text = "-"
                        findViewById<TextView>(R.id.tvAltitude).text = "-"
                        findViewById<TextView>(R.id.tvSpeed).text = "-"
                    }
                        runFragment.newTrackPlot() //Just like with numOfPoints, the plot needs to be created only once after the file has been read, but here we are doing it every time
                        //runFragment is called. The current point along the track has to be updated, and this is being done by recreating the whole plot.
                    setFragment(fileFragment,runFragment)
                }
            }
            true
        }


//            startService(trackPlayServiceIntent)
//        Log.d("GPS", "Service Started")
    }


    override fun onDestroy() {
        Log.d(TAG, "Main Activity onDestroy")
        super.onDestroy()
//        stopService(trackPlayServiceIntent)


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