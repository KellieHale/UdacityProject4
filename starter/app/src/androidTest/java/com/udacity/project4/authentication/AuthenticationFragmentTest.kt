package com.udacity.project4.authentication

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.annotation.UiThreadTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.UiThreadTestRule
import com.udacity.project4.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class AuthenticationFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initialize() {
        stopKoin()

        val myModule = module {
            viewModel {
                LoginViewModel()
            }
        }

        startKoin {
            modules(listOf(myModule))
        }
    }

    @Test
    fun testShowWelcomeToast() {
        val scenario =
            launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        var decorView: View? = null
        scenario.onFragment { authenticationFragment ->
            decorView = authenticationFragment.activity!!.window!!.decorView
            Navigation.setViewNavController(authenticationFragment.view!!, navController)
            authenticationFragment.onActivityResult(1001, Activity.RESULT_OK, null)
        }
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText("Welcome to the Location Reminders App!")))
    }
}