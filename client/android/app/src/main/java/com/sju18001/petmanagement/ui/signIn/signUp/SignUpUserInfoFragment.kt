package com.sju18001.petmanagement.ui.signIn.signUp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentSignUpUserInfoBinding
import com.sju18001.petmanagement.restapi.*
import com.sju18001.petmanagement.ui.signIn.SignInViewModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class SignUpUserInfoFragment : Fragment() {

    // pattern regex for EditTexts
    private val patternPhone: Pattern = Pattern.compile("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$")
    private val patternEmail: Pattern = Patterns.EMAIL_ADDRESS

    // variables for view binding
    private var _binding: FragmentSignUpUserInfoBinding? = null
    private val binding get() = _binding!!

    // variable for storing API call(for cancel)
    private var codeRequestApiCall: Call<SendAuthCodeResponseDto>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentSignUpUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // variable for ViewModel
        val signInViewModel: SignInViewModel by activityViewModels()

        // for state restore(ViewModel)
        restoreState(signInViewModel)

        // for phone text change listener
        binding.phoneEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternPhone.matcher(s).matches()) {
                    signInViewModel.signUpPhoneValid = true
                    binding.phoneMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.signUpPhoneValid = false
                    binding.phoneMessage.visibility = View.VISIBLE
                }
                signInViewModel.signUpPhoneEditText = s.toString()
                signInViewModel.signUpPhoneIsOverlap = false
                binding.phoneMessageOverlap.visibility = View.GONE
                checkIsValid(signInViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for email text change listener
        binding.emailEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternEmail.matcher(s).matches()) {
                    signInViewModel.signUpEmailValid = true
                    binding.emailMessage.visibility = View.GONE
                    binding.requestEmailCodeButton.isEnabled = true
                }
                else {
                    signInViewModel.signUpEmailValid = false
                    binding.emailMessage.visibility = View.VISIBLE
                    binding.requestEmailCodeButton.isEnabled = false
                }
                signInViewModel.signUpEmailEditText = s.toString()
                signInViewModel.signUpEmailIsOverlap = false
                binding.emailMessageOverlap.visibility = View.GONE
                checkIsValid(signInViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for request email code button
        binding.requestEmailCodeButton.setOnClickListener {
            // set current code requested email
            signInViewModel.currentCodeRequestedEmail = signInViewModel.signUpEmailEditText

            // set request code button to loading
            setRequestCodeButtonToLoading()

            // API call
            requestEmailCode(signInViewModel)
        }
    }

    // set request code button to normal
    private fun setRequestCodeButtonToNormal() {
        // set request code button status to normal + enable
        binding.requestEmailCodeButton.text = context?.getText(R.string.request_email_code_button)
        binding.requestEmailCodeProgressBar.visibility = View.GONE
        binding.requestEmailCodeButton.isEnabled = true

        // enable email edit text
        binding.emailEditText.isEnabled = true
    }

    // set request code button to loading
    private fun setRequestCodeButtonToLoading() {
        // set request code button status to loading + disable
        binding.requestEmailCodeButton.text = ""
        binding.requestEmailCodeProgressBar.visibility = View.VISIBLE
        binding.requestEmailCodeButton.isEnabled = false

        // disable email edit text
        binding.emailEditText.isEnabled = false
    }

    private fun checkIsValid(signInViewModel: SignInViewModel) {
        if(signInViewModel.signUpPhoneValid && signInViewModel.signUpEmailValid) {
            (parentFragment as SignUpFragment).enableNextButton()
        }
        else{
            (parentFragment as SignUpFragment).disableNextButton()
        }
    }

    public fun showMessage(signInViewModel: SignInViewModel) {
        if(signInViewModel.signUpPhoneIsOverlap) {
            binding.phoneMessageOverlap.visibility = View.VISIBLE
        }
        if(signInViewModel.signUpEmailIsOverlap) {
            binding.emailMessageOverlap.visibility = View.VISIBLE
        }
    }

    private fun requestEmailCode(signInViewModel: SignInViewModel) {
        val sendAuthCodeRequestDto = SendAuthCodeRequestDto(signInViewModel.signUpEmailEditText)

        codeRequestApiCall = RetrofitBuilder.getServerApi().sendAuthCodeRequest(sendAuthCodeRequestDto)
        codeRequestApiCall!!.enqueue(object: Callback<SendAuthCodeResponseDto> {
            override fun onResponse(
                call: Call<SendAuthCodeResponseDto>,
                response: Response<SendAuthCodeResponseDto>
            ) {
                if(response.isSuccessful) {
                    // if success -> display a toast message
                    Toast.makeText(context, R.string.email_code_sent, Toast.LENGTH_LONG).show()
                }
                else {
                    // get error message
                    val errorMessage = JSONObject(response.errorBody()!!.string().trim()).getString("message")

                    //display error toast message
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }

                // set request code button to normal
                setRequestCodeButtonToNormal()

                // reset codeRequestApiCall variable
                codeRequestApiCall = null
            }

            override fun onFailure(call: Call<SendAuthCodeResponseDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> do nothing
                if(_binding == null) { return }

                //display error toast message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()

                // log error message
                Log.d("error", t.message.toString())

                // set request code button to normal
                setRequestCodeButtonToNormal()

                // reset codeRequestApiCall variable
                codeRequestApiCall = null
            }
        })
    }

    private fun restoreState(signInViewModel: SignInViewModel) {
        if(binding.phoneEditText.text.toString() != signInViewModel.signUpPhoneEditText) {
            binding.phoneEditText.setText(signInViewModel.signUpPhoneEditText)
        }
        if(binding.emailEditText.text.toString() != signInViewModel.signUpEmailEditText) {
            binding.emailEditText.setText(signInViewModel.signUpEmailEditText)
        }

        if(!signInViewModel.signUpPhoneValid && signInViewModel.signUpPhoneEditText != "") {
            binding.phoneMessage.visibility = View.VISIBLE
        }
        if(!signInViewModel.signUpEmailValid && signInViewModel.signUpEmailEditText != "") {
            binding.emailMessage.visibility = View.VISIBLE
        }
        if(signInViewModel.signUpPhoneIsOverlap) {
            binding.phoneMessageOverlap.visibility = View.VISIBLE
        }
        if(signInViewModel.signUpEmailIsOverlap) {
            binding.emailMessageOverlap.visibility = View.VISIBLE
        }
        if(signInViewModel.signUpEmailValid && codeRequestApiCall == null) {
            binding.requestEmailCodeButton.isEnabled = true
        }

        (parentFragment as SignUpFragment).showPreviousButton()
        checkIsValid(signInViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // stop api call when fragment is destroyed
        codeRequestApiCall?.cancel()
    }
}