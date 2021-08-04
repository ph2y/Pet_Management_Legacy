package com.sju18001.petmanagement.ui.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class LoginViewModel(private val handle: SavedStateHandle): ViewModel() {
    // variables for login
    var loginUsernameEditText = handle.get<String>("loginUsernameEditText")?: ""
        set(value){
            handle.set("loginUsernameEditText", value)
            field = value
        }
    var loginPwEditText = handle.get<String>("loginPwEditText")?: ""
        set(value){
            handle.set("loginPwEditText", value)
            field = value
        }

    // variables for create account
        // terms
    var createAccountSelectAllCheckBox = handle.get<Boolean>("createAccountSelectAllCheckBox")?: false
        set(value){
            handle.set("createAccountSelectAllCheckBox", value)
            field = value
        }
    var createAccountTermsCheckBox = handle.get<Boolean>("createAccountTermsCheckBox")?: false
        set(value){
            handle.set("createAccountTermsCheckBox", value)
            field = value
        }
    var createAccountPrivacyCheckBox = handle.get<Boolean>("createAccountPrivacyCheckBox")?: false
        set(value){
            handle.set("createAccountPrivacyCheckBox", value)
            field = value
        }
    var createAccountMarketingCheckBox = handle.get<Boolean>("createAccountMarketingCheckBox")?: false
        set(value){
            handle.set("createAccountMarketingCheckBox", value)
            field = value
        }

        // username/password
    var createAccountUsernameEditText = handle.get<String>("createAccountUsernameEditText")?: ""
        set(value){
            handle.set("createAccountUsernameEditText", value)
            field = value
        }
    var createAccountUsernameValid = handle.get<Boolean>("createAccountUsernameValid")?: false
        set(value){
            handle.set("createAccountUsernameValid", value)
            field = value
        }
    var createAccountUsernameIsOverlap = handle.get<Boolean>("createAccountUsernameIsOverlap")?: false
        set(value){
            handle.set("createAccountUsernameIsOverlap", value)
            field = value
        }
    var createAccountPwEditText = handle.get<String>("createAccountPwEditText")?: ""
        set(value){
            handle.set("createAccountPwEditText", value)
            field = value
        }
    var createAccountPwValid = handle.get<Boolean>("createAccountPwValid")?: false
        set(value){
            handle.set("createAccountPwValid", value)
            field = value
        }
    var createAccountPwCheckEditText = handle.get<String>("createAccountPwCheckEditText")?: ""
        set(value){
            handle.set("createAccountPwCheckEditText", value)
            field = value
        }
    var createAccountPwCheckValid = handle.get<Boolean>("createAccountPwCheckValid")?: false
        set(value){
            handle.set("createAccountPwCheckValid", value)
            field = value
        }

        // user info
    var createAccountPhoneEditText = handle.get<String>("createAccountPhoneEditText")?: ""
        set(value){
            handle.set("createAccountPhoneEditText", value)
            field = value
        }
    var createAccountPhoneValid = handle.get<Boolean>("createAccountPhoneValid")?: false
        set(value){
            handle.set("createAccountPhoneValid", value)
            field = value
        }
    var createAccountPhoneIsOverlap = handle.get<Boolean>("createAccountPhoneIsOverlap")?: false
        set(value){
            handle.set("createAccountPhoneIsOverlap", value)
            field = value
        }
    var createAccountEmailEditText = handle.get<String>("createAccountEmailEditText")?: ""
        set(value){
            handle.set("createAccountEmailEditText", value)
            field = value
        }
    var createAccountEmailValid = handle.get<Boolean>("createAccountEmailValid")?: false
        set(value){
            handle.set("createAccountEmailValid", value)
            field = value
        }
    var createAccountEmailIsOverlap = handle.get<Boolean>("createAccountEmailIsOverlap")?: false
        set(value){
            handle.set("createAccountEmailIsOverlap", value)
            field = value
        }
    var createAccountEmailCodeEditText = handle.get<String>("createAccountEmailCodeEditText")?: ""
        set(value){
            handle.set("createAccountEmailCodeEditText", value)
            field = value
        }
    var currentCodeRequestedEmail = handle.get<String>("currentCodeRequestedEmail")?: ""
        set(value){
            handle.set("currentCodeRequestedEmail", value)
            field = value
        }
    var showsEmailRequestMessage = handle.get<Boolean>("showsEmailRequestMessage")?: false
        set(value){
            handle.set("showsEmailRequestMessage", value)
            field = value
        }
    var emailCodeChronometerBase = handle.get<Long>("emailCodeChronometerBase")?: 0
        set(value){
            handle.set("emailCodeChronometerBase", value)
            field = value
        }
    var emailCodeValid = handle.get<Boolean>("emailCodeValid")?: false
        set(value){
            handle.set("emailCodeValid", value)
            field = value
        }

    public fun resetCreateAccountValues() {
        createAccountSelectAllCheckBox = false
        createAccountTermsCheckBox = false
        createAccountPrivacyCheckBox = false
        createAccountMarketingCheckBox = false

        createAccountUsernameEditText = ""
        createAccountUsernameValid = false
        createAccountUsernameIsOverlap = false
        createAccountPwEditText = ""
        createAccountPwValid = false
        createAccountPwCheckEditText = ""
        createAccountPwCheckValid = false

        createAccountPhoneEditText = ""
        createAccountPhoneValid = false
        createAccountEmailEditText = ""
        createAccountEmailValid = false
        createAccountEmailIsOverlap = false
        createAccountEmailCodeEditText = ""
        currentCodeRequestedEmail = ""
        showsEmailRequestMessage = false
        emailCodeChronometerBase = 0
        emailCodeValid = false
    }
}