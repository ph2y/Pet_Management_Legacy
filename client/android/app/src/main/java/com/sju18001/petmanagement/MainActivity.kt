package com.sju18001.petmanagement

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sju18001.petmanagement.controller.ServerUtil
import com.sju18001.petmanagement.databinding.ActivityMainBinding
import com.sju18001.petmanagement.restapi.AccountProfileLookupResponseDto
import com.sju18001.petmanagement.restapi.AccountProfileUpdateRequestDto
import com.sju18001.petmanagement.restapi.AccountSignInRequestDto
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.ui.community.CommunityFragment
import com.sju18001.petmanagement.ui.map.MapFragment
import com.sju18001.petmanagement.ui.myPage.MyPageFragment
import com.sju18001.petmanagement.ui.myPet.MyPetFragment
import com.sju18001.petmanagement.ui.welcomePage.WelcomePageActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
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

        // For welcome page
        checkIsFirstLogin()

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


    // 첫 로그인인지 체킹
    private fun checkIsFirstLogin(){
        val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

        // API 호출
        val token:String? = intent.getStringExtra("token")
        if(token == null){
            Log.e("MainActivity", "No token!")
            return
        }

        val call = RetrofitBuilder.getServerApiWithToken(token).profileLookupRequest(body)
        call.enqueue(object: Callback<AccountProfileLookupResponseDto> {
            override fun onResponse(
                call: Call<AccountProfileLookupResponseDto>,
                response: Response<AccountProfileLookupResponseDto>
            ) {
                if(response.isSuccessful){
                    // 첫 로그인일 시
                    if(response.body()!!.photo.isNullOrEmpty()){
                        // photo -> default, nickname -> username
                        ServerUtil.updateProfile(
                            token,
                            AccountProfileUpdateRequestDto(
                                response.body()!!.username, response.body()!!.email, response.body()!!.username, response.body()!!.phone,
                                "default", response.body()!!.marketing, response.body()!!.userMessage
                            )
                        )
                        
                        // 웰컴 페이지 호출
                        val intent = Intent(baseContext, WelcomePageActivity::class.java)
                        intent.putExtra("token", token)

                        startActivity(intent)
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<AccountProfileLookupResponseDto>, t: Throwable) {
                Log.e("error", t.message.toString())
            }
        })
    }
}