package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import java.lang.Exception

class FakeReminderDao:RemindersDao {

    private var shouldReturnError = false
    var testReminder = ReminderDTO(
        "Reminder Title ", "Reminder Desctiption ", "Reminder Location",
        69.96, 63.485
    )
    val testList = mutableListOf<ReminderDTO>(testReminder)


    fun setShouldReturnError(shouldReturn: Boolean) {
        this.shouldReturnError = shouldReturn
    }
    override suspend fun getReminders(): List<ReminderDTO> {

        if (shouldReturnError)
            throw (Exception("Getting All Reminders Error"))
        val testReminder = ReminderDTO(
            "Reminder Title ", "Reminder Desctiption ", "Reminder Location",
            69.96, 63.485
        )
        return testList

    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        if (shouldReturnError)
            throw (Exception("Getting All Reminders Error"))
        testList.forEach {
            if (it.id==reminderId){
                return it
            }
        }
        return null
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {

        testList.add(testReminder)

    }

    override suspend fun deleteAllReminders() {
        testList.clear()
    }


}