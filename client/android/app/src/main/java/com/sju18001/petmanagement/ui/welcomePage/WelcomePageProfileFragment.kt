package com.sju18001.petmanagement.ui.welcomePage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sju18001.petmanagement.MainActivity
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentWelcomePageProfileBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.FetchAccountPhotoReqDto
import com.sju18001.petmanagement.ui.setting.SettingActivity

class WelcomePageProfileFragment : Fragment() {
    private var _binding: FragmentWelcomePageProfileBinding? = null
    private val binding get() = _binding!!

    private var isViewDestroyed = false

    private var accountData: Account? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWelcomePageProfileBinding.inflate(layoutInflater)
        isViewDestroyed = false

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // 수락 버튼
        binding.acceptButton.setOnClickListener{
            if(accountData != null){
                val accountLookupIntent = Intent(context, SettingActivity::class.java)
                accountLookupIntent.putExtra("fragmentType", "update_account")
                accountLookupIntent.putExtra("id", accountData!!.id)
                accountLookupIntent.putExtra("username", accountData!!.username)
                accountLookupIntent.putExtra("email", accountData!!.email)
                accountLookupIntent.putExtra("phone", accountData!!.phone)
                accountLookupIntent.putExtra("marketing", accountData!!.marketing)
                accountLookupIntent.putExtra("nickname", accountData!!.nickname)
                accountLookupIntent.putExtra("userMessage", accountData!!.userMessage)
                
                fetchAccountPhotoAndStartAccountLookupActivity(accountLookupIntent)
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

        accountData = SessionManager.fetchLoggedInAccount(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
    }

    private fun fetchAccountPhotoAndStartAccountLookupActivity(accountLookupIntent: Intent) {
        // 사진이 없을 때 -> 사진 업데이트 없이 바로 시작
        if(accountData!!.photoUrl == null){
            startActivity(accountLookupIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)

            return
        }

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchAccountPhotoReq(FetchAccountPhotoReqDto(null))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            accountLookupIntent.putExtra("photoByteArray", response.body()!!.byteStream().readBytes())

            startActivity(accountLookupIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }, {}, {})
    }
}