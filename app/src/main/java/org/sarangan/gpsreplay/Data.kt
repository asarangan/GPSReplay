package org.sarangan.gpsreplay

class Data {

        var play:Boolean = false
        var currentPoint:Int = 0
        var numOfPoints:Int = 0
        lateinit var trackPoints: ArrayList<TrackPoint>
        var timeOffset:Long = 0
}