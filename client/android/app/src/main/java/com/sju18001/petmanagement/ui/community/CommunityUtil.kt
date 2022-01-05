package com.sju18001.petmanagement.ui.community

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dao.Pet
import com.sju18001.petmanagement.restapi.dto.FetchPetReqDto
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
            petProfileIntent.putExtra("petGender", Util.getGenderSymbol(pet.gender, context))
            petProfileIntent.putExtra("petAge", Util.getAgeFromBirth(pet.birth))
            petProfileIntent.putExtra("petMessage", pet.message)

            petProfileIntent.putExtra("fragmentType", "pet_profile_community")
            context.startActivity(petProfileIntent)
            (context as Activity).overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        fun fetchRepresentativePetAndStartPetProfile(context: Context, account: Account, isViewDestroyed: Boolean) {
            // get the representative pet of account
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(context)!!)
                .fetchPetReq(FetchPetReqDto(null, account.username))
            ServerUtil.enqueueApiCall(call, {isViewDestroyed}, context, { response ->
                if (response.body()?.petList!!.isEmpty()) {
                    val emptyPetListMessage = account.nickname + context.getText(R.string.empty_pet_list_for_account_message)
                    Toast.makeText(context, emptyPetListMessage, Toast.LENGTH_LONG).show()
                }
                else {
                    for (pet in response.body()?.petList!!) {
                        if (pet.id == account.representativePetId) {
                            // start pet profile for representative pet
                            startPetProfileFragmentFromCommunity(context, pet, account)
                            return@enqueueApiCall
                        }
                    }
                    // if no representative pet is set
                    startPetProfileFragmentFromCommunity(context, response.body()?.petList!![0], account)
                }
            }, {}, {})
        }
    }
}