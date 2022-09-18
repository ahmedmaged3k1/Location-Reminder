package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.io.IOException
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private var locationPermissionGranted = false
    private var poiPlaces = mutableListOf<ReminderDTO>()
    private lateinit var saveLocationButton: AppCompatButton
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
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >
            android.os.Build.VERSION_CODES.Q

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        saveLocationButton = binding.root.findViewById(R.id.appCompatButton)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        //initMap()
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
            val reminder = ReminderDTO(
                R.string.dropped_pin.toString(),
                "",
                "",
                latLng.latitude,
                latLng.longitude,
                Math.random().toString()
            )
            poiPlaces.add(reminder)
            // _viewModel.reminderTitle.value=poi.name
            _viewModel.latitude.value = latLng.latitude
            _viewModel.longitude.value = latLng.longitude
            //_viewModel.selectedPOI.value=poi
            _viewModel.reminderSelectedLocationStr.value = "Dropped Pin"
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            val reminder = ReminderDTO(
                poi.name,
                "",
                "",
                poi.latLng.latitude,
                poi.latLng.longitude,
                poi.placeId
            )
            poiPlaces.add(reminder)
            // _viewModel.reminderTitle.value=poi.name
            _viewModel.latitude.value = poi.latLng.latitude
            _viewModel.longitude.value = poi.latLng.longitude
            _viewModel.selectedPOI.value = poi
            _viewModel.reminderSelectedLocationStr.value = poi.name




            poiMarker.showInfoWindow()
        }
    }


    private fun getDeviceLocation() {

        val fusedLocationProviderClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        try {
            if (locationPermissionGranted) {
                val location = fusedLocationProviderClient.lastLocation
                location!!.addOnCompleteListener {

                    if (it.isSuccessful) {

                        if (it.result == null) {
                            Toast.makeText(
                                this.requireContext(),
                                "Cannot Get Last Location",
                                Toast.LENGTH_SHORT
                            ).show()
                            /*       view?.findNavController()
                                       ?.navigate(R.id.action_selectLocationFragment_to_saveReminderFragment)*/
                            return@addOnCompleteListener
                        }
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


    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            mMap = p0
        }
        enableMyLocation()
        locationPermissionGranted = true
        enableMyLocation()
        setMapStyle(mMap)
        setMapLongClick(mMap)
        setPoiClick(mMap)
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
    private fun requestForegroundAndBackgroundLocationPermissions(): Boolean {
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

        } else {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        if (requestCode == 115  )
         {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                enableMyLocation()

            }

        } else {
            Snackbar.make(
                binding.root.findViewById(R.id.mapsLayout),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        }
    }


    private fun moveCamera(latLng: LatLng, zoom: Float, title: String) {

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        val options = MarkerOptions().position(latLng).title(title)
        mMap.addMarker(options)


    }


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            mMap.isMyLocationEnabled = true
            getDeviceLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                115
            )
        }
        mMap.moveCamera(CameraUpdateFactory.zoomIn())
    }


    private fun onLocationSelected() {
        saveLocationButton.setOnClickListener {

            view?.findNavController()
                ?.navigate(R.id.action_selectLocationFragment_to_saveReminderFragment)
        }
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

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_styler
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }


}
