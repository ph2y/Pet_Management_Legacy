package com.sju18001.petmanagement.ui.login.createAccount

import android.os.Bundle
import android.os.SystemClock
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.PatternRegex
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCreateAccountUserInfoBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.dto.SendAuthCodeReqDto
import com.sju18001.petmanagement.restapi.dto.SendAuthCodeResDto
import com.sju18001.petmanagement.ui.login.LoginViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateAccountUserInfoFragment : Fragment() {

    // variables for view binding
    private var _binding: FragmentCreateAccountUserInfoBinding? = null
    private val binding get() = _binding!!

    private var isViewDestroyed = false

    // variable for storing API call
    private var codeRequestApiCall: Call<SendAuthCodeResDto>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentCreateAccountUserInfoBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // variable for ViewModel
        val loginViewModel: LoginViewModel by activityViewModels()

        // for state restore(ViewModel)
        restoreState(loginViewModel)

        // for phone text change listener
        binding.phoneEditText.addTextChangedListener(PhoneNumberFormattingTextWatcher("KR"))
        binding.phoneEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(PatternRegex.checkPhoneRegex(s)) {
                    loginViewModel.createAccountPhoneValid = true
                    binding.phoneMessage.visibility = View.GONE
                }
                else {
                    loginViewModel.createAccountPhoneValid = false
                    binding.phoneMessage.visibility = View.VISIBLE
                }
                loginViewModel.createAccountPhoneEditText = s.toString()
                loginViewModel.createAccountPhoneIsOverlap = false
                binding.phoneMessageOverlap.visibility = View.GONE
                checkIsValid(loginViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // emailEditText Listener
        binding.emailEditText.setOnEditorActionListener { _, _, _ ->
            Util.hideKeyboard(requireActivity())
            true
        }
        binding.emailEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(PatternRegex.checkEmailRegex(s)) {
                    loginViewModel.createAccountEmailValid = true
                    binding.emailMessage.visibility = View.GONE
                    binding.requestEmailCodeButton.isEnabled = true
                }
                else {
                    loginViewModel.createAccountEmailValid = false
                    binding.emailMessage.visibility = View.VISIBLE
                    binding.requestEmailCodeButton.isEnabled = false
                }
                loginViewModel.createAccountEmailEditText = s.toString()
                loginViewModel.createAccountEmailIsOverlap = false
                binding.emailMessageOverlap.visibility = View.GONE
                checkIsValid(loginViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // emailCodeEditText Listener
        binding.emailCodeEditText.setOnEditorActionListener { _, _, _ ->
            Util.hideKeyboard(requireActivity())
            true
        }
        binding.emailCodeEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginViewModel.createAccountEmailCodeEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for request email code button
        binding.requestEmailCodeButton.setOnClickListener {
            // set request code button to loading
            setRequestCodeButtonToLoading()

            // API call
            requestEmailCode(loginViewModel)
        }

        // for hiding keyboard
        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentCreateAccountUserInfoParentLayout)
    }

    // set request code button to normal
    private fun setRequestCodeButtonToNormal() {
        // set request code button status to normal + enable
        binding.requestEmailCodeButton.text = context?.getText(R.string.request_email_code_button)
        binding.requestEmailCodeProgressBar.visibility = View.GONE
        binding.requestEmailCodeButton.isEnabled = true

        binding.emailEditText.isEnabled = true
    }

    // set request code button to loading
    private fun setRequestCodeButtonToLoading() {
        // set request code button status to loading + disable
        binding.requestEmailCodeButton.text = ""
        binding.requestEmailCodeProgressBar.visibility = View.VISIBLE
        binding.requestEmailCodeButton.isEnabled = false

        binding.emailEditText.isEnabled = false
    }

    private fun checkIsValid(loginViewModel: LoginViewModel) {
        if(loginViewModel.createAccountPhoneValid && loginViewModel.createAccountEmailValid) {
            (parentFragment as CreateAccountFragment).enableNextButton()
        }
        else{
            (parentFragment as CreateAccountFragment).disableNextButton()
        }
    }

    public fun showOverlapMessage(loginViewModel: LoginViewModel) {
        if(loginViewModel.createAccountPhoneIsOverlap) {
            binding.phoneMessageOverlap.visibility = View.VISIBLE
        }
        if(loginViewModel.createAccountEmailIsOverlap) {
            binding.emailMessageOverlap.visibility = View.VISIBLE
        }
    }

    public fun showHideRequestMessage(loginViewModel: LoginViewModel) {
        if(loginViewModel.showsEmailRequestMessage) {
            binding.emailMessageRequest.visibility = View.VISIBLE
        }
        else {
            binding.emailMessageRequest.visibility = View.GONE
        }
    }

    private fun startTimer(loginViewModel: LoginViewModel) {
        // set base
        binding.emailCodeChronometer.base = loginViewModel.emailCodeChronometerBase

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

    public fun lockViews() {
        lockEmailViews()
        binding.phoneEditText.isEnabled = false
    }

    public fun unlockViews() {
        unlockEmailViews()
        binding.phoneEditText.isEnabled = true
    }

    private fun requestEmailCode(loginViewModel: LoginViewModel) {
        val sendAuthCodeReqDto = SendAuthCodeReqDto(loginViewModel.createAccountEmailEditText)

        codeRequestApiCall = RetrofitBuilder.getServerApi().sendAuthCodeReq(sendAuthCodeReqDto)
        codeRequestApiCall!!.enqueue(object: Callback<SendAuthCodeResDto> {
            override fun onResponse(
                call: Call<SendAuthCodeResDto>,
                response: Response<SendAuthCodeResDto>
            ) {
                if(isViewDestroyed) return

                if(response.isSuccessful) {
                    // if success -> display a toast message
                    Toast.makeText(context, R.string.email_code_sent, Toast.LENGTH_LONG).show()

                    // set current code requested email
                    loginViewModel.currentCodeRequestedEmail = loginViewModel.createAccountEmailEditText

                    // hide request message
                    loginViewModel.showsEmailRequestMessage = false
                    showHideRequestMessage(loginViewModel)

                    // start a 10 minute timer
                    loginViewModel.emailCodeChronometerBase = SystemClock.elapsedRealtime() + 600000.toLong()
                    startTimer(loginViewModel)
                }
                else {
                    Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
                }

                // set request code button to normal
                setRequestCodeButtonToNormal()

                // reset codeRequestApiCall variable
                codeRequestApiCall = null
            }

            override fun onFailure(call: Call<SendAuthCodeResDto>, t: Throwable) {
                if(isViewDestroyed) return

                setRequestCodeButtonToNormal()

                // reset codeRequestApiCall variable
                codeRequestApiCall = null

                Util.showToastAndLog(requireContext(), t.message.toString())
            }
        })
    }

    private fun restoreState(loginViewModel: LoginViewModel) {
        if(binding.phoneEditText.text.toString() != loginViewModel.createAccountPhoneEditText) {
            binding.phoneEditText.setText(loginViewModel.createAccountPhoneEditText)
        }
        if(binding.emailEditText.text.toString() != loginViewModel.createAccountEmailEditText) {
            binding.emailEditText.setText(loginViewModel.createAccountEmailEditText)
        }
        if(binding.emailCodeEditText.text.toString() != loginViewModel.createAccountEmailCodeEditText) {
            binding.emailCodeEditText.setText(loginViewModel.createAccountEmailCodeEditText)
        }

        if(!loginViewModel.createAccountPhoneValid && loginViewModel.createAccountPhoneEditText != "") {
            binding.phoneMessage.visibility = View.VISIBLE
        }
        if(!loginViewModel.createAccountEmailValid && loginViewModel.createAccountEmailEditText != "") {
            binding.emailMessage.visibility = View.VISIBLE
        }
        if(loginViewModel.createAccountPhoneIsOverlap) {
            binding.phoneMessageOverlap.visibility = View.VISIBLE
        }
        if(loginViewModel.createAccountEmailIsOverlap) {
            binding.emailMessageOverlap.visibility = View.VISIBLE
        }
        if(loginViewModel.createAccountEmailValid && codeRequestApiCall == null) {
            binding.requestEmailCodeButton.isEnabled = true
        }
        if(loginViewModel.emailCodeChronometerBase != 0.toLong()) {
            startTimer(loginViewModel)
        }
        if(loginViewModel.emailCodeValid) {
            lockEmailViews()
        }
        showHideRequestMessage(loginViewModel)

        (parentFragment as CreateAccountFragment).showPreviousButton()
        checkIsValid(loginViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
    }
}