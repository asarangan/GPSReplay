package org.sarangan.gpsreplay

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.lang.Math.PI
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class XmlPullParserHandler() {
    //Declare array of track points
    val trackPoints = ArrayList<TrackPoint>()

    //Return code to indicate whether conversion was successful
    var returnCode: Int = 0

    //This is the parser that processes the string that was read from the file
    fun parse(inputStream: InputStream?) {
        //Define the date format used by GPSLogger. Example: 2023-04-02T19:40:14Z. The X stands for time zone. SSS for fractional seconds
        val simpleDateFormats: ArrayList<SimpleDateFormat> = arrayListOf<SimpleDateFormat>(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
        )

        try {
            val factory = XmlPullParserFactory.newInstance()
            //Namespace allows the use of xmlns in the name tags
            factory.isNamespaceAware = true
            val parser: XmlPullParser = factory.newPullParser()
            //Attach the input stream to the parser. The second parameter is encoding, so it can be null
            parser.setInput(inputStream, null)
            //The reading process is initiated with next(). It returns the event type.
            //Event type is START_TAG, END_TAG, TEXT, etc.. The first event type is from the header. It will be updated during the loop
            var eventType: Int = parser.next()
            //Declare a variable to hold the text between the start and end tags
            lateinit var text: String
            //Declare an initial instance of Trackpoint. This will be used to save the header tags, but will be disposed when the trkpt tag is encountered
            var trackPoint: TrackPoint = TrackPoint()

            //This is the main loop. Keep reading until not end of the document is reached
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //Read the current tag name
                val tagName: String? = parser.name

                //If the tag is trkpt and it is the start tag, then read the lat & lon attribute of the trkpt tag. It will be like: <trkpt lat="39.58983167" lon="-84.22876667">
                if ((eventType == XmlPullParser.START_TAG) && (tagName.equals(
                        "trkpt",
                        ignoreCase = true
                    ))
                ) {
                    trackPoint =
                        TrackPoint()  //Create an new track point instance. Discard the old.
                    trackPoint.lat = parser.getAttributeValue(null, "lat").toDouble()
                    trackPoint.lon = parser.getAttributeValue(null, "lon").toDouble()
                }

                //If the event type is text (ie not the start tag, or end tag), then save this text for later processing
                //When the tag closes in the next while loop iteration, we will need this information.
                if (eventType == XmlPullParser.TEXT) {
                    text = parser.text
                }
                //If it is an end tag, then depending on which end tag it is, we do different things
                if (eventType == XmlPullParser.END_TAG) {
                    when {
                        //If it is the end of the trkpt tag, calculate true course from the last point in the stack, but only if the trackpoint stack if not empty.
                        //If the track point stack if empty (ie this is the first point), then set the true course to 0
                        tagName.equals("trkpt", ignoreCase = true) -> {
                            val trueCourse: Float = if (trackPoints.size > 0) {
                                trueCourse(trackPoints, trackPoint)
                            } else {
                                0.0F
                            }
                            //Save the true course into the track point instance
                            trackPoint.trueCourse = trueCourse
                            //Next, push the track point into the stack.
                            trackPoints.add(trackPoint)
                        }
                        //If tag name is ele, then that is the altitude.
                        tagName.equals("ele", ignoreCase = true) -> trackPoint.altitude =
                            text.toDouble()
                        //If tag name is speed, then that is the speed in m/s
                        tagName.equals("speed", ignoreCase = true) -> trackPoint.speed =
                            text.toFloat()
                        //If tag name is time, then that is the time in the format specified in simpledateformat
                        //The time tag is also in the header tags, but this will get saved in the initial TrackPoint instance and will be discarded when the new TrackPoint instance is created
                        //We have several different time formats, varying in the number of decimal points for seconds. We will check them all.
                        tagName.equals("time", ignoreCase = true) -> {
                            for (i in 0 until simpleDateFormats.size) {
                                try {
                                    trackPoint.epoch = simpleDateFormats[i].parse(text).time
                                }
                                catch (e:ParseException){
                                    //Log.d(TAG,"Error detecting ${simpleDateFormats[i]}")
                                }
                            }
                        }
                    }
                }
                //Read the next parsing event. The output of the next is the event type
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
            returnCode = 1

        } catch (e: IOException) {
            e.printStackTrace()
            returnCode = 2
        }
    }

    //The following function calculates the true course between the current point being read, and the last track point in the array.
    //This calculation is being done before loading the current track point into the array
    //The true course is returned from the function
    private fun trueCourse(trackPoints: ArrayList<TrackPoint>, trackPoint: TrackPoint): Float {
        //Longitude of the current track point
        val lon2: Double = trackPoint?.lon.toRad()
        //Longitude of the last track point in the array
        val lon1: Double = trackPoints[trackPoints.size - 1].lon.toRad()
        val lat2: Double = trackPoint?.lat.toRad()
        val lat1: Double = trackPoints[trackPoints.size - 1].lat.toRad()
        //val geoField:GeomagneticField = GeomagneticField(lat1.toFloat(),lon1.toFloat(),0.0F,System.currentTimeMillis())
        //val magVar = geoField.getDeclination()    //in degrees. -ve for W. -4.8 in Dayton. We are not using this
        return ((atan2(                          //This is the true course formula based on lat/lon from Ed Williams
            sin(lon2 - lon1) * cos(lat2),
            cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1)
        ) + 2.0 * PI) % (2.0 * PI)).toDeg().toFloat()
    }


}