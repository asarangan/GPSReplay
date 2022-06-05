package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.ContextMenu
import android.view.View

class GPSTrackPlot : View {

    constructor(context: Context) : super(context) {}
    constructor(context: Context,attrs:AttributeSet) : super(context,attrs) {}
    constructor(context: Context,attrs:AttributeSet, defStyleAttr:Int): super(context,attrs,defStyleAttr) {}
    constructor(context: Context,attrs:AttributeSet, defStyleAttr:Int, defStyleRes:Int): super(context,attrs,defStyleAttr,defStyleRes){}


    private lateinit var mPaint: Paint
    private var mIsInit: Boolean = false
    private lateinit var mPath: Path
    private var originY:Float = 0F
    private var originX:Float = 0F
    private var xDataOffset:Float = 0F
    private var yDataOffset:Float = 0F
    private var xScale:Float = 0F
    private var yScale:Float = 0F
    private var mWidth:Float = 0F
    private var mHeight:Float  = 0F
    private var mXUnit:Float = 0F
    private var mYUnit:Float = 0F
    //private lateinit var mBlackPaint: Paint
    lateinit var xDataPoints:FloatArray
    lateinit var yDataPoints:FloatArray
    var circlePoint:Int = 0



    fun setTrackData(xDataPoints: FloatArray, yDataPoints: FloatArray, circlePoint:Int) {
        this.xDataPoints = xDataPoints
        this.yDataPoints = yDataPoints
        this.circlePoint = circlePoint
    }



    fun init() {
        mPaint = Paint()
        mPath = Path()
//        mWidth = width / (xDataPoints.maxOf { it } - xDataPoints.minOf { it })
//        mHeight = height / (yDataPoints.maxOf { it } - yDataPoints.minOf { it })
//        mXUnit = (mWidth / 12).toFloat() //for 10 plots on x axis, 2 kept for padding;
//        mYUnit = (mHeight / 12).toFloat()
        xDataOffset = xDataPoints.minOf { it }
        yDataOffset = yDataPoints.minOf { it }
        xScale = width *0.9F/ (xDataPoints.maxOf { it } - xDataPoints.minOf { it })
        yScale = height *0.9F/ (yDataPoints.maxOf { it } - yDataPoints.minOf { it })
        //mYUnit = height / (yDataPoints.maxOf { it } - yDataPoints.minOf { it })
        //originY = height.toFloat()  //This is the height in pixels. Since the axis is inverted, 0 is max
        //mBlackPaint = Paint()
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

//    private fun drawAxis(canvas: Canvas, paint: Paint) {
//        canvas.drawLine(mXUnit, mYUnit, mXUnit, (mHeight - 10).toFloat(), paint) //y-axis
//        canvas.drawLine(
//            10f, mHeight - mYUnit,
//            mWidth - mXUnit, mHeight - mYUnit, paint
//        ) //x-axis
//    }

    private fun drawGraphPlotLines(canvas: Canvas, path: Path, paint: Paint) {
        //mPath!!.reset()
        val myPixel:Pixel = toPixel(xDataPoints[0],yDataPoints[0])
        mPath!!.moveTo(myPixel.x,myPixel.y) //shift origin to graph's origin
        for (i in 0 until xDataPoints.size) {
            val myPixel:Pixel = toPixel(xDataPoints[i],yDataPoints[i])
            mPath!!.lineTo(myPixel.x, myPixel.y)
//            canvas.drawCircle(
//                originX + mXUnit, originY - mDataPoints.get(i) * mYUnit, 5f, paint
//            )
            //originX += mXUnit
        } //end for
        canvas.drawPath(mPath!!, paint)
    }

//    private fun drawGraphPaper(canvas: Canvas, blackPaint: Paint) {
//        var cx = mXUnit
//        var cy = mHeight - mYUnit
//        blackPaint.strokeWidth = 1f
//        for (i in 1..11) {
//            canvas.drawLine(cx, mYUnit, cx, cy, blackPaint)
//            cx += mXUnit
//        } //drawing points on x axis(vertical lines)
//        cx = mXUnit
//        for (i in 1..11) {
//            canvas.drawLine(cx, cy, mWidth - mXUnit, cy, blackPaint)
//            cy -= mYUnit
//        } //drawing points on y axis
//    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
            init()

//        mBlackPaint!!.color = Color.BLACK
//        mBlackPaint!!.style = Paint.Style.STROKE
//        mBlackPaint!!.strokeWidth = 10f
            mPaint!!.style = Paint.Style.STROKE
            mPaint!!.strokeWidth = 5f
            mPaint!!.color = Color.BLUE
            //drawAxis(canvas!!, mBlackPaint!!)
            drawGraphPlotLines(canvas!!, mPath!!, mPaint!!)
            //drawGraphPaper(canvas, mBlackPaint!!)
            //drawTextOnXaxis(canvas, mBlackPaint)
            //drawTextOnYaxis(canvas, mBlackPaint)
            val myPixel:Pixel = toPixel(xDataPoints[circlePoint],yDataPoints[circlePoint])
            mPaint!!.color = Color.RED
            mPaint.setStyle(Paint.Style.FILL)
            canvas!!.drawCircle(myPixel.x,myPixel.y,10F,mPaint)
        }
    }

