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

    val data:Data = Data()
    private val fileFragment:FileFragment = FileFragment(data)  //Only the variables in the main section of the fragment would be instantiated. The gpxDataCallBack will be uninitialized. OnCreate would not run.
    private val runFragment:RunFragment = RunFragment(data)     //RunFragment has no variables in the main section, so nothing inside the fragment will be instantiated.


    override fun onCreate(savedInstanceState: Bundle?) {    //The onCreate of the main Activity runs only once. But it will run again when brought into focus from a minimized state.
        super.onCreate(savedInstanceState)
        Log.d(TAG,"MainActivity OnCreate")
        setContentView(R.layout.activity_main)              //This inflates the layout


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
                    setFragment(runFragment, fileFragment)
                }
                R.id.itemRun -> {
                    if (data.numOfPoints > 0) {  //If points are zero, then most likely no file has been read. Calling trackpoints will crash because trackpoints would be uninitiated.
                        findViewById<SeekBar>(R.id.seekBar).max = data.numOfPoints - 1
                        findViewById<SeekBar>(R.id.seekBar).progress = data.currentPoint
                        runFragment.playPauseButtonColor()
                    }
                    else {
                        findViewById<SeekBar>(R.id.seekBar).max = 0
                        findViewById<TextView>(R.id.tvPoint).text = "-"
                        findViewById<TextView>(R.id.tvAltitude).text = "-"
                        findViewById<TextView>(R.id.tvSpeed).text = "-"
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