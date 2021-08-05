package com.sju18001.petmanagement

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.ActivityMainBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.FetchPetScheduleResDto
import com.sju18001.petmanagement.ui.community.CommunityFragment
import com.sju18001.petmanagement.ui.map.MapFragment
import com.sju18001.petmanagement.ui.myPage.MyPageFragment
import com.sju18001.petmanagement.ui.myPet.MyPetFragment
import com.sju18001.petmanagement.ui.myPet.petManager.PetManagerFragment
import com.sju18001.petmanagement.ui.myPet.petScheduleManager.PetScheduleNotification
import com.sju18001.petmanagement.ui.myPet.petScheduleManager.PetScheduleWorker
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

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get session manager
        sessionManager = SessionManager(context = applicationContext)

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
                activeFragment = myPetFragment
                activeFragmentIndex = 0
                actionBar?.setTitle(R.string.title_my_pet)
                actionBar?.show()
            }
            1 -> {
                activeFragment = mapFragment
                activeFragmentIndex = 1
                actionBar?.hide()
            }
            2 -> {
                activeFragment = communityFragment
                activeFragmentIndex = 2
                actionBar?.setTitle(R.string.title_community)
                actionBar?.show()
            }
            3 -> {
                activeFragment = myPageFragment
                activeFragmentIndex = 3
                actionBar?.setTitle(R.string.title_my_page)
                actionBar?.show()
            }
            else -> {
                activeFragment = myPetFragment
                activeFragmentIndex = 0
                actionBar?.setTitle(R.string.title_my_pet)
                actionBar?.show()
            }
        }

        // add fragments and show active fragment
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, myPetFragment, "myPet").hide(myPetFragment).commitNow()
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, mapFragment, "map").hide(mapFragment).commitNow()
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, communityFragment, "community").hide(communityFragment).commitNow()
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main, myPageFragment, "myPage").hide(myPageFragment).commitNow()
        fragmentManager.beginTransaction().show(activeFragment).commitNow()

        navView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navigation_my_pet -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(myPetFragment).commitNow()
                    navView.menu.getItem(0).isChecked = true
                    actionBar?.setTitle(R.string.title_my_pet)
                    actionBar?.show()
                    activeFragmentIndex = 0
                    activeFragment = myPetFragment
                    true
                }
                R.id.navigation_map -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(mapFragment).commitNow()
                    navView.menu.getItem(1).isChecked = true
                    actionBar?.setShowHideAnimationEnabled(false)
                    actionBar?.hide()
                    activeFragmentIndex = 1
                    activeFragment = mapFragment
                    true
                }
                R.id.navigation_community -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(communityFragment).commitNow()
                    navView.menu.getItem(2).isChecked = true
                    actionBar?.setTitle(R.string.title_community)
                    actionBar?.show()
                    activeFragmentIndex = 2
                    activeFragment = communityFragment
                    true
                }
                R.id.navigation_my_page -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(myPageFragment).commitNow()
                    navView.menu.getItem(3).isChecked = true
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

    private fun synchronizeNotificationWorkManager(){
        // 모든 알림 취소
        PetScheduleNotification.cancelAllWorkManager(applicationContext)
        
        // PetSchedule Fetch한 뒤, 알림 등록
        val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")
        
        val fetchPetScheduleApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).fetchPetScheduleReq(body)
        fetchPetScheduleApiCall!!.enqueue(object: Callback<FetchPetScheduleResDto> {
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