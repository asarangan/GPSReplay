package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

val red: Int = Color.rgb(200, 0, 0)
val green: Int = Color.rgb(0, 200, 0)
var play:Boolean = false
val fileFragment:FileFragment = FileFragment()
val runFragment:RunFragment = RunFragment()
var qq:Array<Float> = arrayOf(5F, 2F, 7F, 9F, 3F, 5F, 9F, 1F, 6F, 8F)


class MainActivity : AppCompatActivity() {
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
                    findViewById<SeekBar>(R.id.seekBar).max = fileFragment.gpxDataCallBack.numOfPoints
                    if (fileFragment.gpxDataCallBack.code == 1) {
                        findViewById<TextView>(R.id.tvPoint).text = "0"
                    }
                    setFragment(fileFragment,runFragment)
                }
            }
            true
        }
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