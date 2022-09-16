package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source
    var remindersList = mutableListOf<ReminderDTO>()
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        try {


            return Result.Success(remindersList)

        }
        catch (ex: Exception) {
            return Result.Error(ex.localizedMessage)

        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {

        try {

            if (reminder!=null)
            {
                remindersList.add(reminder)

            }

        }
        catch (ex: Exception) {
             Result.Error(ex.localizedMessage)

        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        try {
            remindersList.forEach{
                if (it.id==id)
                    return Result.Success(it)
            }
            return Result.Error("Cannot Found The Reminder With Id $id")

        }
        catch (ex: Exception) {
            return Result.Error(ex.localizedMessage)

        }

    }

    override suspend fun deleteAllReminders() {
        try {

            remindersList.clear()


        }
        catch (ex: Exception) {
             Result.Error(ex.localizedMessage)

        }
    }


}