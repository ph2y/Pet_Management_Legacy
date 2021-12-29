package com.sju18001.petmanagement.ui.setting.preferences

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.sju18001.petmanagement.R

class ThemePreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey)

        val sharedPref = activity?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val lightModePreference: Preference? = findPreference("light_theme_preference")
        lightModePreference?.setOnPreferenceClickListener {
            sharedPref?.edit()
                ?.putInt(getString(R.string.saved_theme_preference_key), AppCompatDelegate.MODE_NIGHT_NO)
                ?.apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            true
        }

        val darkModePreference: Preference? = findPreference("dark_theme_preference")
        darkModePreference?.setOnPreferenceClickListener {
            sharedPref?.edit()
                ?.putInt(getString(R.string.saved_theme_preference_key), AppCompatDelegate.MODE_NIGHT_YES)
                ?.apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            true
        }

        val systemModePreference: Preference? = findPreference("system_theme_preference")
        systemModePreference?.setOnPreferenceClickListener {
            sharedPref?.edit()
                ?.putInt(getString(R.string.saved_theme_preference_key), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                ?.apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            true
        }
    }
}