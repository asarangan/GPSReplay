package org.sarangan.gpsreplay

import android.app.Activity
import android.view.View
import android.widget.TextView

class Data {

        var play:Boolean = false
        var currentPoint:Int = 0
        var numOfPoints:Int = 0
        lateinit var trackPoints: ArrayList<TrackPoint>
        var timeOffset:Long = 0
        var mockGPSEnabled:Boolean = true
        lateinit var runFragmentView: View
        lateinit var mainActivity:Activity
}