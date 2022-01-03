package com.sju18001.petmanagement.ui.setting

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivitySettingBinding
import com.sju18001.petmanagement.ui.setting.account.UpdateAccountFragment
import com.sju18001.petmanagement.ui.setting.information.LicenseFragment
import com.sju18001.petmanagement.ui.setting.information.PrivacyTermsFragment
import com.sju18001.petmanagement.ui.setting.information.UsageTermsFragment
import com.sju18001.petmanagement.ui.setting.preferences.NotificationPreferencesFragment
import com.sju18001.petmanagement.ui.setting.preferences.PreferencesFragment
import com.sju18001.petmanagement.ui.setting.preferences.ThemePreferencesFragment

class SettingActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivitySettingBinding

    // variable for ViewModel
    private val settingViewModel: SettingViewModel by lazy{
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(SettingViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // get fragment type and show it(for first launch)
        val fragmentType = intent.getStringExtra("fragmentType")

        if(supportFragmentManager.findFragmentById(R.id.setting_activity_fragment_container) == null) {
            val fragment = when(fragmentType){
                "update_account" -> {
                    actionBar?.setTitle(R.string.account)
                    UpdateAccountFragment()
                }
                "preferences" -> {
                    actionBar?.setTitle(R.string.preferences)
                    PreferencesFragment()
                }
                "notification_preferences" -> {
                    actionBar?.setTitle(R.string.notification_preferences)
                    NotificationPreferencesFragment()
                }
                "theme_preferences" -> {
                    actionBar?.setTitle(R.string.theme_preferences)
                    ThemePreferencesFragment()
                }
                "privacy_terms" -> {
                    actionBar?.setTitle(R.string.privacy_terms_title)
                    PrivacyTermsFragment()
                }
                "usage_terms" -> {
                    actionBar?.setTitle(R.string.usage_terms_title)
                    UsageTermsFragment()
                }
                "license" -> {
                    actionBar?.setTitle(R.string.license_title)
                    LicenseFragment()
                }
                else -> PreferencesFragment()
            }
            supportFragmentManager
                .beginTransaction()
                .add(R.id.setting_activity_fragment_container, fragment)
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }
}