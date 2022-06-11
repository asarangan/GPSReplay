package com.example.myapplication

class Data {

        var play:Boolean = false
        var currentPoint:Int = 0
        var numOfPoints:Int = 0
        lateinit var trackpoints: List<Trackpoint>
        var deltaTime:Long = 0


        fun clone():Data{
                val newdata:Data = Data()
                newdata.play = play
                newdata.currentPoint = currentPoint
                newdata.numOfPoints = numOfPoints
                newdata.trackpoints = trackpoints.toList()
                newdata.deltaTime = deltaTime
                return newdata
        }
}