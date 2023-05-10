package com.udacity.project4

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.Application
import android.os.Build
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.firebase.ui.auth.AuthUI.getApplicationContext
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    KoinTest {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext() as Application
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            androidLogger()
            androidContext(appContext)
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @get: Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        POST_NOTIFICATIONS
    )
    @Test
    fun notificationPermission() {
        val activityScenario: ActivityScenario<RemindersActivity> = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getInstrumentation().uiAutomation.executeShellCommand(
                "pm grant " + getTargetContext().packageName + POST_NOTIFICATIONS
            )
        }
        activityScenario.close()
    }
    @Test
    fun isToastShowing() {
        val activity = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)
        onView(withId(R.id.action_authenticationFragment_to_reminderListFragment)).perform(click())
        onView(withText(R.string.welcome_to_the_location_reminder_app))
            .inRoot(withDecorView(not(`is`(activityRule(activity).window.decorView))))
            .check(matches(isDisplayed()))
    }

    private fun activityRule(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }
}
