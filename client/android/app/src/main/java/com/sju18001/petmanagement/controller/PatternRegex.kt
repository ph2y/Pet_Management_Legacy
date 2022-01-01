package com.sju18001.petmanagement.controller

import android.util.Patterns
import java.util.regex.Pattern

class PatternRegex {
    companion object {
        // pattern regex for EditTexts
        private val patternUsername: Pattern = Pattern.compile("^[a-z0-9]{5,16}$")
        private val patternPassword: Pattern = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{8,20}$")
        private val patternEmail: Pattern = Patterns.EMAIL_ADDRESS
        private val patternPhone: Pattern = Pattern.compile("(^02|^\\d{3})-(\\d{3}|\\d{4})-\\d{4}")
        private val patternNickname: Pattern = Pattern.compile("(^[가-힣ㄱ-ㅎa-zA-Z0-9]{2,20}$)")
        private val patternHashtag: Pattern = Pattern.compile("(^[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9_]{1,20}$)")

        fun checkUsernameRegex(s: CharSequence?): Boolean {
            return patternUsername.matcher(s).matches()
        }

        fun checkPasswordRegex(s: CharSequence?): Boolean {
            return patternPassword.matcher(s).matches()
        }

        fun checkEmailRegex(s: CharSequence?): Boolean {
            return patternEmail.matcher(s).matches()
        }

        fun checkPhoneRegex(s: CharSequence?): Boolean {
            return patternPhone.matcher(s).matches()
        }

        fun checkNicknameRegex(s: CharSequence?): Boolean {
            return patternNickname.matcher(s).matches()
        }

        fun checkHashtagRegex(s: CharSequence?): Boolean {
            return patternHashtag.matcher(s).matches()
        }
    }
}