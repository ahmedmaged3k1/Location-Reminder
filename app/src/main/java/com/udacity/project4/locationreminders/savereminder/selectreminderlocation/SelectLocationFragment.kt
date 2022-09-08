package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.io.IOException


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private var locationPermissionGranted = false
    private val defaultZoom = 15f
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mMap: GoogleMap
    private var mapReady = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)


        //        TODO: add the map setup implementation
        initMap()
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }


    private fun initMap() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    // Do if the permission is granted
                    Toast.makeText(
                        this.requireContext(),
                        "Permission Granted",
                        Toast.LENGTH_SHORT
                    ).show()

                    val mapFragment =
                        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync { it ->
                        mMap = it
                        mapReady = true

                        locationPermissionGranted = true
                        getDeviceLocation()

                    }
                } else {
                    // Do otherwise
                    Toast.makeText(
                        this.requireContext(),
                        "Permission Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                    view?.findNavController()
                        ?.navigate(R.id.action_selectLocationFragment_to_saveReminderFragment)
                }
            }

            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

            true
        }
        R.id.terrain_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap?) {
        TODO("Not yet implemented")
    }

    private fun getDeviceLocation() {

        val fusedLocationProviderClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        try {
            if (locationPermissionGranted) {
                val location = fusedLocationProviderClient.lastLocation
                location!!.addOnCompleteListener {

                    if (it.isSuccessful) {

                        val currentLocation = it.result as Location
                        moveCamera(
                            LatLng(
                                currentLocation.latitude,
                                currentLocation.longitude
                            ),
                            defaultZoom,
                            "My Location",
                        )
                        Log.d(TAG, "getDeviceLocation: ${currentLocation.latitude}")
                        val geocoder = Geocoder(requireContext())
                        var addresses: List<Address?>? = ArrayList()
                        try {
                            addresses = geocoder.getFromLocation(
                                currentLocation.latitude,
                                currentLocation.longitude,
                                4
                            )
                        } catch (e: IOException) {

                        }
                    } else {

                    }

                }
            }
        } catch (e: SecurityException) {

        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float, title: String) {

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        val options = MarkerOptions().position(latLng).title(title)
        mMap.addMarker(options)


    }


}
