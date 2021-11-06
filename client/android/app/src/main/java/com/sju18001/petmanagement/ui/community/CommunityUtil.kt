package com.sju18001.petmanagement.ui.community

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dao.Pet
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import java.time.LocalDate
import java.time.Period

class CommunityUtil {
    companion object{
        fun startPetProfileFragmentFromCommunity(context: Context, pet: Pet, author: Account) {
            // create Intent for MyPetActivity
            val petProfileIntent = Intent(context, MyPetActivity::class.java)
            petProfileIntent.putExtra("petPhotoUrl", pet.photoUrl)
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
            petProfileIntent.putExtra("representativePetId", author.representativePetId)

            // start activity
            petProfileIntent.putExtra("fragmentType", "pet_profile_community")
            context.startActivity(petProfileIntent)
            (context as Activity).overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }
}