package com.sju18001.petmanagement.ui.signIn

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentSignUpBinding
import com.sju18001.petmanagement.restapi.*
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class SignUpFragment : Fragment() {

    // variables for view binding
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    // variable for storing API call(for cancel)
    private var signUpApiCall: Call<AccountSignUpResponseDto>? = null

    // variable for checking the validation of inputs
    // [0]: id, [1]: pw, [2]: pw_check, [3]: name, [4]: phone, [5]: email, [6]: terms
    private val isValidInput: HashMap<Int, Boolean> = HashMap()

    // pattern regex for EditTexts(except Email)
    private val patternId: Pattern = Pattern.compile("^[a-z0-9]{5,16}$")
    private val patternPw: Pattern = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{8,20}$")
    private val patternName: Pattern = Pattern.compile("^[a-zA-Z가-힣 ]{1,20}$")
    private val patternPhone: Pattern = Pattern.compile("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // initialize valid input map
        for(i in 0..6) { isValidInput[i] = false }

        return root
    }

    override fun onStart() {
        super.onStart()

        // for back button(top-left)
        binding.backButton.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        // for id text change listener
        binding.idEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternId.matcher(s).matches()) {
                    isValidInput[0] = true
                    binding.idMessage.visibility = View.GONE
                }
                else {
                    isValidInput[0] = false
                    binding.idMessage.visibility = View.VISIBLE
                }
                binding.idMessageOverlap.visibility = View.GONE
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for pw text change listener
        binding.pwEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternPw.matcher(s).matches()) {
                    isValidInput[1] = true
                    binding.pwMessage.visibility = View.GONE
                }
                else {
                    isValidInput[1] = false
                    binding.pwMessage.visibility = View.VISIBLE
                }
                if(s.toString() == binding.pwCheckEditText.text.toString()) {
                    isValidInput[2] = true
                    binding.pwCheckMessage.visibility = View.GONE
                }
                else {
                    isValidInput[2] = false
                    binding.pwCheckMessage.visibility = View.VISIBLE
                }
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for pw check text change listener
        binding.pwCheckEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString() == binding.pwEditText.text.toString()) {
                    isValidInput[2] = true
                    binding.pwCheckMessage.visibility = View.GONE
                }
                else {
                    isValidInput[2] = false
                    binding.pwCheckMessage.visibility = View.VISIBLE
                }
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for name text change listener
        binding.nameEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternName.matcher(s).matches()) {
                    isValidInput[3] = true
                    binding.nameMessage.visibility = View.GONE
                }
                else {
                    isValidInput[3] = false
                    binding.nameMessage.visibility = View.VISIBLE
                }
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for phone text change listener
        binding.phoneEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternPhone.matcher(s).matches()) {
                    isValidInput[4] = true
                    binding.phoneMessage.visibility = View.GONE
                }
                else {
                    isValidInput[4] = false
                    binding.phoneMessage.visibility = View.VISIBLE
                }
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for email text change listener
        binding.emailEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    isValidInput[5] = true
                    binding.emailMessage.visibility = View.INVISIBLE
                }
                else {
                    isValidInput[5] = false
                    binding.emailMessage.visibility = View.VISIBLE
                }
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for terms check button
        binding.termsCheckBox.setOnClickListener {
            isValidInput[6] = binding.termsCheckBox.isChecked
            checkIsValid()
        }

        // for sign up button
        binding.signUpButton.setOnClickListener {
            // call sign up API
            signUp(binding.idEditText.text.toString(), binding.pwEditText.text.toString(),
                binding.emailEditText.text.toString(), binding.nameEditText.text.toString(),
            binding.phoneEditText.text.toString(), binding.marketingCheckBox.isChecked)

            // hide keyboard
            hideKeyboard()

            // disable sign up button
            disableSignUpButton()
        }

        // hide keyboard when touched signUpLayout
        binding.fragmentSignUpLayout.setOnClickListener{ hideKeyboard() }
    }

    private fun signUp(username: String, password: String, email: String, name: String, phone: String, marketing: Boolean) {
        // create sign up request Dto
        val accountSignUpRequestDto = AccountSignUpRequestDto(username, password, email, name, phone, null, marketing)

        // call API using Retrofit
        signUpApiCall = RetrofitBuilder.serverApi.signUpRequest(accountSignUpRequestDto)
        signUpApiCall!!.enqueue(object: Callback<AccountSignUpResponseDto> {
            override fun onResponse(
                call: Call<AccountSignUpResponseDto>,
                response: Response<AccountSignUpResponseDto>
            ) {
                if(response.isSuccessful) {
                    // return to previous fragment + send sign up result data
                    toPreviousFragment(true, null)
                }
                else {
                    // if id overlap -> show message + enable sign up button
                    binding.idMessageOverlap.visibility = View.VISIBLE
                    enableSignUpButton()
                }
            }

            override fun onFailure(call: Call<AccountSignUpResponseDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> do nothing
                if(_binding == null) { return }

                // return to previous fragment + send sign up result data
                toPreviousFragment(false, t.message.toString())

                // log error message
                Log.d("error", t.message.toString())
            }
        })
    }

    // hide keyboard
    private fun hideKeyboard() {
        Util().hideKeyboard(requireActivity(), binding.idEditText)
        Util().hideKeyboard(requireActivity(), binding.pwEditText)
        Util().hideKeyboard(requireActivity(), binding.pwCheckEditText)
        Util().hideKeyboard(requireActivity(), binding.nameEditText)
        Util().hideKeyboard(requireActivity(), binding.phoneEditText)
        Util().hideKeyboard(requireActivity(), binding.emailEditText)
    }

    // return to previous fragment + send sign up result data
    private fun toPreviousFragment(result: Boolean, message: String?) {
        // set result
        setFragmentResult("signUpResult", bundleOf("isSuccessful" to mutableListOf(result, message)))

        // return to previous fragment
        activity?.supportFragmentManager?.popBackStack()
    }

    // check for all valid(for enabling sign up button)
    private fun checkIsValid() {
        for(i in 0..6) {
            if(!isValidInput[i]!!) {
                // if not valid -> disable button + return
                binding.signUpButton.isEnabled = false
                return
            }
        }

        // if all is valid -> enable button
        binding.signUpButton.isEnabled = true
    }

    // disable sign up button
    private fun disableSignUpButton() {
        // set sign up button status to loading + disable
        binding.signUpButton.text = ""
        binding.signUpProgressBar.visibility = View.VISIBLE
        binding.signUpButton.isEnabled = false
    }

    // enable sign up button
    private fun enableSignUpButton() {
        // set sign up button status to active + enable
        binding.signUpButton.text = context?.getText(R.string.sign_up_confirm_button)
        binding.signUpProgressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // stop api call when fragment is destroyed
        signUpApiCall?.cancel()
    }
}