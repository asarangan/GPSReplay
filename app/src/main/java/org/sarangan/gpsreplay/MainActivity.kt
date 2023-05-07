package org.sarangan.gpsreplay

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.R
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
//a lateinit var because other class functions need access to it (playPauseButtonColor and updateTrackPosition). The listener for the
//play/pause button click is set in onCreateView. This toggles the run boolean in the data class, and then sets the color of the button
//(via a function playPauseButtonColor). This color changing function needs access to the inflated view. The listener for the seekbar is
//also set in onCreateView. When the seekbar is changed by the user (instead of by code), the play/pause goes to pause (code.run is set to
//false) and the playPauseButtonColor is called to change its color to red. It also sets updates the track plot based on the dragged position
//of the seekbar. The button click will not do anything if the data class does not have valid data. The updateTrackPosition (via the seekbar
//listener) will also not do anything if the data class is empty.
//The track plot is created based on the data class during onStart (which comes after onCreateView). OnStart happens after onCreateView, and
//the seekbar listener calls updateTrackPosition (which needs the track plot). But this is not a problem because by the time the user interacts
//with the seekbar, onStart would have completed. The track plot needs to be created in onStart (instead of onCreate) because during onCreateView
//there would be no data in the data class. Similarly, the seekBar that was created during onCreateView would also contain no data. Therefore, in
//onStart, the track plot is created and the seekBar length has to be set to the actual data length. Also, the play/pause color has to be set.
//If a new data file is loaded while the old data file is running, that would result in the play color staying green.
//onCreateView of RunFragment also launches an instance of the TrackPlotPollThread class. This is a continuously running thread that will
//update the current position on the track plot every 100ms. The current position is incremented by the TrackPlayService. The TrackPlotPollThread
//needs access to the updateTrackPosition function. Therefore, inside the updateTrackPosition function, a new instance of RunFragment is created,
//to access the updateTrackPosition function. This doesn't run onCreate, onStart etc.. because it is just a regular kotlin class. This function
//also needs access to the main UI thread because the views cannot be modified by other threads. This is done by passing MainActivity().
//When the RunFragment closes with onDestroyView, the TrackPlotPollThread is interrupted (stopped).
//TrackPlayService:
//This service runs in the background to increment the track counter and perform the mockGPS operation. It is started from the button click listener
//in RunFragment as a foreground service. During its onStartCommand, it spawns a new thread that continuously loops by incrementing the stack
//pointer for the GPS track data, and passes it to the mockGPS function. This loop keeps listening to the data.play variable. When the user
//disables play (on the RunFragment button), the loop ends and the service stops with stopForeground. There is also another boolean that is
//set by TrackPlayService to indicate whether the service is running or stopped (trackPlayServiceIsRunning). This is checked by RunFragment
//before a new service is launched to prevent duplicate services from running.



const val TAG:String = "GPS"
var data: Data = Data()   //This a global variable (currently empty) that can be read by the fragments and the service
var trackPlayServiceIsRunning:Boolean = false

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"MainActivity OnCreate")
        setContentView(R.layout.activity_main)

        val fileFragment: FileFragment = FileFragment()  //File fragment will read the file and load the content into the global variable data
        val runFragment: RunFragment = RunFragment()     //Run fragment will move through the data file and perform the mock GPS function

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
                    setFragment(fileFragment,runFragment)
                }
            }
            true
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Main Activity onDestroy")
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "Main Activity onStop")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Main Activity onStart")
    }

    private fun setFragment(fragment1: Fragment, fragment2: Fragment){
        supportFragmentManager.beginTransaction().apply {
            hide(fragment1)
            show(fragment2)
            commit()
        }
    }
}