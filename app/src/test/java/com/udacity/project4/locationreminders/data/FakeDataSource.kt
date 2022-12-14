package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var remindersList: MutableList<ReminderDTO>?) : ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source

    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturn: Boolean) {
        this.shouldReturnError = shouldReturn
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Exception error ")
        }
        remindersList?.let { return@let Result.Success(it.toList()) }
        return Result.Success(emptyList<ReminderDTO>())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList!!.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Exception error ")
        }
        remindersList?.firstOrNull { it.id == id }?.let { return Result.Success(it) }
        return Result.Error("Cannot Found The Reminder With Id $id")
    }

    override suspend fun deleteAllReminders() {
        remindersList?.clear()
    }



    /*
    override suspend fun getReminders(): Result<List<ReminderDTO>> = withContext(Dispatchers.IO) {
        try {
            if (shouldReturnError) {
                return@withContext Result.Error(
                    "No Reminders Found In DataSource "
                )
            } else {
                if (remindersList?.isEmpty() == true) {
                    return@withContext Result.Success(emptyList<ReminderDTO>())
                }
                remindersList?.let { return@let Result.Success(it) }


            }
        } catch (ex: Exception) {
            return@withContext Result.Error(ex.localizedMessage)
        }

        return@withContext Result.Error(
            "Error"
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> =
        withContext(Dispatchers.IO) {
            try {
                if (shouldReturnError) {

                    return@withContext Result.Error("Cannot Found The Reminder With Id $id")

                } else {
                    remindersList?.firstOrNull { it.id == id }
                        ?.let { return@let Result.Success(it) }

                }
            } catch (ex: Exception) {
                return@withContext Result.Error(ex.localizedMessage)
            }

            return@withContext Result.Error(
                "Error"
            )


        }

    override suspend fun deleteAllReminders() {
        remindersList?.clear()

    }
*/

}