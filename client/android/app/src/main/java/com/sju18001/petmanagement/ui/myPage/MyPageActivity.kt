package com.sju18001.petmanagement.ui.myPage

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.ActivityMyPageBinding
import com.sju18001.petmanagement.ui.myPage.account.EditAccountFragment
import com.sju18001.petmanagement.ui.myPage.information.LicenseFragment
import com.sju18001.petmanagement.ui.myPage.information.TermsAndPoliciesFragment
import com.sju18001.petmanagement.ui.myPage.preferences.NotificationPreferencesFragment
import com.sju18001.petmanagement.ui.myPage.preferences.PreferencesFragment
import com.sju18001.petmanagement.ui.myPage.preferences.ThemePreferencesFragment

class MyPageActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivityMyPageBinding

    // variable for ViewModel
    private val myPageViewModel: MyPageViewModel by lazy{
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(MyPageViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // get fragment type and show it(for first launch)
        val fragmentType = intent.getStringExtra("fragmentType")

        if(supportFragmentManager.findFragmentById(R.id.my_page_activity_fragment_container) == null) {
            val fragment = when(fragmentType){
                "account_edit" -> {
                    actionBar?.setTitle(R.string.account)
                    EditAccountFragment()
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
                "terms_and_policies" -> {
                    actionBar?.setTitle(R.string.terms_and_policies_title)
                    TermsAndPoliciesFragment()
                }
                "license" -> {
                    actionBar?.setTitle(R.string.license_title)
                    LicenseFragment()
                }
                else -> PreferencesFragment()
            }
            supportFragmentManager
                .beginTransaction()
                .add(R.id.my_page_activity_fragment_container, fragment)
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

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }
}