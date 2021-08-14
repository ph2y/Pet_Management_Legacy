package com.sju18001.petmanagement.ui.community.followerFollowing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // no title bar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        // view binding
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}