package com.sju18001.petmanagement.ui.login.createAccount

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.databinding.FragmentCreateAccountIdPwBinding
import com.sju18001.petmanagement.ui.login.SignInViewModel
import java.util.regex.Pattern

class CreateAccountIdPwFragment : Fragment() {

    // pattern regex for EditTexts
    private val patternId: Pattern = Pattern.compile("^[a-z0-9]{5,16}$")
    private val patternPw: Pattern = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{8,20}$")

    // variables for view binding
    private var _binding: FragmentCreateAccountIdPwBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentCreateAccountIdPwBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // variable for ViewModel
        val signInViewModel: SignInViewModel by activityViewModels()

        // for state restore(ViewModel)
        restoreState(signInViewModel)

        // for id text change listener
        binding.idEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternId.matcher(s).matches()) {
                    signInViewModel.createAccountIdValid = true
                    binding.idMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.createAccountIdValid = false
                    binding.idMessage.visibility = View.VISIBLE
                }
                signInViewModel.createAccountIdEditText = s.toString()
                signInViewModel.createAccountIdIsOverlap = false
                binding.idMessageOverlap.visibility = View.GONE
                checkIsValid(signInViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for pw text change listener
        binding.pwEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternPw.matcher(s).matches()) {
                    signInViewModel.createAccountPwValid = true
                    binding.pwMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.createAccountPwValid = false
                    binding.pwMessage.visibility = View.VISIBLE
                }
                if(s.toString() == binding.pwCheckEditText.text.toString()) {
                    signInViewModel.createAccountPwCheckValid = true
                    binding.pwCheckMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.createAccountPwCheckValid = false
                    binding.pwCheckMessage.visibility = View.VISIBLE
                }
                signInViewModel.createAccountPwEditText = s.toString()
                checkIsValid(signInViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for pw check text change listener
        binding.pwCheckEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString() == binding.pwEditText.text.toString()) {
                    signInViewModel.createAccountPwCheckValid = true
                    binding.pwCheckMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.createAccountPwCheckValid = false
                    binding.pwCheckMessage.visibility = View.VISIBLE
                }
                signInViewModel.createAccountPwCheckEditText = s.toString()
                checkIsValid(signInViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkIsValid(signInViewModel: SignInViewModel) {
        if(signInViewModel.createAccountIdValid && signInViewModel.createAccountPwValid && signInViewModel.createAccountPwCheckValid) {
            (parentFragment as CreateAccountFragment).enableNextButton()
        }
        else{
            (parentFragment as CreateAccountFragment).disableNextButton()
        }
    }

    private fun restoreState(signInViewModel: SignInViewModel) {
        if(binding.idEditText.text.toString() != signInViewModel.createAccountIdEditText) {
            binding.idEditText.setText(signInViewModel.createAccountIdEditText)
        }
        if(binding.pwEditText.text.toString() != signInViewModel.createAccountPwEditText) {
            binding.pwEditText.setText(signInViewModel.createAccountPwEditText)
        }
        if(binding.pwCheckEditText.text.toString() != signInViewModel.createAccountPwCheckEditText) {
            binding.pwCheckEditText.setText(signInViewModel.createAccountPwCheckEditText)
        }

        if(!signInViewModel.createAccountIdValid && signInViewModel.createAccountIdEditText != "") {
            binding.idMessage.visibility = View.VISIBLE
        }
        if(!signInViewModel.createAccountPwValid && signInViewModel.createAccountPwEditText != "") {
            binding.pwMessage.visibility = View.VISIBLE
        }
        if(!signInViewModel.createAccountPwCheckValid && signInViewModel.createAccountPwCheckEditText != "") {
            binding.pwCheckMessage.visibility = View.VISIBLE
        }
        if(signInViewModel.createAccountIdIsOverlap) {
            binding.idMessageOverlap.visibility = View.VISIBLE
        }

        (parentFragment as CreateAccountFragment).showPreviousButton()
        checkIsValid(signInViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}