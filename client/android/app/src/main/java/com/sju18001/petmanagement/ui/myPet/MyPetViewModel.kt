package com.sju18001.petmanagement.ui.myPet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MyPetViewModel(private val handle: SavedStateHandle) : ViewModel() {
    // variables for pet manager
    var lastScrolledIndex = handle.get<Int>("lastScrolledIndex")?: 0
        set(value){
            handle.set("lastScrolledIndex", value)
            field = value
        }
    var fragmentType = handle.get<String>("fragmentType")
        set(value){
            handle.set("fragmentType", value)
            field = value
        }

    // variables for author profile
    var loadedAuthorFromIntent = handle.get<Boolean>("loadedAuthorFromIntent")?: false
        set(value){
            handle.set("loadedAuthorFromIntent", value)
            field = value
        }
    var accountIdValue = handle.get<Long>("accountIdValue")
        set(value) {
            handle.set("accountIdValue", value)
            field = value
        }
    var accountUsernameValue = handle.get<String>("accountUsernameValue")
        set(value) {
            handle.set("accountUsernameValue", value)
            field = value
        }
    var accountPhotoUrlValue = handle.get<String>("accountPhotoUrlValue")
        set(value){
            handle.set("accountPhotoUrlValue", value)
            field = value
        }
    var accountPhotoByteArray = handle.get<ByteArray>("accountPhotoByteArray")
        set(value){
            handle.set("accountPhotoByteArray", value)
            field = value
        }
    var accountNicknameValue = handle.get<String>("accountNicknameValue")
        set(value) {
            handle.set("accountNicknameValue", value)
            field = value
        }
    var accountRepresentativePetId = handle.get<Long>("accountRepresentativePetId")
        set(value){
            handle.set("accountRepresentativePetId", value)
            field = value
        }

    // variables for pet profile
    var loadedPetFromIntent = handle.get<Boolean>("loadedPetFromIntent")?: false
        set(value){
            handle.set("loadedPetFromIntent", value)
            field = value
        }
    var petPhotoUrlValueProfile = handle.get<String>("petPhotoUrlValueProfile")
        set(value){
            handle.set("petPhotoUrlValueProfile", value)
            field = value
        }
    var petIdValueProfile = handle.get<Long>("petIdValueProfile")
        set(value) {
            handle.set("petIdValueProfile", value)
            field = value
        }
    var petPhotoByteArrayProfile = handle.get<ByteArray>("petPhotoByteArrayProfile")
        set(value){
            handle.set("petPhotoByteArrayProfile", value)
            field = value
        }
    var petNameValueProfile = handle.get<String>("petNameValueProfile")?: ""
        set(value){
            handle.set("petNameValueProfile", value)
            field = value
        }
    var petBirthValueProfile = handle.get<String>("petBirthValueProfile")?: ""
        set(value){
            handle.set("petBirthValueProfile", value)
            field = value
        }
    var petSpeciesValueProfile = handle.get<String>("petSpeciesValueProfile")?: ""
        set(value){
            handle.set("petSpeciesValueProfile", value)
            field = value
        }
    var petBreedValueProfile = handle.get<String>("petBreedValueProfile")?: ""
        set(value){
            handle.set("petBreedValueProfile", value)
            field = value
        }
    var petGenderValueProfile = handle.get<String>("petGenderValueProfile")?: ""
        set(value){
            handle.set("petGenderValueProfile", value)
            field = value
        }
    var petAgeValueProfile = handle.get<String>("petAgeValueProfile")?: ""
        set(value){
            handle.set("petAgeValueProfile", value)
            field = value
        }
    var petMessageValueProfile = handle.get<String>("petMessageValueProfile")?: ""
        set(value){
            handle.set("petMessageValueProfile", value)
            field = value
        }
    var isRepresentativePetProfile = handle.get<Boolean>("isRepresentativePetProfile")?: false
        set(value){
            handle.set("isRepresentativePetProfile", value)
            field = value
        }

    // variables for pet id - name
    var petNameForId = handle.get<HashMap<Long, String>>("petNameForId")?: HashMap()
        set(value){
            handle.set("petNameForId", value)
            field = value
        }

    fun addPetNameForId(id: Long, name: String){
        petNameForId[id] = name
    }

    // variables for create/update pet
    var petIdValue = handle.get<Long>("petIdValue")
        set(value) {
            handle.set("petIdValue", value)
            field = value
        }
    var petPhotoByteArray = handle.get<ByteArray>("petPhotoByteArray")
        set(value){
            handle.set("petPhotoByteArray", value)
            field = value
        }
    var petPhotoPathValue = handle.get<String>("petPhotoPathValue")?: ""
        set(value){
            handle.set("petPhotoPathValue", value)
            field = value
        }
    var petMessageValue = handle.get<String>("petMessageValue")?: ""
        set(value){
            handle.set("petMessageValue", value)
            field = value
        }
    var petNameValue = handle.get<String>("petNameValue")?: ""
        set(value){
            handle.set("petNameValue", value)
            field = value
        }
    var petGenderValue = handle.get<Boolean>("petGenderValue")
        set(value){
            handle.set("petGenderValue", value)
            field = value
        }
    var petSpeciesValue = handle.get<String>("petSpeciesValue")?: ""
        set(value){
            handle.set("petSpeciesValue", value)
            field = value
        }
    var petBreedValue = handle.get<String>("petBreedValue")?: ""
        set(value){
            handle.set("petBreedValue", value)
            field = value
        }
    var petBirthYearValue = handle.get<Int>("petBirthYearValue")
        set(value){
            handle.set("petBirthYearValue", value)
            field = value
        }
    var petBirthMonthValue = handle.get<Int>("petBirthMonthValue")
        set(value){
            handle.set("petBirthMonthValue", value)
            field = value
        }
    var petBirthDateValue = handle.get<Int>("petBirthDateValue")
        set(value){
            handle.set("petBirthDateValue", value)
            field = value
        }
    var petBirthIsYearOnlyValue = handle.get<Boolean>("petBirthIsYearOnlyValue")?: false
        set(value){
            handle.set("petBirthIsYearOnlyValue", value)
            field = value
        }
    var petManagerApiIsLoading = handle.get<Boolean>("petManagerApiIsLoading")?: false
        set(value) {
            handle.set("petManagerApiIsLoading", value)
            field = value
        }
    var isDeletePhoto = handle.get<Boolean>("isDeletePhoto")?: false
        set(value) {
            handle.set("isDeletePhoto", value)
            field = value
        }

    // variables for schedule manager
    var time = handle.get<String>("time")?: 0
        set(value){
            handle.set("time", value)
            field = value
        }

    var isPetChecked = handle.get<Array<Boolean>>("isPetChecked")?: null
        set(value){
            handle.set("isPetChecked", value)
            field = value
        }

    var memo = handle.get<String>("memo")?: ""
        set(value){
            handle.set("memo", value)
            field = value
        }
}