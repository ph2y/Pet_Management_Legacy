package com.sju18001.petmanagement.ui.community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivityCommunityBinding

class CommunityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommunityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // 프래그먼트 분류
        val fragmentType = intent.getStringExtra("fragmentType")
        if(supportFragmentManager.findFragmentById(R.id.community_activity_fragment_container) == null){
            val fragment = when(fragmentType){
                "comment_fragment" -> CommunityCommentFragment()
                else -> CommunityCommentFragment()
            }
            supportFragmentManager
                .beginTransaction()
                .add(R.id.community_activity_fragment_container, fragment)
                .commit()
        }
    }
}