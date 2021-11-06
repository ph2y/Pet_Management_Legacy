package com.sju18001.petmanagement.ui.community

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.dao.Pet
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import java.time.LocalDate
import java.time.Period

class CommunityUtil {
    companion object{
        fun startPetProfileFragmentFromCommunity(context: Context, pet: Pet, photoByteArray: ByteArray?) {
            // save pet photo to SharedPreferences
            if (photoByteArray != null) {
                Util.saveByteArrayToSharedPreferences(context, context.getString(R.string.pref_name_byte_arrays),
                    context.getString(R.string.data_name_community_selected_pet_photo), photoByteArray)
            }
            else {
                Util.saveByteArrayToSharedPreferences(context, context.getString(R.string.pref_name_byte_arrays),
                    context.getString(R.string.data_name_community_selected_pet_photo), null)
            }

            // create Intent for MyPetActivity
            val petProfileIntent = Intent(context, MyPetActivity::class.java)
            petProfileIntent.putExtra("petId", pet.id)
            petProfileIntent.putExtra("petName", pet.name)
            petProfileIntent.putExtra("petBirth", pet.birth)
            petProfileIntent.putExtra("petSpecies", pet.species)
            petProfileIntent.putExtra("petBreed", pet.breed)
            val petGender = if(pet.gender) {
                context.getString(R.string.pet_gender_female_symbol)
            }
            else {
                context.getString(R.string.pet_gender_male_symbol)
            }
            val petAge = Period.between(LocalDate.parse(pet.birth), LocalDate.now()).years.toString()
            petProfileIntent.putExtra("petGender", petGender)
            petProfileIntent.putExtra("petAge", petAge)
            petProfileIntent.putExtra("petMessage", pet.message)

            // start activity
            petProfileIntent.putExtra("fragmentType", "pet_profile_community")
            context.startActivity(petProfileIntent)
            (context as Activity).overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }
}