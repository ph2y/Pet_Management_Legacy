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
import java.time.format.DateTimeFormatter

class CommunityUtil {
    companion object{
        fun startPetProfileFragmentFromCommunity(context: Context, pet: Pet, author: Account) {
            val petProfileIntent = Intent(context, MyPetActivity::class.java)

            petProfileIntent.putExtra("accountId", author.id)
            petProfileIntent.putExtra("accountUsername", author.username)
            petProfileIntent.putExtra("accountPhotoUrl", author.photoUrl)
            petProfileIntent.putExtra("accountNickname", author.nickname)
            petProfileIntent.putExtra("representativePetId", author.representativePetId)

            petProfileIntent.putExtra("petPhotoUrl", pet.photoUrl)
            petProfileIntent.putExtra("petId", pet.id)
            petProfileIntent.putExtra("petName", pet.name)
            var petBirth = ""
            petBirth += if (pet.yearOnly!!) {
                LocalDate.parse(pet.birth).year.toString() + "년생"
            } else {
                LocalDate.parse(pet.birth).format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) + "생"
            }
            petProfileIntent.putExtra("petBirth", petBirth)
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

            petProfileIntent.putExtra("fragmentType", "pet_profile_community")
            context.startActivity(petProfileIntent)
            (context as Activity).overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }
}