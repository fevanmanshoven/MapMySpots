package com.example.getlocationdetails.Screens

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.getlocationdetails.R
import com.example.getlocationdetails.databinding.FragmentMapBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition



lateinit var  binding : FragmentMapBinding


class MapFragment : Fragment(), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null
    private var lastKnownLocation: Location? = null

    //Inflating and Returning the View with DataBindingUtil
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)

//        val mapFragment = binding. as SupportMapFragment?
//        mapFragment?.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(p0: GoogleMap) {
        this.map = map
    }



}