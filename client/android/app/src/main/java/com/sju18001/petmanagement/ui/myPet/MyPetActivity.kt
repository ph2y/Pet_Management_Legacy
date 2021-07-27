package com.sju18001.petmanagement.ui.myPet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivityMyPetBinding
import com.sju18001.petmanagement.databinding.ActivitySignInBinding
import com.sju18001.petmanagement.ui.myPet.petManager.AddPetFragment
import com.sju18001.petmanagement.ui.signIn.SignInViewModel

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

        // hide action bar
        supportActionBar?.hide()

        // get fragment type and show it(for first launch)
        val fragmentType = intent.getStringExtra("fragmentType")

        if(supportFragmentManager.findFragmentById(R.id.my_pet_activity_fragment_container) == null) {
            if(fragmentType == "add_pet") {
                val fragment = AddPetFragment()
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.my_pet_activity_fragment_container, fragment)
                    .commit()
            }
        }
    }
}