package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun saveReminder() {
        val reminder = getReminder()
        saveReminderViewModel.validateAndSaveReminder(reminder)
        //-- TODO Assert for reminder being saved
        //-- TODO Asset for navigationCommand having a value
//        assert(saveReminderViewModel.validateEnteredData(reminder))
    }

    @Test
    fun testClearingAllValues() {
        saveReminderViewModel.onClear()
        //-- Assert for all cleared objects
    }

    @Test
    fun testInvalidDataForReminder() {
        val emptyTitleReminder = ReminderDataItem(
            title = "",
            description = "",
            location = "Orlando, FL",
            latitude = 0.00000,
            longitude = 0.00000)
        //-- TODO: Assert that invalid title doesn't save

        val emptyLocationReminder = ReminderDataItem(
            title = "Test",
            description = "",
            location = "",
            latitude = 0.00000,
            longitude = 0.00000)

        //-- TODO: Assert that invalid location doesn't save
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
        val result = dataSource.getReminder(reminder.id)
        when (result) {
            is Result.Success<*> -> {
                val savedReminder = result.data as ReminderDTO
                assertEquals("", savedReminder.description)
            }
            is Result.Error -> {

            }
        }
    }
}