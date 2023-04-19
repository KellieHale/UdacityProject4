package com.udacity.project4.authentication

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import com.udacity.project4.R

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)


        val login = findViewById<Button>(R.id.login_button)
        login.setOnClickListener {
            onItemClicked(login)
        }

        val register = findViewById<Button>(R.id.register_button)
        register.setOnClickListener {
            onItemClicked(register)
        }
    }

//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    private fun onItemClicked(item: Button){
        when(item.id) {
            R.id.login_button ->{
                findNavController.navigate(R.id.action_authenticationActivity_to_loginFragment)}
            R.id.register_button -> {
                findNavController(.navigate(R.id.action_authenticationActivity_to_registrationFragment)
        }
    }


}
