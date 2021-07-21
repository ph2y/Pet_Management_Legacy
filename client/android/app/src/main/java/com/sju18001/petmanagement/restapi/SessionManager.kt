package com.sju18001.petmanagement.restapi

import android.content.Context
import android.content.SharedPreferences
import com.sju18001.petmanagement.R

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.user_token), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = ""
    }

    fun saveUserToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun fetchUserToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }
}