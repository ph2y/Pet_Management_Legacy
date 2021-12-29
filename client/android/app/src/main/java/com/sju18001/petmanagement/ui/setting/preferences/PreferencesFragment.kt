package com.sju18001.petmanagement.ui.setting.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.sju18001.petmanagement.R

class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}