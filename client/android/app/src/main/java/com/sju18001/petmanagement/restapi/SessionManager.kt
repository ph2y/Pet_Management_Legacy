package com.sju18001.petmanagement.restapi

import android.content.Context
import android.content.SharedPreferences
import com.sju18001.petmanagement.R

class SessionManager {
    companion object {
        fun saveUserToken(context:Context, token: String) {
            val prefs = context.getSharedPreferences(context.getString(R.string.user_token), Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString(context.getString(R.string.user_token), token)
            editor.apply()
        }

        fun fetchUserToken(context:Context): String? {
            val prefs = context.getSharedPreferences(context.getString(R.string.user_token), Context.MODE_PRIVATE)
            return prefs.getString(context.getString(R.string.user_token), null)
        }

        fun removeUserToken(context:Context) {
            val prefs = context.getSharedPreferences(context.getString(R.string.user_token), Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.remove(context.getString(R.string.user_token))
            editor.apply()
        }
    }
}