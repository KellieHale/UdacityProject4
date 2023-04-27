package com.udacity.project4.locationreminders.data.local

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.udacity.project4.locationreminders.data.dto.ReminderDTO


/**
 * Singleton class that is used to create a reminder db
 */
object LocalDB {

    /**
     * static method that creates a reminder class and returns the DAO of the reminder
     */
    fun createRemindersDao(context: Context): RemindersDao {
        return Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "locationReminders.db"
        ).build().reminderDao()
    }

}