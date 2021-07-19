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

    public fun resetSignUpValues() {
        signUpSelectAllCheckBox = false
        signUpTermsCheckBox = false
        signUpPrivacyCheckBox = false
        signUpMarketingCheckBox = false

        signUpIdEditText = ""
        signUpIdValid = false
        signUpPwEditText = ""
        signUpPwValid = false
        signUpPwCheckEditText = ""
        signUpPwCheckValid = false
    }
}