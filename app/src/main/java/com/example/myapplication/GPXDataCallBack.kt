package com.example.myapplication

import android.R.attr.data
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Math.pow
import java.security.AccessController.getContext
import java.util.*
import kotlin.math.*


class GPXDataCallBack (private val view: View): ActivityResultCallback<Uri> {

    var numOfPoints:Int = 0
    var code:Int = 0
    open lateinit var trackpoints: List<Trackpoint>


    override fun onActivityResult(result: Uri?) {
        val tvNumberOfPoints: TextView = view.findViewById<TextView>(R.id.tvNumberOfPoints)
        val tvStartTime: TextView = view.findViewById<TextView>(R.id.tvStartTime)
        val tvEndTime: TextView = view.findViewById<TextView>(R.id.tvEndTime)
        val tvDuration: TextView = view.findViewById<TextView>(R.id.tvDuration)
        val tvDistance: TextView = view.findViewById<TextView>(R.id.tvDistance)

        if (result != null) {
            val inputStream = view.context.contentResolver.openInputStream(result)
            try {
                val parser = XmlPullParserHandler()
                parser.parse(inputStream)
                trackpoints = parser.trackpoints
                code = parser.code

                when (code) {
                    0 -> {          //0 means the file was read successfully
                        numOfPoints = trackpoints.size
                        Toast.makeText(
                            view.context,
                            "Read $numOfPoints points",
                            Toast.LENGTH_SHORT
                        ).show()
                        val startDate: Date = Date(trackpoints!![0].epoch)
                        val endDate: Date = Date(trackpoints!![numOfPoints - 1].epoch)

                        //Write the number of points to the view
                        tvNumberOfPoints.text = numOfPoints.toString()

                        //Write the start date and time to the view
                        tvStartTime.text = startDate.toString()

                        //Write the end date and time to the view
                        tvEndTime.text = endDate.toString()

                        val millis: Long = endDate!!.time - startDate!!.time
                        val hours: Int = (millis / (1000 * 60 * 60)).toInt()
                        val mins: Int = (millis / (1000 * 60) % 60).toInt()
                        val secs: Int =
                            ((millis - (hours * 3600 + mins * 60) * 1000) / 1000).toInt()

                        //Write duration to the view
                        tvDuration.text = "$hours Hrs $mins Mins $secs secs"

                        //Calculate total distance
                        var distance:Double = 0.0
                        var trackpoint1: Trackpoint
                        var trackpoint2: Trackpoint
                        var dDistance: Double
                        var lat1:Double
                        var lat2:Double
                        var lon1:Double
                        var lon2:Double
                        for (i in 0..numOfPoints - 2) {
                            trackpoint1 = trackpoints[i]
                            trackpoint2 = trackpoints[i + 1]
                            lat1 = trackpoint1.lat.toRad()
                            lat2 = trackpoint2.lat.toRad()
                            lon1 = trackpoint1.lon.toRad()
                            lon2 = trackpoint2.lon.toRad()
                            dDistance = 2.0 * asin(
                                sqrt(
                                    sin((lat1 - lat2) / 2.0).pow(2.0) + cos(lat1) * cos(lat2) * sin(
                                        (lon1 - lon2) / 2.0
                                    ).pow(2.0)
                                )
                            ) * (180.0 * 60.0) * 1.15078/ PI
                            distance += dDistance
                        }
                        tvDistance.text = "${((distance*10.0).roundToInt()/10.0)} Statute Miles"

                    }
                    1 -> {  //1 means there was some error in the file
                        Toast.makeText(view.context, "Invalid File", Toast.LENGTH_SHORT).show()
                        numOfPoints = 0
                        tvNumberOfPoints.text = "-"
                        tvStartTime.text = "-"
                        tvEndTime.text = "-"
                        tvDuration.text = "-"
                        tvDuration.text = "-"
                        tvDistance.text = "-"
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}
