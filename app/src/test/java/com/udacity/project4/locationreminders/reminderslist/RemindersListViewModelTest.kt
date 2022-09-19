package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.savereminder.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import android.os.Build

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNot.not
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var reminderViewModel: RemindersListViewModel
    private var remindersList = mutableListOf<ReminderDTO>()

    fun setup() {
        stopKoin()
        val testReminder = ReminderDTO(
            "test reminder 1 ", "Description 1  ", "Location 1  ",
            32.967892, 31.394
        )
        val testReminder2 = ReminderDTO(
            "test reminder 2 ", "Description 2  ", "Location  2 ",
            32.967892, 31.394
        )
        val testReminder3 = ReminderDTO(
            "test reminder 3 ", "Description 3 ", "Location  3 ",
            32.967892, 31.394
        )
        remindersList.add(testReminder)
        remindersList.add(testReminder2)
        remindersList.add(testReminder3)

        fakeDataSource = FakeDataSource(remindersList)
        reminderViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

    }
    fun setupFake() {
        stopKoin()
        fakeDataSource = FakeDataSource(null)
        fakeDataSource.setShouldReturnError(true)
        reminderViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

    }


    @Test
    fun `checkLoadingLiveData`() {
        setup()

        mainCoroutineRule.pauseDispatcher()
        reminderViewModel.loadReminders()
        assertThat(reminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
    }


    @Test
    fun `testingNoRemindersData`() {
        setupFake()
        reminderViewModel.loadReminders()
        assertThat(reminderViewModel.showSnackBar.getOrAwaitValue(), `is`("No Reminders Found "))
    }
    @After
    fun stopDown() {
        stopKoin()
    }

}