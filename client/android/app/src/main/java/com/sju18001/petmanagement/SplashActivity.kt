package com.sju18001.petmanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sju18001.petmanagement.ui.login.LoginActivity

class SplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // load preference values
        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val defaultThemeValue = resources.getInteger(R.integer.saved_default_theme_preference_key)
        val themeValue = sharedPref.getInt(getString(R.string.saved_theme_preference_key), defaultThemeValue)

        AppCompatDelegate.setDefaultNightMode(themeValue)

        // token check & start activity
        // TODO: Implement token validation & login/main activity start: If token is valid -> MainActivity / else -> LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

        finish()
    }
}