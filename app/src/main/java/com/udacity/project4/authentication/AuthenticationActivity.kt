package com.udacity.project4.authentication

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity


class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding

    private val viewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        val view: View = binding.root

        setContentView(view)

//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        binding.getStartedButton.setOnClickListener { launchSignInFlow() }
//          TODO: If the user was authenticated, send him to RemindersActivity


//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }



    private fun launchSignInFlow() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            1001
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)
                Log.d(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {

                Log.d(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

  /*  private fun observeAuthenticationState() {


        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    binding.textView.text =
                        "Welcome" + FirebaseAuth.getInstance().currentUser?.displayName
                    Log.d(
                        TAG,
                        "observeAuthenticationState: ${FirebaseAuth.getInstance().currentUser?.displayName}"
                    )
                    binding.getStartedButton.text = "Sign Out"
                    binding.getStartedButton.setOnClickListener {
                        AuthUI.getInstance().signOut(this)
                    }

                }

                else -> {
                    binding.textView.text = "Welcome"

                    binding.getStartedButton.text = "Login"
                    binding.getStartedButton.setOnClickListener {
                        launchSignInFlow()

                    }

                }
            }
        })
    }*/
}
