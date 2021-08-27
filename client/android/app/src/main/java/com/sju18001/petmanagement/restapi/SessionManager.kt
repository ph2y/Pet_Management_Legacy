package com.sju18001.petmanagement.restapi

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.restapi.dao.Account

class SessionManager {
    companion object {
        private const val TOKEN = "TOKEN"
        private const val ACCOUNT = "ACCOUNT"

        fun saveUserToken(context:Context, token: String) {
            val prefs = context.getSharedPreferences(context.getString(R.string.user_session), Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString(TOKEN, token)
            editor.apply()
        }

        fun fetchUserToken(context:Context): String? {
            val prefs = context.getSharedPreferences(context.getString(R.string.user_session), Context.MODE_PRIVATE)
            return prefs.getString(TOKEN, null)
        }

        fun removeUserToken(context:Context) {
            val prefs = context.getSharedPreferences(context.getString(R.string.user_session), Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.remove(TOKEN)
            editor.apply()
        }

        fun saveLoggedInAccount(context: Context, account: Account) {
            val prefs = context.getSharedPreferences(context.getString(R.string.user_session), Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString(ACCOUNT, Gson().toJson(account))
            editor.apply()
        }

        fun fetchLoggedInAccount(context:Context): Account? {
            val prefs = context.getSharedPreferences(context.getString(R.string.user_session), Context.MODE_PRIVATE)
            return Gson().fromJson(prefs.getString(ACCOUNT, null), Account::class.java)
        }

        fun removeLoggedInAccount(context:Context) {
            val prefs = context.getSharedPreferences(context.getString(R.string.user_session), Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.remove(ACCOUNT)
            editor.apply()
        }
    }
}