package com.sju18001.petmanagement

import android.content.pm.PackageManager
import android.graphics.Color
import android.media.tv.TvContract.Programs.Genres.encode
import android.os.Build
import android.os.Bundle
import android.util.Base64.encode
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.sju18001.petmanagement.databinding.ActivityMainBinding
import com.sju18001.petmanagement.ui.community.CommunityFragment
import com.sju18001.petmanagement.ui.map.MapFragment
import com.sju18001.petmanagement.ui.myPage.MyPageFragment
import com.sju18001.petmanagement.ui.myPet.MyPetFragment
import java.net.URLEncoder.encode
import java.security.MessageDigest
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var myPetFragment: Fragment = MyPetFragment()
    private var mapFragment: Fragment = MapFragment()
    private var communityFragment: Fragment = CommunityFragment()
    private var myPageFragment: Fragment = MyPageFragment()
    private var fragmentManager: FragmentManager = supportFragmentManager
    private var activeFragment: Fragment = myPetFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        val navView: BottomNavigationView = binding.navView

        fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, myPetFragment, "myPet").commit()
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, mapFragment, "map").hide(mapFragment).commit()
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, communityFragment, "community").hide(communityFragment).commit()
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, myPageFragment, "myPage").hide(myPageFragment).commit()

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

        navView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navigation_my_pet -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(myPetFragment).commit()
                    navView.menu.getItem(0).isChecked = true

                    actionBar?.setTitle(R.string.title_my_pet)
                    actionBar?.show()

                    window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.pumpkin)

                    activeFragment = myPetFragment
                    true
                }
                R.id.navigation_map -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(mapFragment).commit()
                    navView.menu.getItem(1).isChecked = true

                    actionBar?.setShowHideAnimationEnabled(false)
                    actionBar?.hide()

                    window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.carrot)

                    activeFragment = mapFragment
                    true
                }
                R.id.navigation_community -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(communityFragment).commit()
                    navView.menu.getItem(2).isChecked = true

                    actionBar?.setTitle(R.string.title_community)
                    actionBar?.show()

                    window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.pumpkin)

                    activeFragment = communityFragment
                    true
                }
                R.id.navigation_my_page -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(myPageFragment).commit()
                    navView.menu.getItem(3).isChecked = true

                    actionBar?.setTitle(R.string.title_my_page)
                    actionBar?.show()

                    window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.pumpkin)

                    activeFragment = myPageFragment
                    true
                }
            }
            false
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