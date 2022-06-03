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

class GPSPlot : View {

    constructor(context: Context) : super(context) {}
    constructor(context: Context,attrs:AttributeSet) : super(context,attrs) {}
    constructor(context: Context,attrs:AttributeSet, defStyleAttr:Int): super(context,attrs,defStyleAttr) {}
    constructor(context: Context,attrs:AttributeSet, defStyleAttr:Int, defStyleRes:Int): super(context,attrs,defStyleAttr,defStyleRes){}


    private lateinit var mPaint: Paint
    private var mIsInit: Boolean = false
    private lateinit var mPath: Path
    private var mOriginY:Float = 0F
    private var mOriginX:Float = 0F
    private var mWidth:Float = 0F
    private var mHeight:Float  = 0F
    private var mXUnit:Float = 0F
    private var mYUnit:Float = 0F
    private lateinit var mBlackPaint: Paint
    lateinit var mDataPoints:Array<Float>



    fun setData(dataPoints: Array<Float>) {
        mDataPoints = dataPoints
    }


    fun init() {
        mPaint = Paint()
        mPath = Path()
        mWidth = width.toFloat()
        mHeight = height.toFloat()
        mXUnit = (mWidth / 12).toFloat() //for 10 plots on x axis, 2 kept for padding;
        mYUnit = (mHeight / 12).toFloat()
        mOriginX = mXUnit
        mOriginY = mHeight - mYUnit
        mBlackPaint = Paint()
        mIsInit = true
    }

    private fun drawAxis(canvas: Canvas, paint: Paint) {
        canvas.drawLine(mXUnit, mYUnit, mXUnit, (mHeight - 10).toFloat(), paint) //y-axis
        canvas.drawLine(
            10f, mHeight - mYUnit,
            mWidth - mXUnit, mHeight - mYUnit, paint
        ) //x-axis
    }

    private fun drawGraphPlotLines(canvas: Canvas, path: Path, paint: Paint) {
        var originX = mXUnit
        val originY = mHeight - mYUnit
        mPath!!.moveTo(originX, originY) //shift origin to graph's origin
        for (i in 0 until mDataPoints.size) {
            mPath!!.lineTo(originX + mXUnit, originY - mDataPoints.get(i) * mYUnit)
            canvas.drawCircle(
                originX + mXUnit, originY - mDataPoints.get(i) * mYUnit, 5f, paint
            )
            originX += mXUnit
        } //end for
        canvas.drawPath(mPath!!, paint)
    }

    private fun drawGraphPaper(canvas: Canvas, blackPaint: Paint) {
        var cx = mXUnit
        var cy = mHeight - mYUnit
        blackPaint.strokeWidth = 1f
        for (i in 1..11) {
            canvas.drawLine(cx, mYUnit, cx, cy, blackPaint)
            cx += mXUnit
        } //drawing points on x axis(vertical lines)
        cx = mXUnit
        for (i in 1..11) {
            canvas.drawLine(cx, cy, mWidth - mXUnit, cy, blackPaint)
            cy -= mYUnit
        } //drawing points on y axis
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!mIsInit) {
            init()
        }
        mBlackPaint!!.color = Color.BLACK
        mBlackPaint!!.style = Paint.Style.STROKE
        mBlackPaint!!.strokeWidth = 10f
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = 10f
        mPaint!!.color = Color.BLUE
        drawAxis(canvas!!, mBlackPaint!!)
        drawGraphPlotLines(canvas, mPath!!, mPaint!!)
        drawGraphPaper(canvas, mBlackPaint!!)
        //drawTextOnXaxis(canvas, mBlackPaint)
        //drawTextOnYaxis(canvas, mBlackPaint)
    }
}
