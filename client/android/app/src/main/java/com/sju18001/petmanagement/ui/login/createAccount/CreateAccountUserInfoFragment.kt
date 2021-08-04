package com.sju18001.petmanagement.ui.login.createAccount

import android.os.Bundle
import android.os.SystemClock
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
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentCreateAccountUserInfoBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.dto.SendAuthCodeReqDto
import com.sju18001.petmanagement.restapi.dto.SendAuthCodeResDto
import com.sju18001.petmanagement.ui.login.SignInViewModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class CreateAccountUserInfoFragment : Fragment() {

    // pattern regex for EditTexts
    private val patternPhone: Pattern = Pattern.compile("(^02|^\\d{3})-(\\d{3}|\\d{4})-\\d{4}")
    private val patternEmail: Pattern = Patterns.EMAIL_ADDRESS

    // variables for view binding
    private var _binding: FragmentCreateAccountUserInfoBinding? = null
    private val binding get() = _binding!!

    // variable for storing API call(for cancel)
    private var codeRequestApiCall: Call<SendAuthCodeResDto>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentCreateAccountUserInfoBinding.inflate(inflater, container, false)
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
                    signInViewModel.createAccountPhoneValid = true
                    binding.phoneMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.createAccountPhoneValid = false
                    binding.phoneMessage.visibility = View.VISIBLE
                }
                signInViewModel.createAccountPhoneEditText = s.toString()
                signInViewModel.createAccountPhoneIsOverlap = false
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
                    signInViewModel.createAccountEmailValid = true
                    binding.emailMessage.visibility = View.GONE
                    binding.requestEmailCodeButton.isEnabled = true
                }
                else {
                    signInViewModel.createAccountEmailValid = false
                    binding.emailMessage.visibility = View.VISIBLE
                    binding.requestEmailCodeButton.isEnabled = false
                }
                signInViewModel.createAccountEmailEditText = s.toString()
                signInViewModel.createAccountEmailIsOverlap = false
                binding.emailMessageOverlap.visibility = View.GONE
                checkIsValid(signInViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for email code text change listener
        binding.emailCodeEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                signInViewModel.createAccountEmailCodeEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for request email code button
        binding.requestEmailCodeButton.setOnClickListener {
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
        if(signInViewModel.createAccountPhoneValid && signInViewModel.createAccountEmailValid) {
            (parentFragment as CreateAccountFragment).enableNextButton()
        }
        else{
            (parentFragment as CreateAccountFragment).disableNextButton()
        }
    }

    public fun showOverlapMessage(signInViewModel: SignInViewModel) {
        if(signInViewModel.createAccountPhoneIsOverlap) {
            binding.phoneMessageOverlap.visibility = View.VISIBLE
        }
        if(signInViewModel.createAccountEmailIsOverlap) {
            binding.emailMessageOverlap.visibility = View.VISIBLE
        }
    }

    public fun showHideRequestMessage(signInViewModel: SignInViewModel) {
        if(signInViewModel.showsEmailRequestMessage) {
            binding.emailMessageRequest.visibility = View.VISIBLE
        }
        else {
            binding.emailMessageRequest.visibility = View.GONE
        }
    }

    private fun startTimer(signInViewModel: SignInViewModel) {
        // set base
        binding.emailCodeChronometer.base = signInViewModel.emailCodeChronometerBase

        // start timer + show
        binding.emailCodeChronometer.start()
        binding.emailCodeChronometerLayout.visibility = View.VISIBLE
    }

    public fun lockEmailViews() {
        binding.emailEditText.isEnabled = false
        binding.requestEmailCodeButton.isEnabled = false
        binding.emailCodeEditText.isEnabled = false
        binding.emailMessageCodeValid.visibility = View.VISIBLE
        binding.emailCodeChronometer.stop()
        binding.emailCodeChronometerLayout.visibility = View.GONE
    }

    public fun unlockEmailViews() {
        binding.emailEditText.isEnabled = true
        binding.requestEmailCodeButton.isEnabled = true
        binding.emailCodeEditText.isEnabled = true
        binding.emailMessageCodeValid.visibility = View.GONE
        binding.emailCodeChronometer.stop()
        binding.emailCodeChronometerLayout.visibility = View.GONE
    }

    private fun requestEmailCode(signInViewModel: SignInViewModel) {
        val sendAuthCodeRequestDto = SendAuthCodeReqDto(signInViewModel.createAccountEmailEditText)

        codeRequestApiCall = RetrofitBuilder.getServerApi().sendAuthCodeReq(sendAuthCodeRequestDto)
        codeRequestApiCall!!.enqueue(object: Callback<SendAuthCodeResDto> {
            override fun onResponse(
                call: Call<SendAuthCodeResDto>,
                response: Response<SendAuthCodeResDto>
            ) {
                if(response.isSuccessful) {
                    // if success -> display a toast message
                    Toast.makeText(context, R.string.email_code_sent, Toast.LENGTH_LONG).show()

                    // set current code requested email
                    signInViewModel.currentCodeRequestedEmail = signInViewModel.createAccountEmailEditText

                    // hide request message
                    signInViewModel.showsEmailRequestMessage = false
                    showHideRequestMessage(signInViewModel)

                    // start a 10 minute timer
                    signInViewModel.emailCodeChronometerBase = SystemClock.elapsedRealtime() + 600000.toLong()
                    startTimer(signInViewModel)
                }
                else {
                    // get error message
                    val errorMessage = JSONObject(response.errorBody()!!.charStream().readText())
                        .getJSONObject("_metadata").getString("message").toString()

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

            override fun onFailure(call: Call<SendAuthCodeResDto>, t: Throwable) {
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
        if(binding.phoneEditText.text.toString() != signInViewModel.createAccountPhoneEditText) {
            binding.phoneEditText.setText(signInViewModel.createAccountPhoneEditText)
        }
        if(binding.emailEditText.text.toString() != signInViewModel.createAccountEmailEditText) {
            binding.emailEditText.setText(signInViewModel.createAccountEmailEditText)
        }
        if(binding.emailCodeEditText.text.toString() != signInViewModel.createAccountEmailCodeEditText) {
            binding.emailCodeEditText.setText(signInViewModel.createAccountEmailCodeEditText)
        }

        if(!signInViewModel.createAccountPhoneValid && signInViewModel.createAccountPhoneEditText != "") {
            binding.phoneMessage.visibility = View.VISIBLE
        }
        if(!signInViewModel.createAccountEmailValid && signInViewModel.createAccountEmailEditText != "") {
            binding.emailMessage.visibility = View.VISIBLE
        }
        if(signInViewModel.createAccountPhoneIsOverlap) {
            binding.phoneMessageOverlap.visibility = View.VISIBLE
        }
        if(signInViewModel.createAccountEmailIsOverlap) {
            binding.emailMessageOverlap.visibility = View.VISIBLE
        }
        if(signInViewModel.createAccountEmailValid && codeRequestApiCall == null) {
            binding.requestEmailCodeButton.isEnabled = true
        }
        if(signInViewModel.emailCodeChronometerBase != 0.toLong()) {
            startTimer(signInViewModel)
        }
        if(signInViewModel.emailCodeValid) {
            lockEmailViews()
        }
        showHideRequestMessage(signInViewModel)

        (parentFragment as CreateAccountFragment).showPreviousButton()
        checkIsValid(signInViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // stop api call when fragment is destroyed
        codeRequestApiCall?.cancel()
    }
}