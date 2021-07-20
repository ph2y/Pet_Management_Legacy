package com.sju18001.petmanagement.ui.signIn.signUp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.databinding.FragmentSignUpIdPwBinding
import com.sju18001.petmanagement.ui.signIn.SignInViewModel
import java.util.regex.Pattern

class SignUpIdPwFragment : Fragment() {

    // pattern regex for EditTexts
    private val patternId: Pattern = Pattern.compile("^[a-z0-9]{5,16}$")
    private val patternPw: Pattern = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{8,20}$")

    // variables for view binding
    private var _binding: FragmentSignUpIdPwBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentSignUpIdPwBinding.inflate(inflater, container, false)
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
                    signInViewModel.signUpIdValid = true
                    binding.idMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.signUpIdValid = false
                    binding.idMessage.visibility = View.VISIBLE
                }
                signInViewModel.signUpIdEditText = s.toString()
                signInViewModel.signUpIdIsOverlap = false
                binding.idMessageOverlap.visibility = View.GONE
                validCheck(signInViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for pw text change listener
        binding.pwEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternPw.matcher(s).matches()) {
                    signInViewModel.signUpPwValid = true
                    binding.pwMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.signUpPwValid = false
                    binding.pwMessage.visibility = View.VISIBLE
                }
                if(s.toString() == binding.pwCheckEditText.text.toString()) {
                    signInViewModel.signUpPwCheckValid = true
                    binding.pwCheckMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.signUpPwCheckValid = false
                    binding.pwCheckMessage.visibility = View.VISIBLE
                }
                signInViewModel.signUpPwEditText = s.toString()
                validCheck(signInViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for pw check text change listener
        binding.pwCheckEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString() == binding.pwEditText.text.toString()) {
                    signInViewModel.signUpPwCheckValid = true
                    binding.pwCheckMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.signUpPwCheckValid = false
                    binding.pwCheckMessage.visibility = View.VISIBLE
                }
                signInViewModel.signUpPwCheckEditText = s.toString()
                validCheck(signInViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validCheck(signInViewModel: SignInViewModel) {
        if(signInViewModel.signUpIdValid && signInViewModel.signUpPwValid && signInViewModel.signUpPwCheckValid) {
            (parentFragment as SignUpFragment).enableNextButton()
        }
        else{
            (parentFragment as SignUpFragment).disableNextButton()
        }
    }

    private fun restoreState(signInViewModel: SignInViewModel) {
        binding.idEditText.setText(signInViewModel.signUpIdEditText)
        binding.pwEditText.setText(signInViewModel.signUpPwEditText)
        binding.pwCheckEditText.setText(signInViewModel.signUpPwCheckEditText)

        if(!signInViewModel.signUpIdValid && signInViewModel.signUpIdEditText != "") {
            binding.idMessage.visibility = View.VISIBLE
        }
        if(!signInViewModel.signUpPwValid && signInViewModel.signUpPwEditText != "") {
            binding.pwMessage.visibility = View.VISIBLE
        }
        if(!signInViewModel.signUpPwCheckValid && signInViewModel.signUpPwCheckEditText != "") {
            binding.pwCheckMessage.visibility = View.VISIBLE
        }
        if(signInViewModel.signUpIdIsOverlap) {
            binding.idMessageOverlap.visibility = View.VISIBLE
        }

        (parentFragment as SignUpFragment).showPreviousButton()
        validCheck(signInViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}