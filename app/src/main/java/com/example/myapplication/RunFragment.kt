package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RunFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RunFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var runFragmentView:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    fun newTrackPlot(circlePoint: Int){
        val gpsPlot:GPSTrackPlot = runFragmentView.findViewById(R.id.cvGraph)
        val qqx:ArrayList<Float> = ArrayList<Float>()
        val qqy:ArrayList<Float> = ArrayList<Float>()

        if (fileFragment.gpxDataCallBack.numOfPoints > 0){
            for (i in 0 until fileFragment.gpxDataCallBack.numOfPoints ){
                qqx.add(fileFragment.gpxDataCallBack.trackpoints[i].lon.toFloat())
                qqy.add(fileFragment.gpxDataCallBack.trackpoints[i].lat.toFloat())
            }
        }
        else{
            qqx.add(0F)
            qqx.add(0F)
            qqy.add(0F)
            qqy.add(0F)
        }
        val qqx1 = qqx.toFloatArray()
        val qqy1 = qqy.toFloatArray()
        gpsPlot.setTrackData(qqx1,qqy1,circlePoint)
        gpsPlot.postInvalidate()
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        runFragmentView = inflater.inflate(R.layout.fragment_run, container, false)
        val playPauseButton: Button = runFragmentView.findViewById<Button>(R.id.buttonPlayPause)
        val seekBar: SeekBar = runFragmentView.findViewById<SeekBar>(R.id.seekBar)
        val tvPoint: TextView = runFragmentView.findViewById<TextView>(R.id.tvPoint)
        val tvAltitude: TextView = runFragmentView.findViewById<TextView>(R.id.tvAltitude)
        val tvSpeed: TextView = runFragmentView.findViewById<TextView>(R.id.tvSpeed)

        playPauseButton.text = "Paused"
        playPauseButton.setBackgroundColor(red)
        newTrackPlot(0)

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2){    //If the seekbar change was caused by screen input (instead of by code)
                    play = false     //put the system on pause
                    tvPoint.text = p1.toString()
                    if (fileFragment.gpxDataCallBack.numOfPoints > 0) {
                        tvAltitude.text =
                            fileFragment.gpxDataCallBack.trackpoints[p1].altitude.toFt().toString()
                        tvSpeed.text = fileFragment.gpxDataCallBack.trackpoints[p1].speed.toMph().toString()
                    }
                    else{
                        tvAltitude.text = "-"
                        tvSpeed.text = "-"
                    }

                    newTrackPlot(p1)
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