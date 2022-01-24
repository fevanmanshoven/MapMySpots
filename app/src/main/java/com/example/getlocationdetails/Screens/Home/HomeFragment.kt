package com.example.getlocationdetails.Screens.Home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.getlocationdetails.LocationModel
import com.example.getlocationdetails.R
import com.example.getlocationdetails.databinding.FragmentHomeBinding
import com.google.android.gms.location.*
import java.lang.Exception

class HomeFragment : Fragment() {

    private val DEFAULT_UPDATE_INTERVAL: Long = 30
    private val FAST_UPDATE_INTERVAL: Long = 5
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: Int = 1

    private lateinit var binding: FragmentHomeBinding

    private lateinit var tv_sensor: TextView
    private lateinit var tv_updates: TextView
    private lateinit var tv_waypointsCounter: TextView


    private lateinit var sw_locationsupdates: Switch
    private var updatesOn: Boolean = false
    private lateinit var sw_gps: Switch

    private lateinit var btn_newWaypoint: Button
    private lateinit var btn_showWaypoints: Button

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val currentLocation: LocationModel = LocationModel()
    private lateinit var savedLocations: List<LocationModel>

    private lateinit var appActivity: Activity
    private lateinit var appContext: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {

            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
            binding.locationModel = currentLocation

            tv_sensor = binding.tvSensor
            tv_updates = binding.tvUpdates
            tv_waypointsCounter = binding.tvLabelWaypointsCounter

            sw_locationsupdates = binding.swLocationsupdates
            sw_gps = binding.swGps

            btn_newWaypoint = binding.btnNewWaypoint
            btn_showWaypoints = binding.btnShowWaypoints

            //set prop of LocationRequest
            locationRequest = LocationRequest.create().apply {
                interval = 1000 * DEFAULT_UPDATE_INTERVAL
                fastestInterval = 1000 * FAST_UPDATE_INTERVAL
                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                maxWaitTime = 100
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        updateUIValues(locationResult.lastLocation)
                    }
                }
            }

            sw_gps.setOnClickListener(View.OnClickListener {
                if (sw_gps.isChecked) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    tv_sensor.setText("Using GPS")
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    tv_sensor.setText("Using Towers or wifi")
                }
            })

            sw_locationsupdates.setOnClickListener(View.OnClickListener {
                if (sw_locationsupdates.isChecked) {
                    startLocationUpdates()
                    updatesOn = true;
                } else {
                    stopLocationUpdates()
                    updatesOn = false;
                }
            })

            //The complete onClickListener with Navigation using createNavigateOnClickListener
            binding.btnShowWaypoints.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_mapFragment)
            )

            updateGPS()
            return binding.root
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appActivity = requireActivity()
        appContext = requireContext()

    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //User Provide Permission
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(appActivity) { task ->
                if (task.isSuccessful) {
                    tv_updates.setText("Location being tracked")
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                appActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun stopLocationUpdates() {
        tv_updates.setText("Location NOT being tracked")
        tv_sensor.setText("Location NOT being tracked")
        currentLocation.lon = "Location NOT being tracked"
        currentLocation.lat = "Location NOT being tracked"
        currentLocation.alt = "Location NOT being tracked"
        currentLocation.accuracy = "Location NOT being tracked"
        currentLocation.speed = "Location NOT being tracked"
        currentLocation.address = "Location NOT being tracked"
        binding.invalidateAll()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                updateGPS()
            } else {
                Toast.makeText(appContext, "Requires GPS", Toast.LENGTH_LONG).show()
                appActivity.finish()
            }
        }
    }

    //Get permissions
    //get current location
    //Update ui
    private fun updateGPS() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appActivity)

        if (ActivityCompat.checkSelfPermission(
                appActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //User Provide Permission
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(appActivity) { task ->
                if (task.isSuccessful) {
                    if (sw_locationsupdates.isChecked) {
                        startLocationUpdates()
                        updateUIValues(task.result)
                        updatesOn = true
                    } else {
                        stopLocationUpdates()
                        updatesOn = false
                    }
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                appActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun setlocationModel(location: Location) {
        currentLocation.lon = location.longitude.toBigDecimal().toPlainString()
        currentLocation.lat = location.latitude.toBigDecimal().toPlainString()
        currentLocation.accuracy = location.accuracy.toBigDecimal().toPlainString()
        if (location.hasAltitude()) {
            currentLocation.alt = location.altitude.toBigDecimal().toPlainString()
        } else {
            currentLocation.alt = "Not Available"
        }
        if (location.hasSpeed()) {
            currentLocation.speed = location.speed.toBigDecimal().toPlainString()
        } else {
            currentLocation.speed = "Not Available"
        }
        var geocoder = Geocoder(appActivity)
        try {
            var addresses: List<Address> =
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            currentLocation.address = addresses.get(0).getAddressLine(0)
        } catch (e: Exception) {
            currentLocation.address = "Not Available"

        }
        binding.invalidateAll()
    }

    private fun updateUIValues(location: Location) {
        setlocationModel(location)

    }
}