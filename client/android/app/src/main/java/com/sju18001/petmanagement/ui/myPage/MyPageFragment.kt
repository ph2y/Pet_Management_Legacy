package com.sju18001.petmanagement.ui.myPage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentMyPageBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.FetchAccountResDto
import com.sju18001.petmanagement.ui.myPage.account.EditAccountFragment
import com.sju18001.petmanagement.ui.myPage.preferences.PreferencesFragment
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Account API
    private var fetchAccountApiCall: Call<FetchAccountResDto>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    // Account DAO
    private lateinit var accountData: Account

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        return root
    }

    override fun onStart() {
        super.onStart()

        binding.accountLookup.setOnClickListener {
            val accountLookupIntent = Intent(context, MyPageActivity::class.java)
            accountLookupIntent.putExtra("fragmentType", "account_edit")
            accountLookupIntent.putExtra("id", accountData.id)
            accountLookupIntent.putExtra("username", accountData.username)
            accountLookupIntent.putExtra("email", accountData.email)
            accountLookupIntent.putExtra("phone", accountData.phone)
            accountLookupIntent.putExtra("marketing", accountData.marketing)
            accountLookupIntent.putExtra("nickname", accountData.nickname)
            accountLookupIntent.putExtra("photoUrl", accountData.photoUrl)
            accountLookupIntent.putExtra("userMessage", accountData.userMessage)

            startActivity(accountLookupIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        binding.preferencesLookup.setOnClickListener {
            val preferencesLookupIntent = Intent(context, MyPageActivity::class.java)
            preferencesLookupIntent.putExtra("fragmentType", "preferences")
            startActivity(preferencesLookupIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        binding.notificationLookup.setOnClickListener {
            val notificationPreferencesIntent = Intent(context, MyPageActivity::class.java)
            notificationPreferencesIntent.putExtra("fragmentType", "notification_preferences")
            startActivity(notificationPreferencesIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        binding.themeLookup.setOnClickListener {
            val themePreferencesIntent = Intent(context, MyPageActivity::class.java)
            themePreferencesIntent.putExtra("fragmentType", "theme_preferences")
            startActivity(themePreferencesIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    override fun onResume() {
        super.onResume()

        val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

        fetchAccountApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchAccountReq(body)
        fetchAccountApiCall!!.enqueue(object : Callback<FetchAccountResDto> {
            override fun onResponse(
                call: Call<FetchAccountResDto>,
                response: Response<FetchAccountResDto>
            ) {
                if(response.isSuccessful) {
                    response.body()?.let {
                        accountData = Account(
                            it.id!!,
                            it.username!!,
                            it.email!!,
                            it.phone!!,
                            it.marketing,
                            it.nickname,
                            it.photoUrl,
                            it.userMessage)
                    }

                    // for test
                    binding.nicknameText.text = accountData.nickname
                }
                else {

                }
            }

            override fun onFailure(call: Call<FetchAccountResDto>, t: Throwable) {
                TODO("Not yet implemented")
            }


        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}