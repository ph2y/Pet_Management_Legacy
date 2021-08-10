package com.sju18001.petmanagement.ui.myPage.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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

    // variables for storing API call(for cancel)
    private var updateAccountApiCall: Call<UpdateAccountResDto>? = null
    private var deleteAccountApiCall: Call<DeleteAccountResDto>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

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

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        return root
    }

    override fun onStart() {
        super.onStart()

        binding.confirmButton.setOnClickListener {
            updateAccount()
        }

        binding.backButton.setOnClickListener {
            activity?.finish()
        }

        binding.signOutButton.setOnClickListener {
            signOut()
        }

        binding.withdrawalAccountButton.setOnClickListener {
            withdrawalAccount()
        }

        binding.marketingSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {

            }
            else {

            }
        }
    }

    override fun onResume() {
        super.onResume()

        // for test
        val intent = requireActivity().intent
        binding.nicknameEdit.setText(intent.getStringExtra("nickname"))
        binding.emailEdit.setText(intent.getStringExtra("email"))
        binding.phoneEdit.setText(intent.getStringExtra("phone"))
        binding.marketingSwitch.isChecked = intent.getBooleanExtra("marketing", false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // update account
    private fun updateAccount() {
        // for test
        val intent = requireActivity().intent

        // create dto
        val updateAccountReqDto = UpdateAccountReqDto(
            binding.emailEdit.text.toString(),
            binding.phoneEdit.text.toString(),
            binding.nicknameEdit.text.toString(),
            binding.marketingSwitch.isChecked,
            ""
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
}