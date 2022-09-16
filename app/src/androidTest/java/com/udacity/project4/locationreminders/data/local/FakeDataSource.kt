package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source
    var remindersList = mutableListOf<ReminderDTO>()
    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturn: Boolean) {
        this.shouldReturnError = shouldReturn
    }
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError)
        return Result.Error("Reminder List Error Function")
        else        return Result.Success(remindersList)

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
       if (reminder!=null)
       {
           remindersList.add(reminder)

       }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        if (shouldReturnError)
            return Result.Error("Cannot Found The Reminder With Id $id Function Error ")
        remindersList.forEach{
            if (it.id==id)
                return Result.Success(it)
        }
        return Result.Error("Cannot Found The Reminder With Id $id")

    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }


}