package org.sarangan.gpsreplay

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView

//This will launch a thread that polls every 100ms to get the current data from the Service and update the track plot view
// The updateTrackPosition has to be run on the UI thread. Otherwise it will crash
class TrackPlotPollThread() : Thread() {
    override fun run() {
        try {
            while (true) {
                data.mainActivity.runOnUiThread(Runnable {
                    RunFragment().updateTrackPosition()
                }
                )
                Thread.sleep(200)
            }
        } catch (e: InterruptedException) {
            Log.d(TAG, "trackPlotWatchThread was interrupted")
        }
    }
}

