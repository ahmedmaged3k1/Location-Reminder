package com.udacity.project4.locationreminders.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification

import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject

import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope  , KoinComponent {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        //        TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            Log.d(TAG, "enqueueWork: enque entered ")
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }


    override fun onHandleWork(intent: Intent) {
        //TODO: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        //TODO call @sendNotification
        Log.d("TAG", "onHandleWork: handling ")
            val event: GeofencingEvent = GeofencingEvent.fromIntent(intent)

            val transition = event.geofenceTransition
            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                val geofences: List<Geofence> = event.triggeringGeofences
                sendNotification( geofences )
            }




    }
    fun showNotification( message: String) {
        val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
        Log.d(TAG, "showNotification:  entered ")
        var context=applicationContext

        var notificationId = 1589
        notificationId += Random(notificationId).nextInt(1, 30)
        val intent = Intent(this, ReminderDescriptionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationBuilder = NotificationCompat.Builder(context!!.applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_access_alarms_24)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.abc_vector_test,"Check The Details",pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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


    //TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {

        Log.d(TAG, "sendNotification: entered  ")

        for (reminder in triggeringGeofences){
            val requestId = reminder.requestId

            //Get the local repository instance
            // val remindersLocalRepository: RemindersLocalRepository by inject()
//        Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                //get the reminder with the request id
                val remindersLocalRepository: ReminderDataSource by inject()

                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }
        }


    }

}
