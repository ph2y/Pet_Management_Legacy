package com.sju18001.petmanagement.ui.signIn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sju18001.petmanagement.R

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

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