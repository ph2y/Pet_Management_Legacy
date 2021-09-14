package com.sju18001.petmanagement

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
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.FetchPetScheduleResDto
import com.sju18001.petmanagement.ui.community.CommunityFragment
import com.sju18001.petmanagement.ui.community.followerFollowing.FollowerFollowingActivity
import com.sju18001.petmanagement.ui.map.MapFragment
import com.sju18001.petmanagement.ui.myPage.MyPageFragment
import com.sju18001.petmanagement.ui.myPet.MyPetFragment
import com.sju18001.petmanagement.ui.myPet.petScheduleManager.PetScheduleNotification
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var myPetFragment: Fragment = MyPetFragment()
    private var mapFragment: Fragment = MapFragment()
    private var communityFragment: Fragment = CommunityFragment()
    private var myPageFragment: Fragment = MyPageFragment()
    private var fragmentManager: FragmentManager = supportFragmentManager
    private lateinit var activeFragment: Fragment
    private var activeFragmentIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        fragmentManager.findFragmentByTag("myPage")?.let {
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

                activeFragmentIndex = 0
                invalidateOptionsMenu()
                actionBar?.setTitle(R.string.title_my_pet)
                actionBar?.show()
            }
            1 -> {
                addFragmentWhenFragmentIsNull(mapFragment, "map")
                activeFragment = mapFragment

                activeFragmentIndex = 1
                invalidateOptionsMenu()
                actionBar?.hide()
            }
            2 -> {
                addFragmentWhenFragmentIsNull(communityFragment, "community")
                activeFragment = communityFragment

                activeFragmentIndex = 2
                invalidateOptionsMenu()
                actionBar?.setTitle(R.string.title_community)
                actionBar?.show()
            }
            3 -> {
                addFragmentWhenFragmentIsNull(myPageFragment, "myPage")
                activeFragment = myPageFragment

                activeFragmentIndex = 3
                invalidateOptionsMenu()
                actionBar?.setTitle(R.string.title_my_page)
                actionBar?.show()
            }
            else -> {
                addFragmentWhenFragmentIsNull(myPetFragment, "myPet")
                activeFragment = myPetFragment

                activeFragmentIndex = 0
                invalidateOptionsMenu()
                actionBar?.setTitle(R.string.title_my_pet)
                actionBar?.show()
            }
        }

        // Show active fragment
        fragmentManager.beginTransaction().show(activeFragment).commitNow()

        navView.setOnNavigationItemSelectedListener {
            // 커뮤니티 -> 다른 탭 이동 시
            if(activeFragment.tag == "community"){
                // 모든 비디오 재생 중지
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
                }
                R.id.navigation_community -> {
                    addFragmentWhenFragmentIsNull(communityFragment, "community")
                    fragmentManager.beginTransaction().hide(activeFragment).show(communityFragment).commitNow()

                    navView.menu.getItem(2).isChecked = true

                    invalidateOptionsMenu()
                    actionBar?.setTitle(R.string.title_community)
                    actionBar?.show()

                    activeFragmentIndex = 2
                    activeFragment = communityFragment
                    
                    // 모든 동영상 재생
                    (activeFragment as CommunityFragment).startAllVideos()

                    true
                }
                R.id.navigation_my_page -> {
                    addFragmentWhenFragmentIsNull(myPageFragment, "myPage")
                    fragmentManager.beginTransaction().hide(activeFragment).show(myPageFragment).commitNow()

                    navView.menu.getItem(3).isChecked = true

                    invalidateOptionsMenu()
                    actionBar?.setTitle(R.string.title_my_page)
                    actionBar?.show()

                    activeFragmentIndex = 3
                    activeFragment = myPageFragment

                    true
                }
            }
            false
        }
        
        // 모든 알람 취소하고, PetSchedule에 따라 알림 등록
        synchronizeNotificationWorkManager()
    }

    // for saving currently active fragment index
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("active_fragment_index", activeFragmentIndex)
    }

    // for action bar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        when(activeFragmentIndex) {
            0 -> { return false }
            1 -> { return false }
            2 -> {
                menuInflater.inflate(R.menu.follower_following_menu, menu)
                return true
            }
            3 -> { return false }
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(activeFragmentIndex) {
            0 -> { return false }
            1 -> { return false }
            2 -> {
                // start follower following activity
                startActivity(Intent(this, FollowerFollowingActivity::class.java))
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)

                return super.onOptionsItemSelected(item)
            }
            3 -> { return false }
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

    private fun synchronizeNotificationWorkManager(){
        // 모든 알림 취소
        PetScheduleNotification.cancelAllWorkManager(applicationContext)
        
        // PetSchedule Fetch한 뒤, 알림 등록
        val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")
        
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(baseContext)!!).fetchPetScheduleReq(body)
        call.enqueue(object: Callback<FetchPetScheduleResDto> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<FetchPetScheduleResDto>,
                response: Response<FetchPetScheduleResDto>
            ) {
                if(response.isSuccessful){
                    // ON인 것들에 대해 알림 설정
                    response.body()?.petScheduleList?.map{
                        if(it.enabled){
                            PetScheduleNotification.enqueueNotificationWorkManager(applicationContext, it.time, it.memo)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<FetchPetScheduleResDto>, t: Throwable) {
                // Do nothing
            }
        })
    }
}