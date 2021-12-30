package com.sju18001.petmanagement

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sju18001.petmanagement.databinding.ActivityMainBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.ui.community.CommunityFragment
import com.sju18001.petmanagement.ui.community.followerFollowing.FollowerFollowingActivity
import com.sju18001.petmanagement.ui.map.MapFragment
import com.sju18001.petmanagement.ui.setting.SettingFragment
import com.sju18001.petmanagement.ui.myPet.MyPetFragment
import com.sju18001.petmanagement.ui.myPet.petScheduleManager.PetScheduleNotification
import java.security.MessageDigest
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var myPetFragment: Fragment = MyPetFragment()
    private var mapFragment: Fragment = MapFragment()
    private var communityFragment: Fragment = CommunityFragment()
    private var settingFragment: Fragment = SettingFragment()
    private var fragmentManager: FragmentManager = supportFragmentManager
    private lateinit var activeFragment: Fragment
    private var activeFragmentIndex: Int = 0

    private var isViewDestroyed = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isViewDestroyed = false

        // for fragment reset(after activity destruction)
        fragmentManager.findFragmentByTag("myPet")?.let {
            fragmentManager.beginTransaction().remove(it).commitNow()
        }
        fragmentManager.findFragmentByTag("map")?.let {
            fragmentManager.beginTransaction().remove(it).commitNow()
        }
        fragmentManager.findFragmentByTag("community")?.let {
            fragmentManager.beginTransaction().remove(it).commitNow()
        }
        fragmentManager.findFragmentByTag("setting")?.let {
            fragmentManager.beginTransaction().remove(it).commitNow()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        val navView: BottomNavigationView = binding.navView

        // get current selected item + set title
        when(savedInstanceState?.getInt("active_fragment_index")) {
            0 -> {
                addFragmentWhenFragmentIsNull(myPetFragment, "myPet")
                activeFragment = myPetFragment

                navView.menu.getItem(0).isChecked = true
                activeFragmentIndex = 0
                invalidateOptionsMenu()
                actionBar?.setTitle(R.string.title_my_pet)
                actionBar?.show()
            }
            /* TODO: 지도 복구 시 해당 코드 복구 + 0123으로 인덱싱
            1 -> {
                addFragmentWhenFragmentIsNull(mapFragment, "map")
                activeFragment = mapFragment

                navView.menu.getItem(1).isChecked = true
                activeFragmentIndex = 1
                invalidateOptionsMenu()
                actionBar?.hide()
            }*/
            1 -> {
                addFragmentWhenFragmentIsNull(communityFragment, "community")
                activeFragment = communityFragment

                navView.menu.getItem(1).isChecked = true
                activeFragmentIndex = 1
                invalidateOptionsMenu()
                actionBar?.setTitle(R.string.title_community)
                actionBar?.show()
            }
            2 -> {
                addFragmentWhenFragmentIsNull(settingFragment, "setting")
                activeFragment = settingFragment

                navView.menu.getItem(2).isChecked = true
                activeFragmentIndex = 2
                invalidateOptionsMenu()
                actionBar?.setTitle(R.string.title_setting)
                actionBar?.show()
            }
            else -> {
                addFragmentWhenFragmentIsNull(myPetFragment, "myPet")
                activeFragment = myPetFragment

                navView.menu.getItem(0).isChecked = true
                activeFragmentIndex = 0
                invalidateOptionsMenu()
                actionBar?.setTitle(R.string.title_my_pet)
                actionBar?.show()
            }
        }

        // Show active fragment
        fragmentManager.beginTransaction().show(activeFragment).commitNow()

        navView.setOnNavigationItemSelectedListener {
            // 커뮤니티에서 다른 탭으로 이동 시
            if(activeFragment.tag == "community"){
                (activeFragment as CommunityFragment).pauseAllVideos()
            }

            when(it.itemId){
                R.id.navigation_my_pet -> {
                    addFragmentWhenFragmentIsNull(myPetFragment, "myPet")
                    fragmentManager.beginTransaction().hide(activeFragment).show(myPetFragment).commitNow()

                    navView.menu.getItem(0).isChecked = true

                    invalidateOptionsMenu()
                    actionBar?.setTitle(R.string.title_my_pet)
                    actionBar?.show()

                    activeFragmentIndex = 0
                    activeFragment = myPetFragment

                    true
                }
                /*
                TODO: 지도 복구 시 해당 코드 복구
                commit: 80872d41103f4a4782e59ab014c6c14d24995b45

                R.id.navigation_map -> {
                    addFragmentWhenFragmentIsNull(mapFragment, "map")
                    fragmentManager.beginTransaction().hide(activeFragment).show(mapFragment).commitNow()

                    navView.menu.getItem(1).isChecked = true

                    invalidateOptionsMenu()
                    actionBar?.setShowHideAnimationEnabled(false)
                    actionBar?.hide()

                    activeFragmentIndex = 1
                    activeFragment = mapFragment

                    true
                }*/
                R.id.navigation_community -> {
                    addFragmentWhenFragmentIsNull(communityFragment, "community")
                    fragmentManager.beginTransaction().hide(activeFragment).show(communityFragment).commitNow()

                    navView.menu.getItem(1).isChecked = true

                    invalidateOptionsMenu()
                    actionBar?.setTitle(R.string.title_community)
                    actionBar?.show()

                    activeFragmentIndex = 1
                    activeFragment = communityFragment

                    (activeFragment as CommunityFragment).startAllVideos()

                    true
                }
                R.id.navigation_setting -> {
                    addFragmentWhenFragmentIsNull(settingFragment, "setting")
                    fragmentManager.beginTransaction().hide(activeFragment).show(settingFragment).commitNow()

                    navView.menu.getItem(2).isChecked = true

                    invalidateOptionsMenu()
                    actionBar?.setTitle(R.string.title_setting)
                    actionBar?.show()

                    activeFragmentIndex = 2
                    activeFragment = settingFragment

                    true
                }
            }
            false
        }

        synchronizeNotificationWorkManager()
    }

    override fun onDestroy() {
        super.onDestroy()

        isViewDestroyed = true
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(baseContext.getString(R.string.exit_dialog))
            .setPositiveButton(
                R.string.confirm
            ) { _, _ ->
                super.onBackPressed()
            }
            .setNegativeButton(
                R.string.cancel
            ) { dialog, _ ->
                dialog.cancel()
            }
            .create().show()
    }

    // for saving currently active fragment index
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
        outState.putInt("active_fragment_index", activeFragmentIndex)
    }

    // for action bar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        when(activeFragmentIndex) {
            0 -> { return false }
            // 1 -> { return false } TODO: 지도 복구 시 해당 코드 복구
            1 -> {
                menuInflater.inflate(R.menu.follower_following_menu, menu)
                return true
            }
            2 -> { return false }
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(activeFragmentIndex) {
            0 -> { return false }
            // 1 -> { return false } TODO: 지도 복구 시 해당 코드 복구
            1 -> {
                // start follower following activity
                startActivity(Intent(this, FollowerFollowingActivity::class.java))
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)

                return super.onOptionsItemSelected(item)
            }
            2 -> { return false }
        }
        return false
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

    private fun addFragmentWhenFragmentIsNull(fragment: Fragment, tag: String){
        if(fragmentManager.findFragmentByTag(tag) == null){
            fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, fragment, tag).commitNow()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun synchronizeNotificationWorkManager(){
        PetScheduleNotification.cancelAllWorkManager(applicationContext)

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(baseContext)!!)
            .fetchPetScheduleReq(ServerUtil.getEmptyBody())
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, this, { response ->
            // ON인 것들에 대해 알림 설정
            response.body()?.petScheduleList?.map{
                if(it.enabled){
                    PetScheduleNotification.enqueueNotificationWorkManager(applicationContext, it.time, it.memo)
                }
            }
        }, {}, {})
    }
}