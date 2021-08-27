package com.sju18001.petmanagement.ui.welcomePage

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sju18001.petmanagement.MainActivity
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentWelcomePageProfileBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.FetchAccountPhotoReqDto
import com.sju18001.petmanagement.restapi.dto.FetchAccountResDto
import com.sju18001.petmanagement.ui.myPage.MyPageActivity
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WelcomePageProfileFragment : Fragment() {
    private var _binding: FragmentWelcomePageProfileBinding? = null
    private val binding get() = _binding!!


    // session manager for user token
    private lateinit var sessionManager: SessionManager

    private var isViewDestroyed = false

    // Account DAO
    private var accountData: Account? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWelcomePageProfileBinding.inflate(layoutInflater)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // 수락 버튼
        binding.acceptButton.setOnClickListener{
            if(accountData != null){
                val accountLookupIntent = Intent(context, MyPageActivity::class.java)
                accountLookupIntent.putExtra("fragmentType", "account_edit")
                accountLookupIntent.putExtra("id", accountData!!.id)
                accountLookupIntent.putExtra("username", accountData!!.username)
                accountLookupIntent.putExtra("email", accountData!!.email)
                accountLookupIntent.putExtra("phone", accountData!!.phone)
                accountLookupIntent.putExtra("marketing", accountData!!.marketing)
                accountLookupIntent.putExtra("nickname", accountData!!.nickname)
                accountLookupIntent.putExtra("userMessage", accountData!!.userMessage)

                startActivity(accountLookupIntent)
                requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }else{
                Toast.makeText(requireContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show()
            }
        }
        
        // 거절 버튼
        binding.declineButton.setOnClickListener{
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onResume() {
        super.onResume()

        fetchAccountProfileData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
    }

    // fetch account profile
    private fun fetchAccountProfileData() {
        // create empty body
        val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")
        val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchAccountReq(body)
        call!!.enqueue(object : Callback<FetchAccountResDto> {
            override fun onResponse(
                call: Call<FetchAccountResDto>,
                response: Response<FetchAccountResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful) {
                    response.body()?.let {
                        accountData = Account(
                            it.id!!,
                            it.username!!,
                            it.email!!,
                            it.phone!!,
                            null,
                            it.marketing,
                            it.nickname,
                            it.photoUrl,
                            it.userMessage)
                    }
                }
                else {
                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchAccountResDto>, t: Throwable) {
                if(isViewDestroyed) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }
}