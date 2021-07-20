package com.sju18001.petmanagement.ui.welcomePage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.WelcomePageFragment
import com.sju18001.petmanagement.databinding.ActivityWelcomePageBinding

class WelcomePageActivity : AppCompatActivity() {
    private var _binding: ActivityWelcomePageBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWelcomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액션바 숨김
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