package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    //TODO: provide testing to the SaveReminderView and its live data objects

    fun initRepository() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(getApplicationContext(), fakeDataSource)

    }


    @Test
    fun `save reminder `() {
        initRepository()
        val testReminder = ReminderDataItem(
            "test reminder", "Description ", "Location ",
            32.967892, 31.394
        )
        saveReminderViewModel.saveReminder(testReminder)
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
    }

    @Test
    fun `save reminder without description `() {
        initRepository()
        val testReminder = ReminderDataItem(
            "test reminder", "", "Location ",
            32.967892, 31.394
        )
        saveReminderViewModel.validateAndSaveReminder(testReminder)
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), notNullValue())
    }

    @Test
    fun `testing show loading live data `() = runBlocking {

        initRepository()
        val testReminder = ReminderDataItem(
            "test reminder", "Description ", "Location ",
            32.967892, 31.394
        )
        mainCoroutineRule.pauseDispatcher()

        saveReminderViewModel.validateAndSaveReminder(testReminder)

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))


    }
}