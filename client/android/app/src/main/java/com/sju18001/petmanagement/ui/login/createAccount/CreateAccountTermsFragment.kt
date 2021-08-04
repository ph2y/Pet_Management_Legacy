package com.sju18001.petmanagement.ui.login.createAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.databinding.FragmentCreateAccountTermsBinding
import com.sju18001.petmanagement.ui.login.SignInViewModel

class CreateAccountTermsFragment: Fragment() {

    // variables for view binding
    private var _binding: FragmentCreateAccountTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentCreateAccountTermsBinding.inflate(inflater, container, false)
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
            signInViewModel.createAccountSelectAllCheckBox = binding.selectAllCheckBox.isChecked
            selectDeselectAll(signInViewModel, signInViewModel.createAccountSelectAllCheckBox)
            checkIsAllSelected(signInViewModel)
            checkIsValid(signInViewModel)
        }

        // for terms check box
        binding.termsCheckBox.setOnClickListener {
            signInViewModel.createAccountTermsCheckBox = binding.termsCheckBox.isChecked
            checkIsAllSelected(signInViewModel)
            checkIsValid(signInViewModel)
        }

        // for privacy check box
        binding.privacyCheckBox.setOnClickListener {
            signInViewModel.createAccountPrivacyCheckBox = binding.privacyCheckBox.isChecked
            checkIsAllSelected(signInViewModel)
            checkIsValid(signInViewModel)
        }

        // for marketing check box
        binding.marketingCheckBox.setOnClickListener {
            signInViewModel.createAccountMarketingCheckBox = binding.marketingCheckBox.isChecked
            checkIsAllSelected(signInViewModel)
        }
    }

    private fun selectDeselectAll(signInViewModel: SignInViewModel, selected: Boolean) {
        signInViewModel.createAccountTermsCheckBox = selected
        signInViewModel.createAccountPrivacyCheckBox = selected
        signInViewModel.createAccountMarketingCheckBox = selected

        binding.termsCheckBox.isChecked = signInViewModel.createAccountTermsCheckBox
        binding.privacyCheckBox.isChecked = signInViewModel.createAccountPrivacyCheckBox
        binding.marketingCheckBox.isChecked = signInViewModel.createAccountMarketingCheckBox
    }

    private fun checkIsAllSelected(signInViewModel: SignInViewModel) {
        signInViewModel.createAccountSelectAllCheckBox =
            signInViewModel.createAccountTermsCheckBox && signInViewModel.createAccountPrivacyCheckBox && signInViewModel.createAccountMarketingCheckBox
        binding.selectAllCheckBox.isChecked = signInViewModel.createAccountSelectAllCheckBox
    }

    private fun checkIsValid(signInViewModel: SignInViewModel) {
        if(signInViewModel.createAccountTermsCheckBox && signInViewModel.createAccountPrivacyCheckBox) {
            (parentFragment as CreateAccountFragment).enableNextButton()
        }
        else {
            (parentFragment as CreateAccountFragment).disableNextButton()
        }
    }

    private fun restoreState(signInViewModel: SignInViewModel) {
        binding.selectAllCheckBox.isChecked = signInViewModel.createAccountSelectAllCheckBox
        binding.termsCheckBox.isChecked = signInViewModel.createAccountTermsCheckBox
        binding.privacyCheckBox.isChecked = signInViewModel.createAccountPrivacyCheckBox
        binding.marketingCheckBox.isChecked = signInViewModel.createAccountMarketingCheckBox

        (parentFragment as CreateAccountFragment).hidePreviousButton()
        checkIsValid(signInViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}