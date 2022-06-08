package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [FileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FileFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var play:Boolean = false
    var currentPoint:Int = 0
    lateinit var gpxDataCallBack:GPXDataCallBack


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"FileFragment OnCreate")
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"FileFragment OnCreateView")
        super.onCreateView(inflater, container, savedInstanceState)

        // Inflate the layout for this fragment
        val fileFragmentView:View = inflater.inflate(R.layout.fragment_file, container, false)

        //This is for reading the external file
        val getContent:ActivityResultContracts.GetContent = ActivityResultContracts.GetContent()
        gpxDataCallBack = GPXDataCallBack(fileFragmentView)
        val getContentActivity =  registerForActivityResult(getContent,gpxDataCallBack)

        val gpxReadFileButton:Button = fileFragmentView.findViewById<Button>(R.id.gpxButton)
        gpxReadFileButton.setOnClickListener {
            getContentActivity.launch("*/*")
            play = false
            currentPoint = 0
        }

        return fileFragmentView
    }

    override fun onDestroyView() {
        Log.d(TAG,"FileFragment OnDestroyView")
        super.onDestroyView()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}