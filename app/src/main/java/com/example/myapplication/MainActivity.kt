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

//Description of this program:
//The main screen contains two fragments - Run Fragment and File Fragment. These take up the whole screen. Each one is enabled and
//disabled depending on the bottom navigation bar selection.
//When the fragments are declared and attached to the layout (in MainActivity), the fragment life cycle is not initiated. But when the
//Both fragments go through onCreate, onCreateView, onViewCreated, OnStart, onResume only when the MainActivity's onCreate exits.
//In MainActivity, the RunFragment is hidden and only the FileFragment is shown. However, both would have gone through their whole
//initiation lifecycle. The bottom navigation listener (which is also on the MainActivity) will show/hide the appropriate fragment.
//This is done through the setFragment private function in MainActivity. The data class is also instantiated in MainActivity as a global
//variable so it can be accessed from all sub classes.
//
//FileFragment:
//In FileFragment the fragment is inflated in onCreateView, and the OpenGPX File button is attached to a file opening function, with a
// callback function. This callback function feeds the input file to the XML parser.
//XML Parser:
//The XML parser uses the parser factory to detect the various tags in the GPX file and load the data in the data class. It loads the GPS
//coordinates, altitude, speed, true course, timestamp etc.. into a track points array inside the data class. It looks for the trkpt, ele,
//speed, time tags. There are also some tags in the GPX header file that need to be ignored. The time must only contain integer seconds.
//Fractional seconds are not allowed. The XML parser also has a true course calculator, which is not in the GPX file. The return from the
//XML parser is the track point array.
//Callback function:
//The callback function reads the track point array from the XML parser and puts it into the global data class. The callback function also displays
//a toast message that X number of data points were read. It also displays information to the FileFragment screen (how many data points,
//start time, end time). The callback function seems to have access to the FileFragment's layout  (maybe because it was called from FileFragment).
//The callback function also computes the total distance and total duration and displays it in the FileFragment. If the parser did not result
//in a success (detected by a return code), the callback function will just display "-" for all the fields.
//RunFragment:
//RunFragment is where most of the work takes place. The display contains a plot area, seekbar, current position and a play/pause button.
//In onCreateView, the fragment is inflated (just like with FileFragment). However, the inflated view is declared outside onCreateView as
//a lateinit var because the other functions in the class need access to it. The listener for the play/pause button click is also set in onCreateView.
//This toggles the the run boolean in the data class, and sets the color of the button (via a function playPauseButtonColor). This function
//is outside onCreateView, so this is why the inflated view needs to be accessible in the whole class. The listener for the seekbar is also set
//in onCreateView. When the seekbar is changed by the user (instead of by code), the play/pause goes to pause (code.run is set to false) and
//the button color changes. It also sets the new current position based on what the user selected, and updates the position display on
//RunFragment. Both listeners (button and seekbar) will not do anything if the data class is empty. Seekbar can crash if we
//try to do things with an empty track points array.




const val TAG:String = "GPS"
var data: Data = Data()   //This a global variable (currently empty) that can be read by the fragments and the service

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"MainActivity OnCreate")
        setContentView(R.layout.activity_main)

        val trackPlayServiceIntent:Intent = Intent(this,TrackPlayService::class.java)

        val fileFragment:FileFragment = FileFragment()  //File fragment will read the file and load the content into the global variable data
        val runFragment:RunFragment = RunFragment()     //Run fragment will move through the data file and perform the mock GPS function

        //Next we will load both fragments, but only show the file fragment. This will trigger the onCreate of both fragments
        val qq = supportFragmentManager.beginTransaction()
        qq.add(R.id.frameLayout, fileFragment)      //Adding the frames does not call onCreateView of the fragment. onCreateView of the fragment will be called after the onCreate (of the Main Activity) exits.
        qq.add(R.id.frameLayout, runFragment)
        qq.hide(runFragment)        //We apply both fragments (they will be on top of each other), and then hide one.
        qq.commit()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.itemFile -> {  //If user selects the File fragment, hide the Run fragment and show the fileFragment
                    setFragment(runFragment, fileFragment)
                }
                R.id.itemRun -> {
//                    if (data.numOfPoints > 0) {  //Points must be >0 in order to plot anything
//                        updateRunFragmentDisplay(data,findViewById<SeekBar>(R.id.seekBar),findViewById<TextView>(R.id.tvPoint),findViewById<TextView>(R.id.tvAltitude),findViewById<TextView>(R.id.tvSpeed))
//                        runFragment.playPauseButtonColor()
//                        runFragment.newTrackPlot()
////runFragment is called. The current point along the track has to be updated, and this is being done by recreating the whole plot.
//                    }
//                    else {  //If points are zero, then most likely no file has been read. Trackpoints would be uninitiated.
//                        updateRunFragmentDisplay(data,findViewById<SeekBar>(R.id.seekBar),findViewById<TextView>(R.id.tvPoint),findViewById<TextView>(R.id.tvAltitude),findViewById<TextView>(R.id.tvSpeed))
//                        runFragment.playPauseButtonColor()
//                    }
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