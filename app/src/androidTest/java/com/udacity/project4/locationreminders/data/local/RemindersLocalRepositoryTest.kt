package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun setup() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(
            remindersDatabase.reminderDao(), Dispatchers.Main
        )
    }

    @Test
    fun `gettingReminderTestFromDataSource`() = runBlocking {
        val testReminder = ReminderDTO(
            "Reminder Title ", "Reminder Desctiption ", "Reminder Location",
            69.96, 63.485
        )

        remindersLocalRepository.saveReminder(testReminder)

        val result = remindersLocalRepository.getReminder(testReminder.id)
        result as Result.Success
        assertThat(result.data.title, `is`("Reminder Title "))
        assertThat(result.data.latitude, `is`(69.96))
        assertThat(result.data.location, `is`("Reminder Location"))
        assertThat(result.data.description, `is`("Reminder Desctiption "))
        assertThat(result.data.longitude, `is`(63.485))

    }


    @After
    fun cleanUp() {
        remindersDatabase.close()
    }
}