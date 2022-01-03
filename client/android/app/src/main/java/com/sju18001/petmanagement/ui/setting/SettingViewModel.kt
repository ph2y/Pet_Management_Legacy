package com.sju18001.petmanagement.ui.setting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SettingViewModel(private val handle: SavedStateHandle) : ViewModel() {
    // variables for account profile fetch in setting
    var accountNicknameProfileValue = handle.get<String>("accountNicknameProfileValue")?: ""
        set(value){
            handle.set("accountNicknameProfileValue", value)
            field = value
        }
    var accountPhotoProfileByteArray = handle.get<ByteArray>("accountPhotoProfileByteArray")
        set(value){
            handle.set("accountPhotoProfileByteArray", value)
            field = value
        }

    // variables for account profile fetch/update
    var loadedFromIntent = handle.get<Boolean>("loadedFromIntent")?: false
        set(value){
            handle.set("loadedFromIntent", value)
            field = value
        }
    var accountEmailValue = handle.get<String>("accountEmailValue")?: ""
        set(value){
            handle.set("accountEmailValue", value)
            field = value
        }
    var accountPhoneValue = handle.get<String>("accountPhoneValue")?: ""
        set(value){
            handle.set("accountPhoneValue", value)
            field = value
        }
    var accountMarketingValue = handle.get<Boolean>("accountMarketingValue")
        set(value){
            handle.set("accountMarketingValue", value)
            field = value
        }
    var accountNicknameValue = handle.get<String>("accountNicknameValue")?: ""
        set(value){
            handle.set("accountNicknameValue", value)
            field = value
        }
    var accountPhotoByteArray = handle.get<ByteArray>("accountPhotoByteArray")
        set(value){
            handle.set("accountPhotoByteArray", value)
            field = value
        }
    var accountPhotoPathValue = handle.get<String>("accountPhotoPathValue")?: ""
        set(value){
            handle.set("accountPhotoPathValue", value)
            field = value
        }
    var isDeletePhoto = handle.get<Boolean>("isDeletePhoto")?: false
        set(value) {
            handle.set("isDeletePhoto", value)
            field = value
        }
    var accountUserMessageValue = handle.get<String>("accountUserMessageValue")?: ""
        set(value){
            handle.set("accountUserMessageValue", value)
            field = value
        }
    var representativePetId = handle.get<Long>("representativePetId")?: 0
        set(value){
            handle.set("representativePetId", value)
            field = value
        }
    var accountPwValid = handle.get<Boolean>("createAccountPwValid")?: false
        set(value){
            handle.set("createAccountPwValid", value)
            field = value
        }
    var accountPwCheckValid = handle.get<Boolean>("createAccountPwCheckValid")?: false
        set(value){
            handle.set("createAccountPwCheckValid", value)
            field = value
        }
    var accountApiIsLoading = handle.get<Boolean>("accountApiIsLoading")?: false
        set(value) {
            handle.set("accountApiIsLoading", value)
        }
}