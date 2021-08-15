package com.sju18001.petmanagement.ui.community.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivityCommunityCommentBinding

class CommunityCommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommunityCommentBinding

    // variable for ViewModel
    private val communityCommentViewModel: CommunityCommentViewModel by lazy{
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(CommunityCommentViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCommunityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // 프래그먼트 실행
        if(supportFragmentManager.findFragmentById(R.id.community_activity_fragment_container) == null){
            supportFragmentManager
                .beginTransaction()
                .add(R.id.community_activity_fragment_container, CommunityCommentFragment())
                .commit()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }
}