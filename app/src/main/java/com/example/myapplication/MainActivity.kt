package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    val fileFragment:FileFragment = FileFragment()
    val runFragment:RunFragment = RunFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.frameLayout, fileFragment)
            add(R.id.frameLayout, runFragment)
            hide(runFragment)
            commit()
        }


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.itemFile -> {
                    setFragment(runFragment, fileFragment)
                }
                R.id.itemRun -> {
                    runFragment.numOfPoints = fileFragment.gpxDataCallBack.numOfPoints
                    if (runFragment.numOfPoints > 0) {
                        runFragment.trackpoints = fileFragment.gpxDataCallBack.trackpoints
                    }
                    setFragment(fileFragment,runFragment)
                }
            }
            true
        }


//        val trackPlayIntent: Intent = Intent(this,TrackPlay::class.java)
//        startService(trackPlayIntent)
}


    private fun setFragment(fragment1: Fragment, fragment2: Fragment){
        supportFragmentManager.beginTransaction().apply {
            hide(fragment1)
            show(fragment2)
            //replace(R.id.frameLayout,fragment)
            commit()
        }
    }
}