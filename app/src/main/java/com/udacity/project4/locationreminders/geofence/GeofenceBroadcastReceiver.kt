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
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import kotlinx.coroutines.runBlocking

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

        Log.d(TAG, "onReceive: enterd in function  happend")

        if (context != null) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            val geofencingTransition = geofencingEvent.geofenceTransition

            if (geofencingTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofencingTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                // Retrieve data from intent


                var localDb = LocalDB.createRemindersDao(context)
                runBlocking {
                    var last = localDb.getReminders().last()
                    SaveReminderFragment
                        .showNotification(
                            context.applicationContext,
                            "The ${last.title} place is entered "
                        )
                }


                Log.d(TAG, "onReceive: enterd in local  happend")



                // remove geofence
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                SaveReminderFragment.removeGeofences(context, triggeringGeofences)
            }
        }
    }
}