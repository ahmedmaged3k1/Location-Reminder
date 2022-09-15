package com.udacity.project4.locationreminders.data.local

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest

class RemindersDaoTest {

    //    TODO: Add testing implementation to the RemindersDao.kt
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun initDb() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }



    @Test
    fun `gettingExistingReminderById`() = runBlockingTest {
        initDb()
        val testReminder = ReminderDTO(
            "Reminder Title ", "Reminder Desctiption ", "Reminder Location",
            69.96, 63.485
        )
        remindersDatabase.reminderDao().saveReminder(testReminder)
        val reminderDTO = remindersDatabase.reminderDao().getReminderById(testReminder.id)

        assertThat(reminderDTO as ReminderDTO, notNullValue())
        assertThat(reminderDTO.location, `is`(testReminder.location))
        assertThat(reminderDTO.description, `is`(testReminder.description))
        assertThat(reminderDTO.latitude, `is`(testReminder.latitude))
        assertThat(reminderDTO.id, `is`(testReminder.id))
        assertThat(reminderDTO.title, `is`(testReminder.title))
        assertThat(reminderDTO.longitude, `is`(testReminder.longitude))
    }

    @Test
    fun `gettingNotExistingReminderById`() = runBlockingTest {
        var testReminder = ReminderDTO(
            "Reminder Title ", "Reminder Desctiption ", "Reminder Location",
            69.96, 63.485
        )
        remindersDatabase.reminderDao().saveReminder(testReminder)
        assertNull(remindersDatabase.reminderDao().getReminderById("36"))

    }
    @After
    fun closeDatabase() = remindersDatabase.close()

}