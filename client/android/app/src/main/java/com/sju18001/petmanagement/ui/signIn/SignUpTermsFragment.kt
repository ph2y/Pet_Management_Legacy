package com.sju18001.petmanagement.ui.signIn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.databinding.FragmentSignUpTermsBinding

class SignUpTermsFragment: Fragment() {

    // variables for view binding
    private var _binding: FragmentSignUpTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentSignUpTermsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // variable for ViewModel
        val signInViewModel: SignInViewModel by activityViewModels()

        // for state restore(ViewModel)
        restoreState(signInViewModel)

        // for select all check box
        binding.selectAllCheckBox.setOnClickListener {
            signInViewModel.signUpSelectAllCheckBox = binding.selectAllCheckBox.isChecked
            selectDeselectAll(signInViewModel, signInViewModel.signUpSelectAllCheckBox)
            validCheck(signInViewModel)
        }

        // for terms check box
        binding.termsCheckBox.setOnClickListener {
            signInViewModel.signUpTermsCheckBox = binding.termsCheckBox.isChecked
            validCheck(signInViewModel)
        }

        // for privacy check box
        binding.privacyCheckBox.setOnClickListener {
            signInViewModel.signUpPrivacyCheckBox = binding.privacyCheckBox.isChecked
            validCheck(signInViewModel)
        }

        // for marketing check box
        binding.marketingCheckBox.setOnClickListener {
            signInViewModel.signUpMarketingCheckBox = binding.marketingCheckBox.isChecked
        }
    }

    private fun selectDeselectAll(signInViewModel: SignInViewModel, selected: Boolean) {
        signInViewModel.signUpTermsCheckBox = selected
        signInViewModel.signUpPrivacyCheckBox = selected
        signInViewModel.signUpMarketingCheckBox = selected

        binding.termsCheckBox.isChecked = signInViewModel.signUpTermsCheckBox
        binding.privacyCheckBox.isChecked = signInViewModel.signUpPrivacyCheckBox
        binding.marketingCheckBox.isChecked = signInViewModel.signUpMarketingCheckBox
    }

    private fun validCheck(signInViewModel: SignInViewModel) {
        if(signInViewModel.signUpTermsCheckBox && signInViewModel.signUpPrivacyCheckBox) {
            (parentFragment as SignUpFragment).enableNextButton()
        }
        else {
            (parentFragment as SignUpFragment).disableNextButton()
        }
    }

    private fun restoreState(signInViewModel: SignInViewModel) {
        binding.selectAllCheckBox.isChecked = signInViewModel.signUpSelectAllCheckBox
        binding.termsCheckBox.isChecked = signInViewModel.signUpTermsCheckBox
        binding.privacyCheckBox.isChecked = signInViewModel.signUpPrivacyCheckBox
        binding.marketingCheckBox.isChecked = signInViewModel.signUpMarketingCheckBox

        validCheck(signInViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}