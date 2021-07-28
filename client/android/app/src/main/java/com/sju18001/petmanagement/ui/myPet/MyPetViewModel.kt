package com.sju18001.petmanagement.ui.myPet

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.time.LocalTime

class MyPetViewModel(private val handle: SavedStateHandle) : ViewModel() {
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

    var petList = handle.get<ArrayList<Int>>("petList")?: arrayListOf()
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