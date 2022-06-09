package com.example.myapplication

import android.app.Service
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
class RunFragment(val data:Data) : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var runFragmentView:View   //Need this in whole class because playPauseButtonColor is called from MainActivity
    private lateinit var playPauseButton: Button //Need this in whole class because playPauseButtonColor is called from MainActivity
    private lateinit var gpsPlot:GPSTrackPlot //Need this in whole class because newTrackPlot is called from MainActivity
    private lateinit var trackPlayServiceIntent:Intent //Need this in whole class because the service is called in onCreateView and stopped in onDestroyView

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
//        val qqx:ArrayList<Float> = ArrayList<Float>()
//        val qqy:ArrayList<Float> = ArrayList<Float>()

//        if (data.numOfPoints > 0){
//            for (i in 0 until data.numOfPoints ){
//                qqx.add(data.trackpoints[i].lon.toFloat())
//                qqy.add(data.trackpoints[i].lat.toFloat())
//            }
//        }
//        else{
//            qqx.add(0F)
//            qqx.add(0F)
//            qqy.add(0F)
//            qqy.add(0F)
//        }

        gpsPlot.setTrackData(data)
        gpsPlot.makeBitmap = true
        gpsPlot.setCirclePoint(data.currentPoint)
        gpsPlot.postInvalidate()
    }

    fun playPauseButtonColor(){
        val playPauseButton: Button = runFragmentView.findViewById<Button>(R.id.buttonPlayPause)
        if (data.play){
            playPauseButton.text = "Playing"
            playPauseButton.setBackgroundColor(ContextCompat.getColor(runFragmentView.context,R.color.myGreen))
        }
        else{
            playPauseButton.text = "Paused"
            playPauseButton.setBackgroundColor(ContextCompat.getColor(runFragmentView.context,R.color.myRed))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"RunFragment OnCreateView")
        super.onCreateView(inflater, container, savedInstanceState)

        // Inflate the layout for this fragment
        runFragmentView = inflater.inflate(R.layout.fragment_run, container, false)
        playPauseButton = runFragmentView.findViewById<Button>(R.id.buttonPlayPause)
        val seekBar: SeekBar = runFragmentView.findViewById<SeekBar>(R.id.seekBar)
        val tvPoint:TextView = runFragmentView.findViewById<TextView>(R.id.tvPoint)
        val tvAltitude:TextView = runFragmentView.findViewById<TextView>(R.id.tvAltitude)
        val tvSpeed:TextView = runFragmentView.findViewById<TextView>(R.id.tvSpeed)
        gpsPlot = runFragmentView.findViewById(R.id.cvGraph)
        lateinit var trackPlayService: TrackPlayService


        val serviceConnection: ServiceConnection = object: ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                Log.d(TAG,"onServiceConnected")
                trackPlayService = (p1 as TrackPlayService.TrackPlayServiceBinder).getService()
                trackPlayService.setData(data)
                trackPlayService.startPlayLoop()
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                Log.d(TAG,"onServiceDisconnected")
            }
        }

        trackPlayServiceIntent = Intent(runFragmentView.context,TrackPlayService::class.java)
        activity?.startService(trackPlayServiceIntent)
        activity?.bindService(trackPlayServiceIntent,serviceConnection, Context.BIND_AUTO_CREATE)


        playPauseButtonColor()      //We need to set the color to red on the first run


        Thread{
            while (true){
                activity?.runOnUiThread( Runnable {
                    getDataFromServiceAndUpdateDisplay(seekBar, tvPoint, tvAltitude, tvSpeed)
                }
                )
                Thread.sleep(100)
            }
        }.start()


        playPauseButton.setOnClickListener {
            if (data.numOfPoints>0) {
                data.play = !data.play
                playPauseButtonColor()
            }
        }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2){    //If the seekbar change was caused by screen input (instead of by code)
                    data.play = false     //put the player on pause
                    playPauseButtonColor()
                    tvPoint.text = p1.toString()
                    data.currentPoint = p1
                    if (data.numOfPoints > 0) {
                        tvAltitude.text =
                            data.trackpoints[p1].altitude.toFt().toString()
                        tvSpeed.text = data.trackpoints[p1].speed.toMph().toString()
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
        activity?.stopService(trackPlayServiceIntent)
        super.onDestroyView()
    }


    fun getDataFromServiceAndUpdateDisplay(seekBar: SeekBar, tvPoint:TextView, tvAltitude:TextView, tvSpeed:TextView){
            if (data.play) {
                seekBar.progress = data.currentPoint
                tvPoint.text = data.currentPoint.toString()
                tvAltitude.text = data.trackpoints[data.currentPoint].altitude.toFt().toString()
                tvSpeed.text = data.trackpoints[data.currentPoint].speed.toMph().toString()
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
            RunFragment(Data()).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}