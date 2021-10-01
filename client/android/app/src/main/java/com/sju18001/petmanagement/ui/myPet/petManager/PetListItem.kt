package com.sju18001.petmanagement.ui.myPet.petManager

import java.time.LocalDate

class PetListItem {

    // item elements
    private var mPetId: Long? = null
    private var mPetName: String? = null
    private var mPetBirth: LocalDate? = null
    private var mPetYearOnly: Boolean? = null
    private var mPetSpecies: String? = null
    private var mPetBreed: String? = null
    private var mPetGender: Boolean? = null
    private var mPetPhotoUrl: String? = null
    private var mPetMessage: String? = null
    private var mIsRepresentativePet: Boolean? = null

    // set values for the item
    public fun setValues(petId: Long, petName: String, petBirth: LocalDate?, petYearOnly: Boolean?, petSpecies: String?,
                         petBreed: String?, petGender: Boolean?, petPhotoUrl: String?, petMessage: String?, isRepresentativePet: Boolean?) {
        mPetId = petId
        mPetName = petName
        mPetBirth = petBirth
        mPetYearOnly = petYearOnly
        mPetSpecies = petSpecies
        mPetBreed = petBreed
        mPetGender = petGender
        mPetPhotoUrl = petPhotoUrl
        mPetMessage = petMessage
        mIsRepresentativePet = isRepresentativePet
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
    public fun getPetYearOnly() : Boolean? {
        return mPetYearOnly
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
    public fun getPetPhotoUrl() : String? {
        return mPetPhotoUrl
    }
    public fun getPetMessage() : String? {
        return mPetMessage
    }
    public fun getIsRepresentativePet() : Boolean? {
        return mIsRepresentativePet
    }
}