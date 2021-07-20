package com.sju18001.petmanagement.ui.welcomePage

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.ActionBar
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivityWelcomePageBinding

class WelcomePageActivity : AppCompatActivity() {
    private var _binding: ActivityWelcomePageBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWelcomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()
    }
}