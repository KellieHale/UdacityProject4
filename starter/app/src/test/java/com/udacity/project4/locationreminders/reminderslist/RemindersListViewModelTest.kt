package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.udacity.project4.locationreminders.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.livedata.getOrAwaitValue
import com.udacity.project4.locationreminders.savereminder.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var context: Application

    private fun getReminder(): ReminderDTO {
        return ReminderDTO(
            title = "Test Title",
            description = "Test Description",
            location = "Orlando, FL",
            latitude = 0.00000,
            longitude = 0.00000)
    }

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initialize() {
        stopKoin()

        context = ApplicationProvider.getApplicationContext()
        fakeDataSource = FakeDataSource(mutableListOf(getReminder()))
        remindersListViewModel = RemindersListViewModel(context, fakeDataSource)
    }

    @Test
    fun checkLoadingValues() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertTrue(remindersListViewModel.showLoading.value!!)
        mainCoroutineRule.resumeDispatcher()
        assertFalse(remindersListViewModel.showLoading.value!!)
        assertFalse(remindersListViewModel.showNoData.value!!)
    }

    @Test
    fun checkDataFromFakeDataSource() {
        remindersListViewModel.loadReminders()
        val remindersList = remindersListViewModel.remindersList.getOrAwaitValue()
        assertEquals(1, remindersList?.size)
        assertEquals("Test Title", remindersList?.first()?.title)
        assertEquals("Test Description", remindersList?.first()?.description)
        assertEquals("Orlando, FL", remindersList?.first()?.location)
        assertEquals(0.0, remindersList?.first()?.longitude)
        assertEquals(0.0, remindersList?.first()?.latitude)
    }

    @Test
    fun showErrorWhenLoadingReminders() {
        fakeDataSource.setShouldReturnError(true)
        remindersListViewModel.loadReminders()

        assertEquals("Fake error encountered...", remindersListViewModel.showSnackBar.value)
    }

    @Test
    fun testNoData() =  mainCoroutineRule.runBlockingTest{
        mainCoroutineRule.pauseDispatcher()
        fakeDataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()
        mainCoroutineRule.resumeDispatcher()
        assertTrue(remindersListViewModel.showNoData.value!!)
    }
}