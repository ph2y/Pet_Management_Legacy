package com.sju18001.petmanagement.ui.welcomePage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivityWelcomePageBinding

class WelcomePageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        if(supportFragmentManager.findFragmentById(R.id.welcome_page_activity_fragment_container) == null){
            val fragment = WelcomePageFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.welcome_page_activity_fragment_container, fragment)
                .commit()
        }
    }
}