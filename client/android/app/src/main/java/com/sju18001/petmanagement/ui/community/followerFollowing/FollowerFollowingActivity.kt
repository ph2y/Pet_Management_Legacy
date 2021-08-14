package com.sju18001.petmanagement.ui.community.followerFollowing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivityFollowerFollowingBinding

class FollowerFollowingActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivityFollowerFollowingBinding

    // variable for ViewModel
    private val followerFollowingViewModel: FollowerFollowingViewModel by lazy{
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(FollowerFollowingViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivityFollowerFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // hide action bar
        supportActionBar?.hide()

//        // get fragment type and show it(for first launch)
//        val fragmentType = intent.getStringExtra("fragmentType")
//
//        if(supportFragmentManager.findFragmentById(R.id.my_pet_activity_fragment_container) == null) {
//            val fragment = when(fragmentType){
//                "create_pet" -> CreateUpdatePetFragment()
//                "pet_profile_pet_manager" -> PetProfileFragment()
//                else -> PetScheduleEditFragment()
//            }
//            supportFragmentManager
//                .beginTransaction()
//                .add(R.id.my_pet_activity_fragment_container, fragment)
//                .commit()
//        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }
}