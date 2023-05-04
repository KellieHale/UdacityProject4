package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource
    private lateinit var context: Application

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
        assert(saveReminderViewModel.validateEnteredData(reminder))
    }

    @Test
    fun clickOnReminderLocation() {

    }

    @Test
    fun saveReminder_withNoDescription() {
        val reminder = ReminderDataItem(
            title = "Test Title",
            description = "",
            location = "Orlando, FL",
            latitude = 0.00000,
            longitude = 0.00000)
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assert(saveReminderViewModel.validateEnteredData(reminder))
    }
}