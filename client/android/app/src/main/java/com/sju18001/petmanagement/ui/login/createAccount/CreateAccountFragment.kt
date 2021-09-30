package com.sju18001.petmanagement.ui.login.createAccount

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCreateAccountBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.login.LoginViewModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateAccountFragment : Fragment() {

    // variable for back press callback
    private lateinit var callback: OnBackPressedCallback

    // variables for view binding
    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    private var isViewDestroyed = false

    // const variables for fragment tags
    private val FRAGMENT_TAG_TERMS: String = "terms"
    private val FRAGMENT_TAG_USERNAME_PASSWORD: String = "username_password"
    private val FRAGMENT_TAG_USER_INFO: String = "user_info"

    // const variables for error message
    private val MESSAGE_USERNAME_OVERLAP: String = "Username already exists"
    private val MESSAGE_PHONE_OVERLAP: String = "Phone number already exists"
    private val MESSAGE_EMAIL_OVERLAP: String = "Email already exists"

    // variables for storing API call
    private var createAccountApiCall: Call<CreateAccountResDto>? = null
    private var verifyAuthCodeApiCall: Call<VerifyAuthCodeResDto>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // open dialog on back press
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val alertBuilder = AlertDialog.Builder(context)
                alertBuilder.setMessage(R.string.return_to_login_dialog)
                alertBuilder.setPositiveButton(R.string.confirm){ _, _->
                    activity?.supportFragmentManager?.popBackStack()
                }
                alertBuilder.setNegativeButton(R.string.cancel){ _, _-> }
                alertBuilder.create().show()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // variable for ViewModel
        val loginViewModel: LoginViewModel by activityViewModels()

        // for close button
        binding.backButton.setOnClickListener {
            val alertBuilder = AlertDialog.Builder(context)
            alertBuilder.setMessage(R.string.return_to_login_dialog)
            alertBuilder.setPositiveButton(R.string.confirm){ _, _->
                activity?.supportFragmentManager?.popBackStack()
            }
            alertBuilder.setNegativeButton(R.string.cancel){ _, _-> }
            alertBuilder.create().show()
        }

        // for child fragment(open terms fragment)
        if(childFragmentManager.findFragmentById(R.id.child_fragment_container) == null) {
            val createAccountTermsFragment = CreateAccountTermsFragment()
            childFragmentManager.beginTransaction()
                .add(R.id.child_fragment_container, createAccountTermsFragment, FRAGMENT_TAG_TERMS)
                .commit()
        }

        // for previous step button
        binding.previousStepButton.setOnClickListener {
            childFragmentManager.popBackStack()
        }

        // for next step button
        binding.nextStepButton.setOnClickListener {
            var nextFragment: Fragment? = null
            var nextFragmentTag: String? = null

            if(childFragmentManager.fragments[0].tag != FRAGMENT_TAG_USER_INFO) {
                if(childFragmentManager.fragments[0].tag == FRAGMENT_TAG_TERMS) {
                    nextFragment = CreateAccountCredentialsFragment()
                    nextFragmentTag = FRAGMENT_TAG_USERNAME_PASSWORD
                }
                else if(childFragmentManager.fragments[0].tag == FRAGMENT_TAG_USERNAME_PASSWORD) {
                    nextFragment = CreateAccountUserInfoFragment()
                    nextFragmentTag = FRAGMENT_TAG_USER_INFO
                }

                childFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.child_fragment_container, nextFragment!!, nextFragmentTag)
                    .addToBackStack(null)
                    .commit()
            }
            else {
                // set create account button to loading
                setNextButtonToLoading()

                // check if inputted email is the same as code requested email
                if(loginViewModel.createAccountEmailEditText == loginViewModel.currentCodeRequestedEmail) {
                    // validate email code(+ create account)
                    validateEmailCode(loginViewModel)
                }
                else {
                    loginViewModel.showsEmailRequestMessage = true
                    (childFragmentManager.findFragmentByTag(FRAGMENT_TAG_USER_INFO) as CreateAccountUserInfoFragment)
                        .showHideRequestMessage(loginViewModel)

                    // set next button to normal
                    setNextButtonToNormal()
                }
            }
        }

        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentCreateAccountParentLayout)
    }

    public fun showPreviousButton() {
        if(createAccountApiCall == null && verifyAuthCodeApiCall == null) {
            binding.previousStepButton.visibility = View.VISIBLE
        }
    }

    public fun hidePreviousButton() {
        binding.previousStepButton.visibility = View.INVISIBLE
    }

    public fun enableNextButton() {
        if(createAccountApiCall == null && verifyAuthCodeApiCall == null) {
            binding.nextStepButton.isEnabled = true

            if(childFragmentManager.fragments[0].tag == FRAGMENT_TAG_USER_INFO) {
                binding.nextStepButton.text = getText(R.string.create_account_button)
            }
            else {
                binding.nextStepButton.text = getText(R.string.next_step_button)
            }
        }
    }

    public fun disableNextButton() {
        binding.nextStepButton.isEnabled = false

        if(childFragmentManager.fragments[0].tag == FRAGMENT_TAG_USER_INFO) {
            binding.nextStepButton.text = getText(R.string.create_account_button)
        }
        else {
            binding.nextStepButton.text = getText(R.string.next_step_button)
        }
    }

    private fun setNextButtonToNormal() {
        // set create account button status to normal + enable
        binding.nextStepButton.text = context?.getText(R.string.create_account_button)
        binding.createAccountProgressBar.visibility = View.GONE
        binding.nextStepButton.isEnabled = true

        // hide back button
        binding.previousStepButton.visibility = View.VISIBLE
    }

    private fun setNextButtonToLoading() {
        // set create account button status to loading + disable
        binding.nextStepButton.text = ""
        binding.createAccountProgressBar.visibility = View.VISIBLE
        binding.nextStepButton.isEnabled = false

        // hide back button
        binding.previousStepButton.visibility = View.INVISIBLE
    }

    private fun createAccount(loginViewModel: LoginViewModel) {
        // create create account request Dto
        val accountCreateAccountRequestDto = CreateAccountReqDto(loginViewModel.createAccountUsernameEditText,
            loginViewModel.createAccountPwEditText, loginViewModel.createAccountEmailEditText, loginViewModel.createAccountPhoneEditText,
            "#", loginViewModel.createAccountMarketingCheckBox, null)

        // call API using Retrofit
        createAccountApiCall = RetrofitBuilder.getServerApi().createAccountReq(accountCreateAccountRequestDto)

        createAccountApiCall!!.enqueue(object: Callback<CreateAccountResDto> {
            override fun onResponse(
                call: Call<CreateAccountResDto>,
                response: Response<CreateAccountResDto>
            ) {
                if(isViewDestroyed) return

                if(response.isSuccessful) {
                    returnToPreviousFragment()
                }
                else {
                        // enable inputs
                    (childFragmentManager.findFragmentByTag(FRAGMENT_TAG_USER_INFO) as CreateAccountUserInfoFragment)
                        .unlockViews()
                    binding.backButton.isEnabled = true

                    when (Util.getMessageFromErrorBody(response.errorBody()!!)) {
                        // if email overlap + show message
                        MESSAGE_EMAIL_OVERLAP -> {
                            loginViewModel.createAccountEmailIsOverlap = true

                            // set email code valid to false + reset chronometer/requested email
                            loginViewModel.emailCodeValid = false
                            loginViewModel.emailCodeChronometerBase = 0
                            loginViewModel.currentCodeRequestedEmail = ""

                            // show message
                            (childFragmentManager.findFragmentByTag(FRAGMENT_TAG_USER_INFO) as CreateAccountUserInfoFragment)
                                .showOverlapMessage(loginViewModel)
                            setNextButtonToNormal()
                        }
                        // if username overlap -> go to credentials fragment + show message
                        MESSAGE_USERNAME_OVERLAP -> {
                            loginViewModel.createAccountUsernameIsOverlap = true
                            childFragmentManager.popBackStack()
                        }
                        // if phone overlap + show message
                        MESSAGE_PHONE_OVERLAP -> {
                            loginViewModel.createAccountPhoneIsOverlap = true
                            (childFragmentManager.findFragmentByTag(FRAGMENT_TAG_USER_INFO) as CreateAccountUserInfoFragment)
                                .showOverlapMessage(loginViewModel)
                            setNextButtonToNormal()
                        }
                        else -> {
                            Toast.makeText(requireContext(), context?.getText(R.string.fail_request), Toast.LENGTH_SHORT).show()
                            setNextButtonToNormal()
                            (childFragmentManager.findFragmentByTag(FRAGMENT_TAG_USER_INFO) as CreateAccountUserInfoFragment)
                                .unlockViews()
                            binding.backButton.isEnabled = true
                        }
                    }
                }

                // reset createAccountApiCall variable
                createAccountApiCall = null
            }

            override fun onFailure(call: Call<CreateAccountResDto>, t: Throwable) {
                if(isViewDestroyed) return

                setNextButtonToNormal()

                // reset createAccountApiCall variable
                createAccountApiCall = null

                Util.showToastAndLog(requireContext(), t.message.toString())
            }
        })
    }

    private fun validateEmailCode(loginViewModel: LoginViewModel) {
        // lock views
        (childFragmentManager.findFragmentByTag(FRAGMENT_TAG_USER_INFO) as CreateAccountUserInfoFragment)
            .lockViews()
        binding.backButton.isEnabled = false

        // create verify code request DTO
        val verifyAuthCodeRequestDto = VerifyAuthCodeReqDto(loginViewModel.currentCodeRequestedEmail,
            loginViewModel.createAccountEmailCodeEditText)

        // if not yet verified
        if(!loginViewModel.emailCodeValid) {
            // call API using Retrofit
            verifyAuthCodeApiCall = RetrofitBuilder.getServerApi().verifyAuthCodeReq(verifyAuthCodeRequestDto)
            verifyAuthCodeApiCall!!.enqueue(object: Callback<VerifyAuthCodeResDto> {
                override fun onResponse(
                    call: Call<VerifyAuthCodeResDto>,
                    response: Response<VerifyAuthCodeResDto>
                ) {
                    if(isViewDestroyed) return

                    if(response.isSuccessful) {
                        // set email code valid to true
                        loginViewModel.emailCodeValid = true


                        // call create account API if the code is valid
                        createAccount(loginViewModel)
                    }
                    else {
                        setNextButtonToNormal()

                        Util.showToastAndLog(requireContext(), requireContext().getString(R.string.email_message_code_invalid))
                    }

                    // reset verifyAuthCodeApiCall variable
                    verifyAuthCodeApiCall = null
                }

                override fun onFailure(call: Call<VerifyAuthCodeResDto>, t: Throwable) {
                    if(isViewDestroyed) return

                    setNextButtonToNormal()

                    // reset verifyAuthCodeApiCall variable
                    verifyAuthCodeApiCall = null

                    Util.showToastAndLog(requireContext(), t.message.toString())
                }
            })
        }
        else {
            // call create account API if already verified
            createAccount(loginViewModel)
        }
    }

    // return to previous fragment
    private fun returnToPreviousFragment() {
        // set result
        setFragmentResult("createAccountResult", bundleOf("isSuccessful" to true))

        // return to previous fragment
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
    }
}
