package com.sju18001.petmanagement.ui.myPet

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MyPetViewModel(private val handle: SavedStateHandle) : ViewModel() {
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
        set(value){
            handle.set("addPetApiIsLoading", value)
            field = value
        }
}