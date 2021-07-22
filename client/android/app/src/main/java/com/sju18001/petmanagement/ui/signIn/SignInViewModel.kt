package com.sju18001.petmanagement.ui.signIn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SignInViewModel(private val handle: SavedStateHandle): ViewModel() {
    // variables for sign in
    var signInIdEditText = handle.get<String>("signInIdEditText")?: ""
        set(value){
            handle.set("signInIdEditText", value)
            field = value
        }
    var signInPwEditText = handle.get<String>("signInPwEditText")?: ""
        set(value){
            handle.set("signInPwEditText", value)
            field = value
        }

    // variables for sign up
        // terms
    var signUpSelectAllCheckBox = handle.get<Boolean>("signUpSelectAllCheckBox")?: false
        set(value){
            handle.set("signUpSelectAllCheckBox", value)
            field = value
        }
    var signUpTermsCheckBox = handle.get<Boolean>("signUpTermsCheckBox")?: false
        set(value){
            handle.set("signUpTermsCheckBox", value)
            field = value
        }
    var signUpPrivacyCheckBox = handle.get<Boolean>("signUpPrivacyCheckBox")?: false
        set(value){
            handle.set("signUpPrivacyCheckBox", value)
            field = value
        }
    var signUpMarketingCheckBox = handle.get<Boolean>("signUpMarketingCheckBox")?: false
        set(value){
            handle.set("signUpMarketingCheckBox", value)
            field = value
        }

        // id/pw
    var signUpIdEditText = handle.get<String>("signUpIdEditText")?: ""
        set(value){
            handle.set("signUpIdEditText", value)
            field = value
        }
    var signUpIdValid = handle.get<Boolean>("signUpIdValid")?: false
        set(value){
            handle.set("signUpIdValid", value)
            field = value
        }
    var signUpIdIsOverlap = handle.get<Boolean>("signUpIdIsOverlap")?: false
        set(value){
            handle.set("signUpIdIsOverlap", value)
            field = value
        }
    var signUpPwEditText = handle.get<String>("signUpPwEditText")?: ""
        set(value){
            handle.set("signUpPwEditText", value)
            field = value
        }
    var signUpPwValid = handle.get<Boolean>("signUpPwValid")?: false
        set(value){
            handle.set("signUpPwValid", value)
            field = value
        }
    var signUpPwCheckEditText = handle.get<String>("signUpPwCheckEditText")?: ""
        set(value){
            handle.set("signUpPwCheckEditText", value)
            field = value
        }
    var signUpPwCheckValid = handle.get<Boolean>("signUpPwCheckValid")?: false
        set(value){
            handle.set("signUpPwCheckValid", value)
            field = value
        }

        // user info
    var signUpPhoneEditText = handle.get<String>("signUpPhoneEditText")?: ""
        set(value){
            handle.set("signUpPhoneEditText", value)
            field = value
        }
    var signUpPhoneValid = handle.get<Boolean>("signUpPhoneValid")?: false
        set(value){
            handle.set("signUpPhoneValid", value)
            field = value
        }
    var signUpPhoneIsOverlap = handle.get<Boolean>("signUpPhoneIsOverlap")?: false
        set(value){
            handle.set("signUpPhoneIsOverlap", value)
            field = value
        }
    var signUpEmailEditText = handle.get<String>("signUpEmailEditText")?: ""
        set(value){
            handle.set("signUpEmailEditText", value)
            field = value
        }
    var signUpEmailValid = handle.get<Boolean>("signUpEmailValid")?: false
        set(value){
            handle.set("signUpEmailValid", value)
            field = value
        }
    var signUpEmailIsOverlap = handle.get<Boolean>("signUpEmailIsOverlap")?: false
        set(value){
            handle.set("signUpEmailIsOverlap", value)
            field = value
        }
    var signUpEmailCodeValid = handle.get<Boolean>("signUpEmailCodeIsValid")?: false
        set(value){
            handle.set("signUpEmailCodeIsValid", value)
            field = value
        }
    var currentCodeRequestedEmail = handle.get<String>("currentCodeRequestedEmail")?: ""
        set(value){
            handle.set("currentCodeRequestedEmail", value)
            field = value
        }

    public fun resetSignUpValues() {
        signUpSelectAllCheckBox = false
        signUpTermsCheckBox = false
        signUpPrivacyCheckBox = false
        signUpMarketingCheckBox = false

        signUpIdEditText = ""
        signUpIdValid = false
        signUpIdIsOverlap = false
        signUpPwEditText = ""
        signUpPwValid = false
        signUpPwCheckEditText = ""
        signUpPwCheckValid = false

        signUpPhoneEditText = ""
        signUpPhoneValid = false
        signUpEmailEditText = ""
        signUpEmailValid = false
    }
}