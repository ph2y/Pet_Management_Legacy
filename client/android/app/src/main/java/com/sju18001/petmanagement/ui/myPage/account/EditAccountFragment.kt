package com.sju18001.petmanagement.ui.myPage.account

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentAccountEditBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.DeleteAccountResDto
import com.sju18001.petmanagement.restapi.dto.UpdateAccountReqDto
import com.sju18001.petmanagement.restapi.dto.UpdateAccountResDto
import com.sju18001.petmanagement.ui.login.LoginActivity
import com.sju18001.petmanagement.ui.myPage.MyPageViewModel
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditAccountFragment : Fragment() {
    private lateinit var editAccountViewModel: EditAccountViewModel
    private var _binding: FragmentAccountEditBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // variable for ViewModel
    private val myPageViewModel: MyPageViewModel by activityViewModels()

    // variables for storing API call(for cancel)
    private var updateAccountApiCall: Call<UpdateAccountResDto>? = null
    private var deleteAccountApiCall: Call<DeleteAccountResDto>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        editAccountViewModel =
            ViewModelProvider(this).get(EditAccountViewModel::class.java)

        _binding = FragmentAccountEditBinding.inflate(inflater, container, false)
        val root: View = binding.root

        (activity as AppCompatActivity)!!.supportActionBar!!.hide()

        // save account data to ViewModel(for account profile) if not already loaded
        if(!myPageViewModel.loadedFromIntent) { saveAccountDataForAccountProfile() }

        return root
    }

    override fun onStart() {
        super.onStart()

        // for view restore
        restoreState()

        binding.backButton.setOnClickListener {
            activity?.finish()
        }

        binding.confirmButton.setOnClickListener {
            updateAccount()
        }

        binding.signOutButton.setOnClickListener {
            signOut()
        }

        binding.withdrawalAccountButton.setOnClickListener {
            withdrawalAccount()
        }

        binding.marketingSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                Toast.makeText(context, context?.getText(R.string.marketing_agree), Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, context?.getText(R.string.marketing_decline), Toast.LENGTH_SHORT).show()
            }
        }

        // for EditText text change listeners
        binding.nicknameEdit.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myPageViewModel.accountNicknameValue = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.phoneEdit.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myPageViewModel.accountPhoneValue = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.passwordEdit.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myPageViewModel.accountPasswordValue = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.emailEdit.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myPageViewModel.accountEmailValue = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()

        // set views with data from ViewModel
        setViewsWithAccountProfileData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        updateAccountApiCall?.cancel()
        deleteAccountApiCall?.cancel()
    }

    // update account
    private fun updateAccount() {
        // create dto
        val updateAccountReqDto = UpdateAccountReqDto(
            myPageViewModel.accountEmailValue,
            myPageViewModel.accountPhoneValue,
            myPageViewModel.accountNicknameValue,
            myPageViewModel.accountMarketingValue,
            myPageViewModel.accountUserMessageValue
        )

        updateAccountApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .updateAccountReq(updateAccountReqDto)
        updateAccountApiCall!!.enqueue(object: Callback<UpdateAccountResDto> {
            override fun onResponse(
                call: Call<UpdateAccountResDto>,
                response: Response<UpdateAccountResDto>
            ) {
                if(response.isSuccessful) {
                    if(response.body()?._metadata?.status == true) {
                        closeAfterSuccess()
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

            override fun onFailure(call: Call<UpdateAccountResDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> return
                if(_binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun closeAfterSuccess() {
        Toast.makeText(context, context?.getText(R.string.account_update_success), Toast.LENGTH_LONG).show()
        activity?.finish()
    }

    private fun signOut() {
        // remove user token in sessionManager
        sessionManager.removeUserToken()

        // go back to login activity
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        startActivity(intent)
    }

    private fun withdrawalAccount() {
        val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

        deleteAccountApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .deleteAccountReq(body)
        deleteAccountApiCall!!.enqueue(object: Callback<DeleteAccountResDto> {
            override fun onResponse(
                call: Call<DeleteAccountResDto>,
                response: Response<DeleteAccountResDto>
            ) {
                if(response.isSuccessful) {
                    if(response.body()?._metadata?.status == true) {
                        Toast.makeText(context, context?.getText(R.string.account_delete_success), Toast.LENGTH_LONG).show()
                        signOut()
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

            override fun onFailure(call: Call<DeleteAccountResDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> return
                if(_binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun saveAccountDataForAccountProfile() {
        myPageViewModel.loadedFromIntent = true
        myPageViewModel.accountEmailValue = requireActivity().intent.getStringExtra("email").toString()
        myPageViewModel.accountPhoneValue = requireActivity().intent.getStringExtra("phone").toString()
        myPageViewModel.accountMarketingValue = requireActivity().intent.getBooleanExtra("marketing", false)
        myPageViewModel.accountNicknameValue = requireActivity().intent.getStringExtra("nickname").toString()
        myPageViewModel.accountUserMessageValue = requireActivity().intent.getStringExtra("userMessage").toString()
        myPageViewModel.accountPhotoByteArray = requireActivity().intent.getByteArrayExtra("photoByteArray")
    }

    private fun setViewsWithAccountProfileData() {
        if(myPageViewModel.accountPhotoByteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(myPageViewModel.accountPhotoByteArray, 0, myPageViewModel.accountPhotoByteArray!!.size)
            binding.accountPhoto.setImageBitmap(bitmap)
        }
        
        binding.nicknameEdit.setText(myPageViewModel.accountNicknameValue)
        binding.emailEdit.setText(myPageViewModel.accountEmailValue)
        binding.phoneEdit.setText(myPageViewModel.accountPhoneValue)
        binding.marketingSwitch.isChecked = myPageViewModel.accountMarketingValue!!
    }

    private fun restoreState() {
        setViewsWithAccountProfileData()
    }
}