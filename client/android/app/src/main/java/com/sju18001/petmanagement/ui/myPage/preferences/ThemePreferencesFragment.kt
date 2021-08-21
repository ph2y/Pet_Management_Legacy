package com.sju18001.petmanagement.ui.myPage.preferences

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.sju18001.petmanagement.R

class ThemePreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey)

        val lightModePreference: Preference? = findPreference("light_theme_preference")
        lightModePreference?.setOnPreferenceClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            true
        }

        val darkModePreference: Preference? = findPreference("dark_theme_preference")
        darkModePreference?.setOnPreferenceClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            true
        }

        val systemModePreference: Preference? = findPreference("system_theme_preference")
        systemModePreference?.setOnPreferenceClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            true
        }
    }
}