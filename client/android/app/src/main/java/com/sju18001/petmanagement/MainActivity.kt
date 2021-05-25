package com.sju18001.petmanagement

import android.content.pm.PackageManager
import android.media.tv.TvContract.Programs.Genres.encode
import android.os.Build
import android.os.Bundle
import android.util.Base64.encode
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.sju18001.petmanagement.databinding.ActivityMainBinding
import java.net.URLEncoder.encode
import java.security.MessageDigest
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_my_pet, R.id.navigation_map, R.id.navigation_community, R.id.navigation_my_page
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        var currentItem: Int = -1
        navView.setOnNavigationItemSelectedListener {
            if (currentItem != it.itemId) {
                currentItem = it.itemId

                NavigationUI.onNavDestinationSelected(it, navController)
                true
            }
            else {
                false
            }
        }
    }

    // 디버그 전용 Key
    private fun getAppKeyHash() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for(i in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(i.toByteArray())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val encoder = Base64.getEncoder()
                    Log.e("Debug key", encoder.encodeToString(md.digest()))
                }
            }
        } catch(e: Exception) {
            Log.e("Not found", e.toString())
        }
    }
}