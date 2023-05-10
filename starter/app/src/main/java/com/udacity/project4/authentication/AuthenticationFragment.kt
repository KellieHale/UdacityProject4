package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

class AuthenticationFragment : Fragment() {

    private lateinit var binding: ActivityAuthenticationBinding

    private val viewModel by viewModels<LoginViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityAuthenticationBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel



        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_authenticationFragment_to_reminderListFragment)
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.loginButton.setOnClickListener {
            signInFlow()
        }
    }

    companion object {
        const val TAG = "Authentication Fragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private fun signInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.map)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(), SIGN_IN_RESULT_CODE
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            if (authenticationState == LoginViewModel.AuthenticationState.AUTHENTICATED) {
                startActivity(Intent(context, RemindersActivity::class.java))
            }
        })

        if (requestCode == SIGN_IN_RESULT_CODE){
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.i(
                    TAG, "Successfully signed in user" +
                        "${FirebaseAuth.getInstance().currentUser?.displayName}!")
                findNavController().navigate(R.id.action_authenticationFragment_to_reminderListFragment)
                Snackbar.make(binding.root, "Welcome to the Location Reminders App!", Snackbar.LENGTH_SHORT).show()
            } else {
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.message}")
                Toast.makeText(context,"Please register for an Account", Toast.LENGTH_LONG).show()
            }
        }
    }
}