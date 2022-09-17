package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var remindersList: MutableList<ReminderDTO>?) : ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        remindersList?.let { return Result.Success(it) }
        return Result.Error(
            "No Reminders Found "
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        remindersList?.forEach{
            if (it.id==id)
                return Result.Success(it)
        }
            return Result.Error("Cannot Found The Reminder With Id $id")



    }

    override suspend fun deleteAllReminders() {
        remindersList?.clear()

    }


}