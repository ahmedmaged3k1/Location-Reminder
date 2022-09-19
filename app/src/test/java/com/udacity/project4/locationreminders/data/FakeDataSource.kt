package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var remindersList: MutableList<ReminderDTO>?) : ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source

    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturn: Boolean) {
        this.shouldReturnError = shouldReturn
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> = withContext(Dispatchers.IO) {
        try {
            if (!shouldReturnError)
            {
                remindersList?.let { return@let Result.Success(it) }

            }
        }
        catch (ex : Exception){
            return@withContext  Result.Error("No Reminders Found ")
        }

        return@withContext Result.Error(
            "No Reminders Found "
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> =
        withContext(Dispatchers.IO) {
            try {
                if (!shouldReturnError)
                {
                    remindersList?.firstOrNull { it.id == id }?.let { return@let Result.Success(it) }


                }
            }
            catch (ex : Exception){
                return@withContext  Result.Error("Cannot Found The Reminder With Id $id")
            }

            return@withContext Result.Error("Cannot Found The Reminder With Id $id")


        }

    override suspend fun deleteAllReminders() {
        remindersList?.clear()

    }


}