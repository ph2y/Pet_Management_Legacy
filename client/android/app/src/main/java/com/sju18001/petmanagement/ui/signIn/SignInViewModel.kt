package com.sju18001.petmanagement.ui.signIn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SignInViewModel(private val handle: SavedStateHandle): ViewModel() {
    // variables for sign in
    var signInIdEditText = handle.get<String>("signInIdEditText")?: "testId"
        set(value){
            handle.set("signInIdEditText", value)
            field = value
        }
    var signInPwEditText = handle.get<String>("signInPwEditText")?: "testPw"
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
}