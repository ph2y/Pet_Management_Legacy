package com.sju18001.petmanagement.ui.signIn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivitySignInBinding

    // variable for ViewModel
    private val signInViewModel: SignInViewModel by lazy{
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(SignInViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // hide action bar
        supportActionBar?.hide()

        // show sign in fragment
        if(supportFragmentManager.findFragmentById(R.id.sign_in_activity_fragment_container) == null) {
            val fragment = SignInFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.sign_in_activity_fragment_container, fragment)
                .commit()
        }
    }
}