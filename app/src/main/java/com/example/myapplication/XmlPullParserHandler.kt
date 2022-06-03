package com.example.myapplication

import android.hardware.GeomagneticField
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.lang.Math.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class XmlPullParserHandler() {
    val trackpoints = ArrayList<Trackpoint>()
    private var trackpoint: Trackpoint? = null
    private var text: String? = null
    private var tagname: String? = null
    private var simpleDateFormat: SimpleDateFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ssX",
        Locale.US
    )
    private var inside_trk: Boolean = false
    var code: Int = 0

    private fun trueCourse(trackpoints: ArrayList<Trackpoint>, trackpoint: Trackpoint?): Float {

        val lon2: Double = trackpoint!!.lon.toRad()
        val lon1: Double = trackpoints[trackpoints.size - 1].lon.toRad()
        val lat2: Double = trackpoint!!.lat.toRad()
        val lat1: Double = trackpoints[trackpoints.size - 1].lat.toRad()
        val geoField:GeomagneticField = GeomagneticField(lat1.toFloat(),lon1.toFloat(),0.0F,System.currentTimeMillis())
        val magVar = geoField.getDeclination()    //in degrees. -ve for W. -4.8 in Dayton
        val tc =  ((atan2(
            sin(lon2 - lon1) * cos(lat2),
            cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1)
        )+2.0*PI)%(2.0*PI)).toDeg().toFloat()
        return (tc)
    }


    fun parse(inputStream: InputStream?) {
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)
            var eventType = parser.eventType
            var tc:Float = 0.0F
            while (eventType != XmlPullParser.END_DOCUMENT) {
                tagname = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> if (tagname.equals("trkpt", ignoreCase = true)) {
                        inside_trk = true
                        trackpoint = Trackpoint()
                        trackpoint!!.lat = parser.getAttributeValue(null, "lat").toDouble()
                        trackpoint!!.lon = parser.getAttributeValue(null, "lon").toDouble()
                    }
                    XmlPullParser.TEXT -> text = parser.text
                    XmlPullParser.END_TAG -> when {
                        tagname.equals("trkpt", ignoreCase = true) -> {
                            tc = if (trackpoints.size > 0) {
                                trueCourse(trackpoints,trackpoint)
                            } else{
                                0.0F
                            }
                            trackpoint!!.bearing = tc
                            trackpoints.add(trackpoint!!)
                            inside_trk = false
                        }
                        tagname.equals("ele", ignoreCase = true) -> trackpoint!!.altitude =
                            text!!.toDouble()
                        tagname.equals("speed", ignoreCase = true) -> trackpoint!!.speed =
                            text!!.toFloat()
                        tagname.equals("time", ignoreCase = true) -> if (inside_trk) {
                            trackpoint!!.epoch = simpleDateFormat.parse(text).time
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
            code = 1

        } catch (e: IOException) {
            e.printStackTrace()
            code = 2
        }
    }
}