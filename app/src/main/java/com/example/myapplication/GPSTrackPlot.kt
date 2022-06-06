package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View

class GPSTrackPlot : View {

    constructor(context: Context) : super(context) {}
    constructor(context: Context,attrs:AttributeSet) : super(context,attrs) {}
    constructor(context: Context,attrs:AttributeSet, defStyleAttr:Int): super(context,attrs,defStyleAttr) {}
    constructor(context: Context,attrs:AttributeSet, defStyleAttr:Int, defStyleRes:Int): super(context,attrs,defStyleAttr,defStyleRes){}

    private var trackPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5F
        color = Color.BLUE
    }
    private var circlePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = 5F
        color = Color.RED
    }
    private lateinit var trackPath: Path
    private var xDataOffset:Float = 0F
    private var yDataOffset:Float = 0F
    private var xScale:Float = 0F
    private var yScale:Float = 0F
    lateinit var xDataPoints:FloatArray
    lateinit var yDataPoints:FloatArray
    private var circlePoint:Int = 0
    private var circleRadius:Float = 0F

    fun setTrackData(xDataPoints: FloatArray, yDataPoints: FloatArray, circlePoint:Int) {
        this.xDataPoints = xDataPoints
        this.yDataPoints = yDataPoints
        this.circlePoint = circlePoint
    }

    fun init() {
        trackPath = Path()
        xDataOffset = xDataPoints.minOf { it }
        yDataOffset = yDataPoints.minOf { it }
        xScale = width *0.9F/ (xDataPoints.maxOf { it } - xDataPoints.minOf { it })
        yScale = height *0.9F/ (yDataPoints.maxOf { it } - yDataPoints.minOf { it })
        circleRadius = height/100F
    }

    class Pixel{
        var x:Float = 0F
        var y:Float = 0F
    }

    fun toPixel(x:Float,y:Float): Pixel {
        val myPixel:Pixel = Pixel()
        myPixel.x = (x-xDataOffset)*xScale + 0.05F*width
        myPixel.y = height-(y-yDataOffset)*yScale - 0.05F*height
        return myPixel
    }

    private fun drawGraphPlotLines(canvas: Canvas, path: Path, paint: Paint) {
        val myPixel:Pixel = toPixel(xDataPoints[0],yDataPoints[0])
        trackPath!!.moveTo(myPixel.x,myPixel.y) //shift origin to graph's origin
        for (i in 0 until xDataPoints.size) {
            val myPixel:Pixel = toPixel(xDataPoints[i],yDataPoints[i])
            trackPath.lineTo(myPixel.x, myPixel.y)
        }
        canvas.drawPath(trackPath, paint)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
            init()
            drawGraphPlotLines(canvas!!, trackPath, trackPaint)
            val myPixel:Pixel = toPixel(xDataPoints[circlePoint],yDataPoints[circlePoint])
            canvas!!.drawCircle(myPixel.x,myPixel.y,circleRadius,circlePaint)
        }
    }