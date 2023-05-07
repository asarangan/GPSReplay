package org.sarangan.gpsreplay

import android.net.Uri
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import com.example.myapplication.R
import java.io.IOException
import java.util.Date
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


class GPXDataCallBack(private val view: View) : ActivityResultCallback<Uri> {


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
                data.trackPoints = parser.trackPoints
                data.play = false
                data.currentPoint = 0

                when (parser.returnCode) {
                    0 -> {          //0 means the file was read successfully
                        data.numOfPoints = data.trackPoints.size
                        Toast.makeText( //Show a toast message on how many tags were read
                            view.context,
                            "Read ${data.numOfPoints} points",
                            Toast.LENGTH_LONG
                        ).show()
                        val startDate: Date = Date(data.trackPoints[0].epoch)
                        val endDate: Date = Date(data.trackPoints[data.numOfPoints - 1].epoch)

                        //Write the number of points to the view
                        tvNumberOfPoints.text = data.numOfPoints.toString()

                        //Write the start date and time to the view
                        tvStartTime.text = startDate.toString()

                        //Write the end date and time to the view
                        tvEndTime.text = endDate.toString()

                        //The default epoch time is in milliseconds
                        val millis: Long = endDate.time - startDate.time
                        val hours: Int = (millis / (1000 * 60 * 60)).toInt()
                        val mins: Int = (millis / (1000 * 60) % 60).toInt()
                        val secs: Int =
                            ((millis - (hours * 3600 + mins * 60) * 1000) / 1000).toInt()

                        //Write duration to the view
                        tvDuration.text = "$hours Hrs $mins Mins $secs secs"

                        //Calculate total distance
                        var distance: Double = 0.0
                        var trackPoint1: TrackPoint
                        var trackPoint2: TrackPoint
                        //incremental distance
                        var dDistance: Double
                        var lat1: Double
                        var lat2: Double
                        var lon1: Double
                        var lon2: Double
                        for (i in 0..data.numOfPoints - 2) {
                            trackPoint1 = data.trackPoints[i]
                            trackPoint2 = data.trackPoints[i + 1]
                            lat1 = trackPoint1.lat.toRad()
                            lat2 = trackPoint2.lat.toRad()
                            lon1 = trackPoint1.lon.toRad()
                            lon2 = trackPoint2.lon.toRad()
                            dDistance = 2.0 * asin(
                                sqrt(
                                    sin((lat1 - lat2) / 2.0).pow(2.0) + cos(lat1) * cos(lat2) * sin(
                                        (lon1 - lon2) / 2.0
                                    ).pow(2.0)
                                )
                            ) * (180.0 * 60.0) * 1.15078 / PI
                            distance += dDistance
                        }
                        tvDistance.text = "${((distance * 10.0).roundToInt() / 10.0)} Statute Miles"
                    }

                    1 -> {  //1 means there was some error in the file
                        Toast.makeText(view.context, "Invalid File", Toast.LENGTH_SHORT).show()
                        data.numOfPoints = 0
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
