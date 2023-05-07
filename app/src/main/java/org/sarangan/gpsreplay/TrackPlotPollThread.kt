package org.sarangan.gpsreplay

import android.util.Log
import android.widget.SeekBar
import android.widget.TextView

//This will launch a thread that polls every 100ms to get the current data from the Service and update the track plot view
// The updateTrackPosition has to be run on the UI thread. Otherwise it will crash
class TrackPlotPollThread(
    private val seekBar: SeekBar,
    private val tvPoint: TextView,
    private val tvTime: TextView,
    private val tvAltitude: TextView,
    private val tvSpeed: TextView,
    private val gpsTrackPlot: GPSTrackPlot,
    private val activity: MainActivity
) : Thread() {
    override fun run() {
        try {
            while (true) {
                activity.runOnUiThread(Runnable {
                    RunFragment().updateTrackPosition(
                        seekBar,
                        tvPoint,
                        tvTime,
                        tvAltitude,
                        tvSpeed,
                        gpsTrackPlot
                    )
                }
                )
                Thread.sleep(200)
            }
        } catch (e: InterruptedException) {
            Log.d(TAG, "trackPlotWatchThread was interrupted")
        }
    }
}

