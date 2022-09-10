package com.udacity.project4.locationreminders.geofence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d(TAG, "onReceive:  ")
        if (intent != null) {
            if (intent.action == ACTION_GEOFENCE_EVENT) {
                val geofencingEvent = GeofencingEvent.fromIntent(intent)

                if (geofencingEvent.hasError()) {
                    val errorMessage = context?.let { errorMessage(it, geofencingEvent.errorCode) }

                    return
                }

                if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {


                    val fenceId = when {
                        geofencingEvent.triggeringGeofences.isNotEmpty() ->
                            geofencingEvent.triggeringGeofences[0].requestId
                        else -> {
                            Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                            return
                        }
                    }
                    // Check geofence against the constants listed in GeofenceUtil.kt to see if the
                    // user has entered any of the locations we track for geofences.
                    val foundIndex = GeofencingConstants.LANDMARK_DATA.indexOfFirst {
                        it.id == fenceId
                    }

                    // Unknown Geofences aren't helpful to us
                    /* if ( -1 == foundIndex ) {
                         Log.e(TAG, "Unknown Geofence: Abort Mission")
                         return
                     }*/

                    val notificationManager = context?.let {
                        ContextCompat.getSystemService(
                            it,
                            NotificationManager::class.java
                        )
                    } as NotificationManager

                    Log.d(TAG, "onReceive: before sending  ")
                    if (context != null) {
                        notificationManager.sendGeofenceEnteredNotification(
                            context, foundIndex
                        )
                    }
                }
            }
        }

    }
}