package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setupFragmentTest() {
        fakeDataSource = FakeDataSource()

        remindersListViewModel =
            RemindersListViewModel(getApplicationContext(), fakeDataSource)

        stopKoin()
        val myModule = module {
            single {
                remindersListViewModel
            }
        }
        startKoin {
            modules(listOf(myModule))
        }
    }
    @Test
    fun displayRemindersDataUiTest() = runBlockingTest {
        setupFragmentTest()
        val testReminder = ReminderDTO(
            "Reminder Title ", "Reminder Desctiption ", "Reminder Location",
            69.96, 63.485
        )
        fakeDataSource.saveReminder(testReminder)
        val bundle = Bundle.EMPTY
        launchFragmentInContainer<ReminderListFragment>(bundle, R.style.AppTheme)
        onView(withId(R.id.noDataTextView)).check(matches(CoreMatchers.not(isDisplayed())))
        onView(ViewMatchers.withText(testReminder.title)).check(matches(isDisplayed()))
        onView(ViewMatchers.withText(testReminder.description)).check(matches(isDisplayed()))
        onView(ViewMatchers.withText(testReminder.location)).check(matches(isDisplayed()))

    }

    @Test
    fun navigateToAddReminderFragment() = runBlockingTest {
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }




    @After
    fun stopDown() {
        stopKoin()
    }
}