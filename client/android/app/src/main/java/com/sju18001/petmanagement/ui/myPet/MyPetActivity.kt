package com.sju18001.petmanagement.ui.myPet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivityMyPetBinding
import com.sju18001.petmanagement.ui.myPet.petScheduleManager.CreateUpdatePetScheduleFragment
import com.sju18001.petmanagement.ui.myPet.petManager.CreateUpdatePetFragment
import com.sju18001.petmanagement.ui.myPet.petManager.PetProfileFragment

class MyPetActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivityMyPetBinding

    // variable for ViewModel
    private val myPetViewModel: MyPetViewModel by lazy{
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(MyPetViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivityMyPetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // get fragment type and show it(for first launch)
        val fragmentType = intent.getStringExtra("fragmentType")

        if(supportFragmentManager.findFragmentById(R.id.my_pet_activity_fragment_container) == null) {
            val fragment = when(fragmentType){
                "create_pet" -> CreateUpdatePetFragment()
                "pet_profile_pet_manager" -> PetProfileFragment()
                "pet_profile_community" -> PetProfileFragment()
                else -> CreateUpdatePetScheduleFragment()
            }
            supportFragmentManager
                .beginTransaction()
                .add(R.id.my_pet_activity_fragment_container, fragment)
                .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }
}