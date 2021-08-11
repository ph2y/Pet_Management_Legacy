package com.sju18001.petmanagement.ui.community.createUpdatePost

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivityCreateUpdatePostBinding
import com.sju18001.petmanagement.databinding.ActivityMyPetBinding

class CreateUpdatePostActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivityCreateUpdatePostBinding

    // variable for ViewModel
    private val createUpdatePostViewModel: CreateUpdatePostViewModel by lazy{
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(CreateUpdatePostViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivityCreateUpdatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // hide action bar
        supportActionBar?.hide()

        // show fragment
        if(supportFragmentManager.findFragmentById(R.id.create_update_post_activity_fragment_container) == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.create_update_post_activity_fragment_container, CreateUpdatePostFragment())
                .commit()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }
}