package com.udacity.project4.locationreminders.savereminder


import android.Manifest
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Context.JOB_SCHEDULER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.ReminderJobService
import com.udacity.project4.locationreminders.geofence.createChannel
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import kotlin.random.Random

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var geofencingClient: GeofencingClient
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q
    private lateinit var binding: FragmentSaveReminderBinding
    private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

    private val TAG = "HuntMainActivity"
    private val LOCATION_PERMISSION_INDEX = 0
    private val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        createChannel(requireContext())
        binding.viewModel = _viewModel

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value


//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
            if (!requestForegroundAndBackgroundLocationPermissions()) {
                return@setOnClickListener
            }
            if (longitude != null) {
                if (latitude != null) {
                    createGeoFence(
                        LatLng(latitude.toDouble(), longitude.toDouble()),
                        geofencingClient
                    )
                }
            }
            scheduleJob()
            /* if (_viewModel.geofenceIsActive()) return@setOnClickListener
             val currentGeofenceIndex = _viewModel.nextGeofenceIndex()
             if(currentGeofenceIndex >= GeofencingConstants.NUM_LANDMARKS) {
                // removeGeofences()
                 _viewModel.geofenceActivated()
                 return@setOnClickListener
             }
             Log.d(TAG, "onViewCreated: $latitude and $longitude")
             val currentGeofenceData = GeofencingConstants.LANDMARK_DATA[currentGeofenceIndex]
             val geofence = Geofence.Builder()
                 .setRequestId(_viewModel.selectedPOI.value?.placeId)
                 .setCircularRegion(
                     latitude!!,
                     longitude!!,
                     GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
                 )
                 .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                 .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                 .build()
             val geofencingRequest = GeofencingRequest.Builder()
                 .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                 .addGeofence(geofence)
                 .build()
             geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                 addOnCompleteListener {
                     if (context?.let { it1 ->
                             ActivityCompat.checkSelfPermission(
                                 it1,
                                 Manifest.permission.ACCESS_FINE_LOCATION
                             )
                         } != PackageManager.PERMISSION_GRANTED
                     ) {
                         // TODO: Consider calling
                         //    ActivityCompat#requestPermissions
                         // here to request the missing permissions, and then overriding
                         //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                         //                                          int[] grantResults)
                         // to handle the case where the user grants the permission. See the documentation
                         // for ActivityCompat#requestPermissions for more details.
                         return@addOnCompleteListener
                     }
                     geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                         addOnSuccessListener {
                             Toast.makeText(
                                 context, "Geofence Added",
                                 Toast.LENGTH_SHORT
                             )
                                 .show()


                         }
                         addOnFailureListener {
                             Toast.makeText(
                                 context, "Geofence Not Addedd",
                                 Toast.LENGTH_SHORT
                             ).show()
                             if ((it.message != null)) {

                             }
                         }
                     }
                 }
             }*/
//             2) save the reminder to the local db
            val reminder = _viewModel.selectedPOI.value?.let { it1 ->
                ReminderDataItem(
                    title.toString(), description.toString(), location, latitude, longitude,
                    it1.placeId
                )
            }

            val reminder1 = ReminderDataItem(
                _viewModel.reminderTitle.value,
                _viewModel.reminderDescription.value,
                _viewModel.reminderSelectedLocationStr.value,
                latitude,
                longitude
            )


            if (_viewModel.validateEnteredData(reminder1))
            {
                _viewModel.saveReminder(reminder1)
                view?.findNavController()
                    ?.navigate(R.id.action_saveReminderFragment_to_reminderListFragment)
            }



        }
    }

    private fun createGeoFence(location: LatLng, geofencingClient: GeofencingClient) {
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS.toFloat())
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)

            .putExtra("message", "Geofence alert - ${location.latitude}, ${location.longitude}")

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    GEOFENCE_LOCATION_REQUEST_CODE
                )
            } else {
                geofencingClient.addGeofences(geofenceRequest, pendingIntent)
            }
        } else {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {

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
        } else {

            // checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    companion object {
        fun removeGeofences(context: Context, triggeringGeofenceList: MutableList<Geofence>) {
            val geofenceIdList = mutableListOf<String>()
            for (entry in triggeringGeofenceList) {
                geofenceIdList.add(entry.requestId)
            }
            LocationServices.getGeofencingClient(context).removeGeofences(geofenceIdList)
        }

        fun showNotification(context: Context?, message: String) {
            val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
            var notificationId = 1589
            notificationId += Random(notificationId).nextInt(1, 30)

            val notificationBuilder =
                NotificationCompat.Builder(context!!.applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_access_alarms_24)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(message)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(message)
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.app_name)
                }
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scheduleJob() {

        val componentName =
            ComponentName(requireActivity().applicationContext, ReminderJobService::class.java)
        val info = JobInfo.Builder(321, componentName)
            .setRequiresCharging(false)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setPeriodic(15 * 60 * 1000)
            .build()

        val scheduler = activity?.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled")
        } else {
            Log.d(TAG, "Job scheduling failed")
            scheduleJob()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun cancelJob() {
        val scheduler = activity?.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler

        scheduler.cancel(321)
        Log.d(TAG, "Job cancelled")
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
            resultCode as Int
        )
        return false
    }


}


const val GEOFENCE_RADIUS = 200
const val GEOFENCE_ID = "REMINDER_GEOFENCE_ID"
const val GEOFENCE_EXPIRATION = 10 * 24 * 60 * 60 * 1000 // 10 days
const val GEOFENCE_DWELL_DELAY = 10 * 1000 // 10 secs // 2 minutes
const val GEOFENCE_LOCATION_REQUEST_CODE = 12345
const val CAMERA_ZOOM_LEVEL = 13f
const val LOCATION_REQUEST_CODE = 123

