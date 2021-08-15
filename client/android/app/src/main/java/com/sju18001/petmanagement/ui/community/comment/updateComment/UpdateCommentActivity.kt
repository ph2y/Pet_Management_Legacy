package com.sju18001.petmanagement.ui.community.comment.updateComment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivityUpdateCommentBinding


class UpdateCommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateCommentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdateCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // 프래그먼트 실행
        if(supportFragmentManager.findFragmentById(R.id.update_comment_activity_fragment_container) == null){
            supportFragmentManager
                .beginTransaction()
                .add(R.id.update_comment_activity_fragment_container, UpdateCommentFragment())
                .commit()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }
}