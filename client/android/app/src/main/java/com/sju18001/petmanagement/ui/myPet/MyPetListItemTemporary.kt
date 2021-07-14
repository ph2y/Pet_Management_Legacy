package com.sju18001.petmanagement.ui.myPet

class MyPetListItemTemporary {
    private var mPetId: Int? = null
    private var mPetName: String? = null
    private var mPetBirth: String? = null
    private var mPetSpecies: String? = null
    private var mPetBreed: String? = null
    private var mPetGender: Boolean? = null
    private var mPetPhotoUrl: Int? = null

    // set values for the item
    public fun MyPetListItemTemporary(petId: Int, petName: String, petBirth: String, petSpecies: String, petBreed: String, petGender: Boolean, petPhotoUrl: Int) {
        mPetId = petId
        mPetName = petName
        mPetBirth = petBirth
        mPetSpecies = petSpecies
        mPetBreed = petBreed
        mPetGender = petGender
        mPetPhotoUrl = petPhotoUrl
    }

    // get values from the item
    public fun getPetId() : Int? {
        return mPetId
    }
    public fun getPetName() : String? {
        return mPetName
    }
    public fun getPetBirth() : String? {
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