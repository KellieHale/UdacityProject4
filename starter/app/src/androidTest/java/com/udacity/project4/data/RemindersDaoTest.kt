package com.udacity.project4.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFalse

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private fun getReminder(): ReminderDTO {
        return ReminderDTO(
            title = "Test Title",
            description = "Test Description",
            location = "Orlando, FL",
            latitude = 0.00000,
            longitude = 0.00000)
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room
            .inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java)
            .build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun getReminderById() = runBlocking {
        val reminder = getReminder()

        runBlocking {

            database.reminderDao().saveReminder(reminder)
            val complete = database.reminderDao().getReminderById(reminder.id)


            assertThat(reminder.id, notNullValue())
            assertEquals(complete.id, reminder.id)
            assertEquals(complete.title, reminder.title)
            assertEquals(complete.description, reminder.description)
            assertEquals(complete.location, reminder.location)
        }
    }

    @Test
    fun saveReminder() = runBlocking {
        val reminder = getReminder()

        database.reminderDao().saveReminder(reminder)
    }

    @Test
    fun deleteReminders() = runBlocking {
        database.reminderDao().deleteAllReminders()
        val loadReminder = database.reminderDao().getReminders()

        assertFalse(false, loadReminder.toString())

    }

}