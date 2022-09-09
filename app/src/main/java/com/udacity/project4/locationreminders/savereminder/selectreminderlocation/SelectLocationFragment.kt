package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.TargetApi
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
import androidx.core.content.ContextCompat
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
import java.util.*
import kotlin.collections.ArrayList


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private var locationPermissionGranted = false
    private val defaultZoom = 15f
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mMap: GoogleMap
    private var mapReady = false
    private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    private val TAG = "HuntMainActivity"
    private val LOCATION_PERMISSION_INDEX = 0
    private val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q


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

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { it ->
            mMap = it
            mapReady = true

        }
        initMap()

//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }

    private fun setMapLongClick(map: GoogleMap?) {
        map?.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)

            )
        }
    }

    private fun initMap() {

        Log.d(TAG, "initMap: entered condition ")
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (  !requestForegroundAndBackgroundLocationPermissions())
            {
                return@registerForActivityResult
            }

            if (isGranted) {
                // Do if the permission is granted
                Log.d(TAG, "initMap: granted  ")

                Toast.makeText(
                    this.requireContext(),
                    "Permission Granted",
                    Toast.LENGTH_SHORT
                ).show()
                locationPermissionGranted = true
                getDeviceLocation()
                setMapLongClick(mMap)
                setPoiClick(mMap)
            } else {
                // Do otherwise
                Log.d(TAG, "initMap:  not granted  ")

                Toast.makeText(
                    this.requireContext(),
                    "Permission Denied",
                    Toast.LENGTH_SHORT
                ).show()
                view?.findNavController()
                    ?.navigate(R.id.action_selectLocationFragment_to_saveReminderFragment)
            }
        }
        Log.d(TAG, "initMap: last area  statemnet  ")

        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)


    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
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

    override fun onMapReady(p0: GoogleMap?) {
        Log.d(TAG, "onMapReady: asasdd")

    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() : Boolean{
        var option = -1
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            return true
        }

        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE

            }

            else -> {
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE

            }

        }
        if (runningQOrLater) {
            Toast.makeText(
                this.requireContext(),
                "Please Allow Location for all time to enable geofencing",
                Toast.LENGTH_SHORT
            ).show()

        }
       else{
            Toast.makeText(
                this.requireContext(),
                "Please Allow Location Access",
                Toast.LENGTH_SHORT
            ).show()

        }



        ActivityCompat.requestPermissions(
            requireActivity(),
            permissionsArray,
            resultCode
        )
        return false
    }


}
