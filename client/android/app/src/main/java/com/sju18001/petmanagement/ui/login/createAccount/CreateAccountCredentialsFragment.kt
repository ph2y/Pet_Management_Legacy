package com.sju18001.petmanagement.ui.login.createAccount

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.databinding.FragmentCreateAccountCredentialsBinding
import com.sju18001.petmanagement.ui.login.LoginViewModel
import java.util.regex.Pattern

class CreateAccountCredentialsFragment : Fragment() {

    // pattern regex for EditTexts
    private val patternUsername: Pattern = Pattern.compile("^[a-z0-9]{5,16}$")
    private val patternPassword: Pattern = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{8,20}$")

    // variables for view binding
    private var _binding: FragmentCreateAccountCredentialsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentCreateAccountCredentialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // variable for ViewModel
        val loginViewModel: LoginViewModel by activityViewModels()

        // for state restore(ViewModel)
        restoreState(loginViewModel)

        // for username text change listener
        binding.usernameEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternUsername.matcher(s).matches()) {
                    loginViewModel.createAccountUsernameValid = true
                    binding.usernameMessage.visibility = View.GONE
                }
                else {
                    loginViewModel.createAccountUsernameValid = false
                    binding.usernameMessage.visibility = View.VISIBLE
                }
                loginViewModel.createAccountUsernameEditText = s.toString()
                loginViewModel.createAccountUsernameIsOverlap = false
                binding.usernameMessageOverlap.visibility = View.GONE
                checkIsValid(loginViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for pw text change listener
        binding.pwEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternPassword.matcher(s).matches()) {
                    loginViewModel.createAccountPwValid = true
                    binding.pwMessage.visibility = View.GONE
                }
                else {
                    loginViewModel.createAccountPwValid = false
                    binding.pwMessage.visibility = View.VISIBLE
                }
                if(s.toString() == binding.pwCheckEditText.text.toString()) {
                    loginViewModel.createAccountPwCheckValid = true
                    binding.pwCheckMessage.visibility = View.GONE
                }
                else {
                    loginViewModel.createAccountPwCheckValid = false
                    binding.pwCheckMessage.visibility = View.VISIBLE
                }
                loginViewModel.createAccountPwEditText = s.toString()
                checkIsValid(loginViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for pw check text change listener
        binding.pwCheckEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString() == binding.pwEditText.text.toString()) {
                    loginViewModel.createAccountPwCheckValid = true
                    binding.pwCheckMessage.visibility = View.GONE
                }
                else {
                    loginViewModel.createAccountPwCheckValid = false
                    binding.pwCheckMessage.visibility = View.VISIBLE
                }
                loginViewModel.createAccountPwCheckEditText = s.toString()
                checkIsValid(loginViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkIsValid(loginViewModel: LoginViewModel) {
        if(loginViewModel.createAccountUsernameValid && loginViewModel.createAccountPwValid && loginViewModel.createAccountPwCheckValid) {
            (parentFragment as CreateAccountFragment).enableNextButton()
        }
        else{
            (parentFragment as CreateAccountFragment).disableNextButton()
        }
    }

    private fun restoreState(loginViewModel: LoginViewModel) {
        if(binding.usernameEditText.text.toString() != loginViewModel.createAccountUsernameEditText) {
            binding.usernameEditText.setText(loginViewModel.createAccountUsernameEditText)
        }
        if(binding.pwEditText.text.toString() != loginViewModel.createAccountPwEditText) {
            binding.pwEditText.setText(loginViewModel.createAccountPwEditText)
        }
        if(binding.pwCheckEditText.text.toString() != loginViewModel.createAccountPwCheckEditText) {
            binding.pwCheckEditText.setText(loginViewModel.createAccountPwCheckEditText)
        }

        if(!loginViewModel.createAccountUsernameValid && loginViewModel.createAccountUsernameEditText != "") {
            binding.usernameMessage.visibility = View.VISIBLE
        }
        if(!loginViewModel.createAccountPwValid && loginViewModel.createAccountPwEditText != "") {
            binding.pwMessage.visibility = View.VISIBLE
        }
        if(!loginViewModel.createAccountPwCheckValid && loginViewModel.createAccountPwCheckEditText != "") {
            binding.pwCheckMessage.visibility = View.VISIBLE
        }
        if(loginViewModel.createAccountUsernameIsOverlap) {
            binding.usernameMessageOverlap.visibility = View.VISIBLE
        }

        (parentFragment as CreateAccountFragment).showPreviousButton()
        checkIsValid(loginViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}