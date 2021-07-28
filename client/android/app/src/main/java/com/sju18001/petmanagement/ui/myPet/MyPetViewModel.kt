package com.sju18001.petmanagement.ui.myPet

import android.content.ClipData
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MyPetViewModel(private val handle: SavedStateHandle) : ViewModel() {
    // variables for pet manager
    var lastScrolledIndex = handle.get<Int>("lastScrolledIndex")?: 0
        set(value){
            handle.set("lastScrolledIndex", value)
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

    // variables for add/edit pet
    var petImageValue = handle.get<Uri>("petImageValue")
        set(value){
            handle.set("petImageValue", value)
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
    var addPetApiIsLoading = handle.get<Boolean>("addPetApiIsLoading")?: false
        set(value) {
            handle.set("addPetApiIsLoading", value)
        }

    // variables for scheduler
    var feedTimeHour = handle.get<Int>("feedTimeHour")?: 0
        set(value){
            handle.set("feedTimeHour", value)
            field = value
        }

    var feedTimeMinute = handle.get<Int>("feedTimeMinute")?: 0
        set(value){
            handle.set("feedTimeMinute", value)
            field = value
        }

    var petList = handle.get<ArrayList<Long>>("petList")?: arrayListOf()
        set(value){
            handle.set("petList", value)
            field = value
        }

    var memo = handle.get<String>("memo")?: ""
        set(value){
            handle.set("memo", value)
            field = value
        }

    var isTurnedOn = handle.get<Boolean>("isTurnedOn")?: false
        set(value){
            handle.set("isTurnedOn", value)
            field = value
        }
}