package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat

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
    private lateinit var runFragmentView:View
    lateinit var trackpoints: List<Trackpoint>
    var numOfPoints = 0
    var play:Boolean = false
    var currentPoint:Int = 0
    lateinit var gpsPlot:GPSTrackPlot
    lateinit var tvPoint: TextView
    lateinit var tvAltitude: TextView
    lateinit var tvSpeed: TextView
    lateinit var trackPlayService: TrackPlayService

    private val serviceConnection: ServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.d(TAG,"onServiceConnected")
            trackPlayService = (p1 as TrackPlayService.TrackPlayServiceBinder).getService()
            trackPlayService.startPlayLoop()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.d(TAG,"onServiceDisconnected")
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"RunFragment OnCreate")
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    fun newTrackPlot(){
        gpsPlot = runFragmentView.findViewById(R.id.cvGraph)
        val qqx:ArrayList<Float> = ArrayList<Float>()
        val qqy:ArrayList<Float> = ArrayList<Float>()

        if (numOfPoints > 0){
            for (i in 0 until numOfPoints ){
                qqx.add(trackpoints[i].lon.toFloat())
                qqy.add(trackpoints[i].lat.toFloat())
            }
            tvPoint.text = currentPoint.toString()
            tvAltitude.text = trackpoints[currentPoint].altitude.toFt().toString()
            tvSpeed.text = trackpoints[currentPoint].speed.toMph().toString()
        }
        else{
            qqx.add(0F)
            qqx.add(0F)
            qqy.add(0F)
            qqy.add(0F)
        }

        gpsPlot.setTrackData(qqx,qqy)
        gpsPlot.makeBitmap = true
        gpsPlot.setCirclePoint(currentPoint)
        gpsPlot.postInvalidate()
    }

    fun playPauseButtonColor(){
        //trackPlayService.play = play
        val playPauseButton: Button = runFragmentView.findViewById<Button>(R.id.buttonPlayPause)
        if (play){
            playPauseButton.text = "Playing"
            playPauseButton.setBackgroundColor(ContextCompat.getColor(runFragmentView.context,R.color.myGreen))
        }
        else{
            playPauseButton.text = "Paused"
            playPauseButton.setBackgroundColor(ContextCompat.getColor(runFragmentView.context,R.color.myRed))
        }
    }

    fun updatePointIndex(i:Int){
        val tvPoint: TextView = runFragmentView.findViewById<TextView>(R.id.tvPoint)
        tvPoint.text = i.toString()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"RunFragment OnCreateView")
        super.onCreateView(inflater, container, savedInstanceState)

        // Inflate the layout for this fragment
        runFragmentView = inflater.inflate(R.layout.fragment_run, container, false)
        val playPauseButton: Button = runFragmentView.findViewById<Button>(R.id.buttonPlayPause)
        val seekBar: SeekBar = runFragmentView.findViewById<SeekBar>(R.id.seekBar)
        tvPoint = runFragmentView.findViewById<TextView>(R.id.tvPoint)
        tvAltitude = runFragmentView.findViewById<TextView>(R.id.tvAltitude)
        tvSpeed = runFragmentView.findViewById<TextView>(R.id.tvSpeed)

        val trackPlayServiceIntent: Intent = Intent(runFragmentView.context,TrackPlayService::class.java)
        getActivity()?.bindService(trackPlayServiceIntent,serviceConnection, Context.BIND_AUTO_CREATE)

        playPauseButtonColor()
        //newTrackPlot(currentPoint)

        playPauseButton.setOnClickListener {
            if (numOfPoints>0) {
                play = !play
                trackPlayService.numOfPoints = numOfPoints
                trackPlayService.trackpoints = trackpoints
                trackPlayService.play = play
                trackPlayService.currentPoint = currentPoint
                playPauseButtonColor()
            }
        }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2){    //If the seekbar change was caused by screen input (instead of by code)
                    play = false     //put the player on pause
                    playPauseButtonColor()
                    tvPoint.text = p1.toString()
                    currentPoint = p1
                    if (numOfPoints > 0) {
                        tvAltitude.text =
                            trackpoints[p1].altitude.toFt().toString()
                        tvSpeed.text = trackpoints[p1].speed.toMph().toString()
                        trackPlayService.play = play
                        trackPlayService.currentPoint = currentPoint
                    }
                    else{
                        tvAltitude.text = "-"
                        tvSpeed.text = "-"
                    }
                    gpsPlot.setCirclePoint(p1)
                    gpsPlot.postInvalidate()
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

    override fun onDestroyView() {
        Log.d(TAG,"RunFragment OnDestroyView")
        super.onDestroyView()
        MainActivity().unbindService(serviceConnection)
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