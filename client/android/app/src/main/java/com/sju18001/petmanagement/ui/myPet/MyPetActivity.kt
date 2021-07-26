package com.sju18001.petmanagement.ui.myPet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.ui.myPet.petManager.AddPetFragment

class MyPetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_pet)

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