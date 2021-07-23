package com.sju18001.petmanagement.ui.signIn.signUp

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
import com.google.gson.Gson
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentSignUpBinding
import com.sju18001.petmanagement.restapi.*
import com.sju18001.petmanagement.ui.signIn.SignInViewModel
import org.json.JSONObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpFragment : Fragment() {

    // variable for back press callback
    private lateinit var callback: OnBackPressedCallback

    // variables for view binding
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    // const variables for fragment tags
    private val FRAGMENT_TAG_TERMS: String = "terms"
    private val FRAGMENT_TAG_ID_PW: String = "id_pw"
    private val FRAGMENT_TAG_USER_INFO: String = "user_info"

    // const variables for error message
    private val MESSAGE_ID_OVERLAP: String = "Username already exists"
    private val MESSAGE_PHONE_OVERLAP: String = "Phone number already exists"
    private val MESSAGE_EMAIL_OVERLAP: String = "Email already exists"

    // variables for storing API call(for cancel)
    private var signUpApiCall: Call<AccountSignUpResponseDto>? = null
    private var verifyAuthCodeApiCall: Call<VerifyAuthCodeResponseDto>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // open dialog on back press
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val alertBuilder = AlertDialog.Builder(context)
                alertBuilder.setMessage(R.string.return_to_sign_in_dialog)
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
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // variable for ViewModel
        val signInViewModel: SignInViewModel by activityViewModels()

        // for close button
        binding.backButton.setOnClickListener {
            val alertBuilder = AlertDialog.Builder(context)
            alertBuilder.setMessage(R.string.return_to_sign_in_dialog)
            alertBuilder.setPositiveButton(R.string.confirm){ _, _->
                activity?.supportFragmentManager?.popBackStack()
            }
            alertBuilder.setNegativeButton(R.string.cancel){ _, _-> }
            alertBuilder.create().show()
        }

        // for child fragment(open terms fragment)
        if(childFragmentManager.findFragmentById(R.id.child_fragment_container) == null) {
            val signUpTermsFragment = SignUpTermsFragment()
            childFragmentManager.beginTransaction()
                .add(R.id.child_fragment_container, signUpTermsFragment, FRAGMENT_TAG_TERMS)
                .commit()
        }

        // for previous step button
        binding.previousStepButton.setOnClickListener {
            childFragmentManager.popBackStack()
        }

        //for next step button
        binding.nextStepButton.setOnClickListener {
            var nextFragment: Fragment? = null
            var nextFragmentTag: String? = null

            if(childFragmentManager.fragments[0].tag != FRAGMENT_TAG_USER_INFO) {
                if(childFragmentManager.fragments[0].tag == FRAGMENT_TAG_TERMS) {
                    nextFragment = SignUpIdPwFragment()
                    nextFragmentTag = FRAGMENT_TAG_ID_PW
                }
                else if(childFragmentManager.fragments[0].tag == FRAGMENT_TAG_ID_PW) {
                    nextFragment = SignUpUserInfoFragment()
                    nextFragmentTag = FRAGMENT_TAG_USER_INFO
                }

                childFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.child_fragment_container, nextFragment!!, nextFragmentTag)
                    .addToBackStack(null)
                    .commit()
            }
            else {
                // set sign up button to loading
                setNextButtonToLoading()

                // check if inputted email is the same as code requested email
                if(signInViewModel.signUpEmailEditText == signInViewModel.currentCodeRequestedEmail) {
                    // validate email code(+ sign up)
                    validateEmailCode(signInViewModel)
                }
                else {
                    signInViewModel.showsEmailRequestMessage = true
                    (childFragmentManager.findFragmentByTag(FRAGMENT_TAG_USER_INFO) as SignUpUserInfoFragment)
                        .showHideRequestMessage(signInViewModel)

                    // set next button to normal
                    setNextButtonToNormal()
                }
            }

            // hide keyboard
            Util().hideKeyboard(requireActivity())
        }

        // hide keyboard when touched outside
        binding.fragmentSignUpLayout.setOnClickListener{ Util().hideKeyboard(requireActivity()) }
    }

    // show previous step button
    public fun showPreviousButton() {
        if(signUpApiCall == null && verifyAuthCodeApiCall == null) {
            binding.previousStepButton.visibility = View.VISIBLE
        }
    }

    // hide previous step button
    public fun hidePreviousButton() {
        binding.previousStepButton.visibility = View.INVISIBLE
    }

    // enable next step button
    public fun enableNextButton() {
        if(signUpApiCall == null && verifyAuthCodeApiCall == null) {
            binding.nextStepButton.isEnabled = true

            if(childFragmentManager.fragments[0].tag == FRAGMENT_TAG_USER_INFO) {
                binding.nextStepButton.text = getText(R.string.sign_up_button)
            }
            else {
                binding.nextStepButton.text = getText(R.string.next_step_button)
            }
        }
    }

    // disable next step button
    public fun disableNextButton() {
        binding.nextStepButton.isEnabled = false

        if(childFragmentManager.fragments[0].tag == FRAGMENT_TAG_USER_INFO) {
            binding.nextStepButton.text = getText(R.string.sign_up_button)
        }
        else {
            binding.nextStepButton.text = getText(R.string.next_step_button)
        }
    }

    // set next step button to normal
    private fun setNextButtonToNormal() {
        // set sign up button status to normal + enable
        binding.nextStepButton.text = context?.getText(R.string.sign_up_button)
        binding.signUpProgressBar.visibility = View.GONE
        binding.nextStepButton.isEnabled = true

        // hide back button
        binding.previousStepButton.visibility = View.VISIBLE
    }

    // set next step button to loading
    private fun setNextButtonToLoading() {
        // set sign up button status to loading + disable
        binding.nextStepButton.text = ""
        binding.signUpProgressBar.visibility = View.VISIBLE
        binding.nextStepButton.isEnabled = false

        // hide back button
        binding.previousStepButton.visibility = View.INVISIBLE
    }

    private fun signUp(signInViewModel: SignInViewModel) {
        // create sign up request Dto
        val accountSignUpRequestDto = AccountSignUpRequestDto(signInViewModel.signUpIdEditText,
            signInViewModel.signUpPwEditText, signInViewModel.signUpEmailEditText, null,
            signInViewModel.signUpPhoneEditText, null, signInViewModel.signUpMarketingCheckBox, null)

        // call API using Retrofit
        signUpApiCall = RetrofitBuilder.getServerApi().signUpRequest(accountSignUpRequestDto)
        signUpApiCall!!.enqueue(object: Callback<AccountSignUpResponseDto> {
            override fun onResponse(
                call: Call<AccountSignUpResponseDto>,
                response: Response<AccountSignUpResponseDto>
            ) {
                if(response.isSuccessful) {
                    // return to previous fragment
                    returnToPreviousFragment()
                }
                else {
                    // get error message(overlap)
                    val errorMessage = JSONObject(response.errorBody()!!.string().trim()).getString("message")

                    // if email overlap + show message
                    if(errorMessage == MESSAGE_EMAIL_OVERLAP) {
                        signInViewModel.signUpEmailIsOverlap = true

                        // set email code valid to false + reset chronometer/requested email + unlock email views
                        signInViewModel.emailCodeValid = false
                        signInViewModel.emailCodeChronometerBase = 0
                        signInViewModel.currentCodeRequestedEmail = ""
                        (childFragmentManager.findFragmentByTag(FRAGMENT_TAG_USER_INFO) as SignUpUserInfoFragment)
                            .unlockEmailViews()

                        // show message
                        (childFragmentManager.findFragmentByTag(FRAGMENT_TAG_USER_INFO) as SignUpUserInfoFragment)
                            .showOverlapMessage(signInViewModel)
                        setNextButtonToNormal()
                    }
                    // if id overlap -> go to id/pw fragment + show message
                    else if(errorMessage == MESSAGE_ID_OVERLAP) {
                        signInViewModel.signUpIdIsOverlap = true
                        childFragmentManager.popBackStack()
                    }
                    // if phone overlap + show message
                    else if(errorMessage == MESSAGE_PHONE_OVERLAP) {
                        signInViewModel.signUpPhoneIsOverlap = true
                        (childFragmentManager.findFragmentByTag(FRAGMENT_TAG_USER_INFO) as SignUpUserInfoFragment)
                            .showOverlapMessage(signInViewModel)
                        setNextButtonToNormal()
                    }
                }

                // reset signUpApiCall variable
                signUpApiCall = null
            }

            override fun onFailure(call: Call<AccountSignUpResponseDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> do nothing
                if(_binding == null) { return }

                //display error toast message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()

                // log error message
                Log.d("error", t.message.toString())

                // set next button to normal
                setNextButtonToNormal()

                // reset signUpApiCall variable
                signUpApiCall = null
            }
        })
    }

    private fun validateEmailCode(signInViewModel: SignInViewModel) {
        // create verify code request DTO
        val verifyAuthCodeRequestDto = VerifyAuthCodeRequestDto(signInViewModel.currentCodeRequestedEmail,
        signInViewModel.signUpEmailCodeEditText)

        // if not yet verified
        if(!signInViewModel.emailCodeValid) {
            // call API using Retrofit
            verifyAuthCodeApiCall = RetrofitBuilder.getServerApi().verifyAuthCodeRequest(verifyAuthCodeRequestDto)
            verifyAuthCodeApiCall!!.enqueue(object: Callback<VerifyAuthCodeResponseDto> {
                override fun onResponse(
                    call: Call<VerifyAuthCodeResponseDto>,
                    response: Response<VerifyAuthCodeResponseDto>
                ) {
                    if(response.isSuccessful) {
                        // set email code valid to true + lock email views
                        signInViewModel.emailCodeValid = true
                        (childFragmentManager.findFragmentByTag(FRAGMENT_TAG_USER_INFO) as SignUpUserInfoFragment)
                            .lockEmailViews()

                        // call sign up API if the code is valid
                        signUp(signInViewModel)
                    }
                    else {
                        // code invalid Toast message
                        Toast.makeText(context, context?.getText(R.string.email_message_code_invalid),
                            Toast.LENGTH_LONG).show()

                        // set next button to normal
                        setNextButtonToNormal()
                    }

                    // reset verifyAuthCodeApiCall variable
                    verifyAuthCodeApiCall = null
                }

                override fun onFailure(call: Call<VerifyAuthCodeResponseDto>, t: Throwable) {
                    // if the view was destroyed(API call canceled) -> set result to false + return
                    if(_binding == null) {
                        return
                    }

                    //display error toast message
                    Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", t.message.toString())

                    // set next button to normal
                    setNextButtonToNormal()

                    // reset verifyAuthCodeApiCall variable
                    verifyAuthCodeApiCall = null
                }
            })
        }
        else {
            // call sign up API if already verified
            signUp(signInViewModel)
        }
    }

    // return to previous fragment
    private fun returnToPreviousFragment() {
        // set result
        setFragmentResult("signUpResult", bundleOf("isSuccessful" to true))

        // return to previous fragment
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // stop api calls when fragment is destroyed
        signUpApiCall?.cancel()
        verifyAuthCodeApiCall?.cancel()
    }
}