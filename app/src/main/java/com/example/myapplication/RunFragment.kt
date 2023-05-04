package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RunFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RunFragment() : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var runFragmentView: View   //Need this in whole class because playPauseButtonColor is called from MainActivity
    private lateinit var playPauseButton: Button //Need this in whole class because playPauseButtonColor is called from MainActivity
    private lateinit var gpsPlot: GPSTrackPlot //Need this in whole class because newTrackPlot is called from MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "RunFragment OnCreate")
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    //During file open, both fragments will go into onStop. Then they resume with onStart. We need to change the length of seekbar after the file has been read
    override fun onStart() {
        Log.d(TAG, "RunFragment OnStart")




        val seekBar: SeekBar = runFragmentView.findViewById<SeekBar>(R.id.seekBar)
        seekBar.max = data.numOfPoints - 1
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "RunFragment OnCreateView")
        super.onCreateView(inflater, container, savedInstanceState)

        // Inflate the layout for this fragment
        runFragmentView = inflater.inflate(R.layout.fragment_run, container, false)

        playPauseButton = runFragmentView.findViewById<Button>(R.id.buttonPlayPause)
        val seekBar: SeekBar = runFragmentView.findViewById<SeekBar>(R.id.seekBar)
        val tvPoint: TextView = runFragmentView.findViewById<TextView>(R.id.tvPoint)
        val tvAltitude: TextView = runFragmentView.findViewById<TextView>(R.id.tvAltitude)
        val tvSpeed: TextView = runFragmentView.findViewById<TextView>(R.id.tvSpeed)
        gpsPlot = runFragmentView.findViewById(R.id.cvGraph)

        newTrackPlot()

        //Set the color of the play/pause button based on the variable inside data. At the beginning this will be red.
        playPauseButtonColor()

        //This will launch a thread that polls every 100ms to get the current data from the Service and update the track plot view
        Thread {
            while (true) {
                activity?.runOnUiThread(Runnable {
                    getDataFromServiceAndUpdateDisplay(seekBar, tvPoint, tvAltitude, tvSpeed)
                }
                )
                Thread.sleep(100)
                data.currentPoint++
            }
        }.start()

        //This is the listener for the play/pause button
        playPauseButton.setOnClickListener {
            if (data.numOfPoints > 0) {
                data.deltaTime =
                    System.currentTimeMillis() - Date(data.trackPoints[data.currentPoint].epoch).time
                //Toggle the play/pause button color
                data.play = !data.play
                //trackPlayService.setData(data)
                //Set the play/pause button color
                playPauseButtonColor()
            }
        }

        //This is the listener for the seekbar.
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                //p2 will be true if the seekbar change was caused by screen input. It would be false if the change was caused by code.
                if (p2) {
                    //First check if a file has been read. Otherwise don't do anything
                    if (data.numOfPoints > 0) {
                        //Put the player on pause if the user had interrupted the run
                        data.play = false
                        //Then change the color of the play/pause button
                        playPauseButtonColor()
                        //Move the current data position to the user selected point p1
                        data.currentPoint = p1
                        //Update the text fields
                        tvPoint.text = p1.toString()
                        tvAltitude.text =
                            data.trackPoints[p1].altitude.toFt().toString()
                        tvSpeed.text = data.trackPoints[p1].speed.toMph().toString()
//                    else {
//                        tvAltitude.text = "-"
//                        tvSpeed.text = "-"
//                    }
                        //Update the track plot
                        gpsPlot.setCirclePoint(p1)
                        gpsPlot.postInvalidate()
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                //TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                //TODO("Not yet implemented")
            }
        })

        return runFragmentView
    }


    fun newTrackPlot() {
        gpsPlot = runFragmentView.findViewById(R.id.cvGraph)
        gpsPlot.setTrackData(data)
        gpsPlot.makeBitmap = true
        gpsPlot.setCirclePoint(data.currentPoint)
        gpsPlot.postInvalidate()
    }

    fun playPauseButtonColor() {
        val playPauseButton: Button = runFragmentView.findViewById<Button>(R.id.buttonPlayPause)
        if (data.play) {
            playPauseButton.text = "Playing"
            playPauseButton.setBackgroundColor(
                ContextCompat.getColor(
                    runFragmentView.context,
                    R.color.myGreen
                )
            )
        } else {
            playPauseButton.text = "Paused"
            playPauseButton.setBackgroundColor(
                ContextCompat.getColor(
                    runFragmentView.context,
                    R.color.myRed
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "RunFragment OnViewCreated")
    }

    override fun onDestroyView() {
        Log.d(TAG, "RunFragment OnDestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "RunFragment OnDestroy")
    }

    override fun onStop() {
        Log.d(TAG, "RunFragment OnStop")
        super.onStop()
    }




    private fun getDataFromServiceAndUpdateDisplay(
        seekBar: SeekBar,
        tvPoint: TextView,
        tvAltitude: TextView,
        tvSpeed: TextView
    ) {
        if (data.play) {
            seekBar.progress = data.currentPoint
            tvPoint.text = data.currentPoint.toString()
            tvAltitude.text = data.trackPoints[data.currentPoint].altitude.toFt().toString()
            tvSpeed.text = data.trackPoints[data.currentPoint].speed.toMph().toString()
            newTrackPlot()
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RunFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RunFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}