package com.udacity.project4.locationreminders

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> = withContext(Dispatchers.IO) {
        if (shouldReturnError) {
             Result.Error("Fake error encountered...")
        } else {
            reminders?.let {
                Result.Success(ArrayList(it))
            }
            Result.Error("No reminders found")
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (shouldReturnError) {
            Result.Error("Fake error encountered...")
        } else {
            val reminder = reminders?.first { it.id == id }
            if (reminder != null)
                Result.Success(reminder)
            else
                Result.Error("Reminder with id of $id not found")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}