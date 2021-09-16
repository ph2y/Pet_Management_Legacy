package com.sju18001.petmanagement.ui.login

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivityLoginBinding

    // variable for ViewModel
    private val loginViewModel: LoginViewModel by lazy{
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(LoginViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // show login fragment
        if(supportFragmentManager.findFragmentById(R.id.login_activity_fragment_container) == null) {
            val fragment = LoginFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.login_activity_fragment_container, fragment)
                .commit()
        }
    }
}