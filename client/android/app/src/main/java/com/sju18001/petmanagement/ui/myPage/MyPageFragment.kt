package com.sju18001.petmanagement.ui.myPage

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentMyPageBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.FetchAccountPhotoReqDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MyPageFragment : Fragment() {
    private var _binding: FragmentMyPageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // variable for ViewModel
    val myPageViewModel: MyPageViewModel by activityViewModels()

    private var isViewDestroyed = false

    // Account DAO
    private lateinit var accountData: Account

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        return binding.root
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
            accountLookupIntent.putExtra("userMessage", accountData.userMessage)
            accountLookupIntent.putExtra("representativePetId", accountData.representativePetId)

            if(accountData.photoUrl != null) {
                accountLookupIntent.putExtra("photoByteArray", myPageViewModel.accountPhotoProfileByteArray)
            }

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

        binding.deleteTemporaryFilesButton.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(context?.getString(R.string.delete_temporary_files_message))
                .setPositiveButton(
                    R.string.confirm
                ) { _, _ ->
                    // delete temporary files + set size to 0 after deletion
                    File(requireContext().getExternalFilesDir(null).toString()).deleteRecursively()
                    setTemporaryFilesSize()
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, _ ->
                    dialog.cancel()
                }
                .create().show()
        }

        binding.termsAndPoliciesLookup.setOnClickListener {
            val termsAndPoliciesIntent = Intent(context, MyPageActivity::class.java)
            termsAndPoliciesIntent.putExtra("fragmentType", "terms_and_policies")
            startActivity(termsAndPoliciesIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
        binding.licenseLookup.setOnClickListener {
            val licenseIntent = Intent(context, MyPageActivity::class.java)
            licenseIntent.putExtra("fragmentType", "license")
            startActivity(licenseIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    override fun onResume() {
        super.onResume()

        fetchAccountProfileData()
        setTemporaryFilesSize()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
    }

    // fetch account profile
    private fun fetchAccountProfileData() {
        // create empty body
        accountData = SessionManager.fetchLoggedInAccount(requireContext())!!
        myPageViewModel.accountNicknameProfileValue = accountData.nickname!!

        // set views after API response
        setViewsWithAccountProfileData()
    }

    // fetch account photo
    private fun fetchAccountPhotoAndSetView() {
        if(accountData!!.photoUrl == null){
            // 기본 사진으로 세팅
            binding.accountPhoto.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_account_circle_36))
            return
        }
        
        // create DTO
        val fetchAccountPhotoReqDto = FetchAccountPhotoReqDto(null)

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchAccountPhotoReq(fetchAccountPhotoReqDto)
        call.enqueue(object: Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(isViewDestroyed) return

                    if(response.isSuccessful) {
                        // save in ViewModel by byte array
                        myPageViewModel.accountPhotoProfileByteArray = response.body()!!.byteStream().readBytes()
                        binding.accountPhoto.setImageBitmap(BitmapFactory.decodeByteArray(myPageViewModel.accountPhotoProfileByteArray, 0, myPageViewModel.accountPhotoProfileByteArray!!.size))
                    }
                    else {
                        Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    if(isViewDestroyed) return

                    Util.showToastAndLog(requireContext(), t.message.toString())
                }
            })
    }

    // set views for account profile
    private fun setViewsWithAccountProfileData() {
        fetchAccountPhotoAndSetView()
        binding.nicknameText.text = myPageViewModel.accountNicknameProfileValue
    }

    // set temporary files size
    private fun setTemporaryFilesSize() {
        val size = String.format("%.1f", (Util.getTemporaryFilesSize(requireContext()) / 1e6)) + "MB"
        binding.temporaryFilesSize.text = size
    }
}