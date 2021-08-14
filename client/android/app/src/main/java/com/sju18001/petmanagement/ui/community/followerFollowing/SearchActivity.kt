package com.sju18001.petmanagement.ui.community.followerFollowing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivitySearchBinding
import java.util.regex.Pattern

class SearchActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivitySearchBinding

    // pattern regex for EditText
    private val patternUsername: Pattern = Pattern.compile("^[a-zA-Z0-9가-힣_]{2,20}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // no title bar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        // view binding
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // for search EditText listener
        binding.searchEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternUsername.matcher(s).matches()) {
                    Log.d("test", "regex True")
                }
                else {
                    Log.d("test", "regex False")
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for close button
        binding.closeButton.setOnClickListener { finish() }
    }
}