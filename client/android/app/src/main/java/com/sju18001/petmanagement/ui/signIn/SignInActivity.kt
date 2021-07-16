package com.sju18001.petmanagement.ui.signIn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //view binding
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