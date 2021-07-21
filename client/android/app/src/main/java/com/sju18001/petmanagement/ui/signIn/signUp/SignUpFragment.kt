package com.sju18001.petmanagement.ui.signIn.signUp

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentSignUpBinding
import com.sju18001.petmanagement.restapi.AccountSignUpRequestDto
import com.sju18001.petmanagement.restapi.AccountSignUpResponseDto
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.ui.signIn.SignInViewModel
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

    // variable for storing API call(for cancel)
    private var signUpApiCall: Call<AccountSignUpResponseDto>? = null

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
                // call sign up API
                signUp(signInViewModel.signUpIdEditText, signInViewModel.signUpPwEditText,
                    signInViewModel.signUpEmailEditText, signInViewModel.signUpNameEditText,
                    signInViewModel.signUpPhoneEditText, signInViewModel.signUpMarketingCheckBox,
                    signInViewModel)

                // set sign up button to loading
                setNextButtonToLoading()
            }

            // hide keyboard
            Util().hideKeyboard(requireActivity())
        }

        // hide keyboard when touched outside
        binding.fragmentSignUpLayout.setOnClickListener{ Util().hideKeyboard(requireActivity()) }
    }

    // show previous step button
    public fun showPreviousButton() {
        binding.previousStepButton.visibility = View.VISIBLE
    }

    // hide previous step button
    public fun hidePreviousButton() {
        binding.previousStepButton.visibility = View.INVISIBLE
    }

    // enable next step button
    public fun enableNextButton() {
        binding.nextStepButton.isEnabled = true

        if(childFragmentManager.fragments[0].tag == FRAGMENT_TAG_USER_INFO) {
            binding.nextStepButton.text = getText(R.string.sign_up_button)
        }
        else {
            binding.nextStepButton.text = getText(R.string.next_step_button)
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

    // set next step button to loading
    private fun setNextButtonToLoading() {
        // set sign up button status to loading + disable
        binding.nextStepButton.text = ""
        binding.signUpProgressBar.visibility = View.VISIBLE
        binding.nextStepButton.isEnabled = false

        // hide back button
        binding.previousStepButton.visibility = View.INVISIBLE
    }

    private fun signUp(username: String, password: String, email: String, name: String, phone: String, marketing: Boolean,
                       signInViewModel: SignInViewModel) {
        // create sign up request Dto
        val accountSignUpRequestDto = AccountSignUpRequestDto(username, password, email, name, phone, null, marketing, null)

        // call API using Retrofit
        signUpApiCall = RetrofitBuilder.getServerApi().signUpRequest(accountSignUpRequestDto)
        signUpApiCall!!.enqueue(object: Callback<AccountSignUpResponseDto> {
            override fun onResponse(
                call: Call<AccountSignUpResponseDto>,
                response: Response<AccountSignUpResponseDto>
            ) {
                if(response.isSuccessful) {
                    // return to previous fragment + send sign up result data
                    returnToPreviousFragment(true, null)
                }
                else {
                    // if id overlap -> go to id/pw fragment + show message
                    signInViewModel.signUpIdIsOverlap = true
                    childFragmentManager.popBackStack()
                }
            }

            override fun onFailure(call: Call<AccountSignUpResponseDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> do nothing
                if(_binding == null) { return }

                // return to previous fragment + send sign up result data
                returnToPreviousFragment(false, t.message.toString())

                // log error message
                Log.d("error", t.message.toString())
            }
        })
    }

    // return to previous fragment + send sign up result data
    private fun returnToPreviousFragment(result: Boolean, message: String?) {
        // set result
        setFragmentResult("signUpResult", bundleOf("isSuccessful" to mutableListOf(result, message)))

        // return to previous fragment
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // stop api call when fragment is destroyed
        signUpApiCall?.cancel()
    }
}