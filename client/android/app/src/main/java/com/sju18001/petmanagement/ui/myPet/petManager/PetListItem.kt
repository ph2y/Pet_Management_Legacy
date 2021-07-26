package com.sju18001.petmanagement.ui.myPet.petManager

import java.time.LocalDate

class PetListItem {

    // item elements
    private var mPetId: Long? = null
    private var mPetName: String? = null
    private var mPetBirth: LocalDate? = null
    private var mPetSpecies: String? = null
    private var mPetBreed: String? = null
    private var mPetGender: Boolean? = null
    private var mPetPhotoUrl: Int? = null

    // set values for the item
    public fun setValues(petId: Long, petName: String, petBirth: LocalDate?, petSpecies: String,
                         petBreed: String?, petGender: Boolean?, petPhotoUrl: Int) {
        mPetId = petId
        mPetName = petName
        mPetBirth = petBirth
        mPetSpecies = petSpecies
        mPetBreed = petBreed
        mPetGender = petGender
        mPetPhotoUrl = petPhotoUrl
    }

    // get values from the item
    public fun getPetId() : Long? {
        return mPetId
    }
    public fun getPetName() : String? {
        return mPetName
    }
    public fun getPetBirth() : LocalDate? {
        return mPetBirth
    }
    public fun getPetSpecies() : String? {
        return mPetSpecies
    }
    public fun getPetBreed() : String? {
        return mPetBreed
    }
    public fun getPetGender() : Boolean? {
        return mPetGender
    }
    public fun getPetPhotoUrl() : Int? {
        return mPetPhotoUrl
    }
}