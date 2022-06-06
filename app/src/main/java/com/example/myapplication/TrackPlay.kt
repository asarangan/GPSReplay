package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import androidx.annotation.MainThread
import com.google.android.material.internal.ContextUtils.getActivity

class TrackPlay : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //return super.onStartCommand(intent, flags, startId)
        //val tvPoint: TextView = runFragment.runFragmentView.findViewById<TextView>(R.id.tvPoint)

        Thread (Runnable() {
            while (true) {
//                if ((play)&&(runFragment.currentPoint < fileFragment.gpxDataCallBack.numOfPoints)) {
//                    Log.d("TEST", (System.currentTimeMillis()/1000).toString())
//                    Thread.sleep(1000)
//                    runFragment.currentPoint++
//                    runFragment.updatePointIndex(runFragment.currentPoint)
//                }
            }
        }).start()

        return START_STICKY
    }
}