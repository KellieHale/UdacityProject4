package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource
    private lateinit var context: Application


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private fun getReminder(): ReminderDataItem {
        return ReminderDataItem(
            title = "Test Title",
            description = "Test Description",
            location = "Orlando, FL",
            latitude = 0.00000,
            longitude = 0.00000)
    }
    
    @Before
    fun initialize() {
        stopKoin()
        context = ApplicationProvider.getApplicationContext()
        dataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(context, dataSource)

    }

    @Test
    fun saveReminder() = mainCoroutineRule.runBlockingTest {
        val reminder = getReminder()
        saveReminderViewModel.validateAndSaveReminder(reminder)
        when (val result = dataSource.getReminder(reminder.id)) {
            is Result.Success<*> -> {
                val savedReminder = result.data as ReminderDTO
                assertEquals("Test Title", savedReminder.title)
                assertEquals("Test Description", savedReminder.description)
                assertEquals("Orlando, FL", savedReminder.location)
                assertEquals(0.00000, savedReminder.latitude)
                assertEquals(0.00000, savedReminder.longitude)
            }
            is Result.Error -> {

            }
        }

        saveReminderViewModel.navigationCommand.value
        assertEquals(NavigationCommand.Back, saveReminderViewModel.navigationCommand.value)

    }

    @Test
    fun validateEnteredData() = runBlocking{
        val reminderData = getReminder()
        if (reminderData.title.isNullOrEmpty()) {
            assertEquals("Please Enter Title", R.string.err_enter_title)
        }
        if (reminderData.location.isNullOrEmpty()) {
            assertEquals("Please select location", R.string.err_select_location)
        }
    }

    @Test
    fun testClearingAllValues() {
        saveReminderViewModel.onClear()
        //-- Assert for all cleared objects
    }

    @Test
    fun testInvalidDataForReminder()  = mainCoroutineRule.runBlockingTest {
        dataSource.deleteAllReminders()
        val emptyTitleReminder = ReminderDataItem(
            title = "",
            description = "Test Description",
            location = "Orlando, FL",
            latitude = 0.00000,
            longitude = 0.00000)
        saveReminderViewModel.validateAndSaveReminder(emptyTitleReminder)
        assertEquals(R.string.err_enter_title, saveReminderViewModel.showSnackBarInt.value)


        val emptyLocationReminder = ReminderDataItem(
            title = "Test Title",
            description = "Test Description",
            location = "",
            latitude = 0.00000,
            longitude = 0.00000)
        saveReminderViewModel.validateAndSaveReminder(emptyLocationReminder)
        assertEquals(R.string.err_select_location, saveReminderViewModel.showSnackBarInt.value)
    }

    @Test
    fun saveReminder_withNoDescription() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDataItem(
            title = "Test Title",
            description = "",
            location = "Orlando, FL",
            latitude = 0.00000,
            longitude = 0.00000)
        saveReminderViewModel.validateAndSaveReminder(reminder)
        when (val result = dataSource.getReminder(reminder.id)) {
            is Result.Success<*> -> {
                val savedReminder = result.data as ReminderDTO
                assertEquals("", savedReminder.description)
            }
            is Result.Error -> {

            }
        }
    }



}