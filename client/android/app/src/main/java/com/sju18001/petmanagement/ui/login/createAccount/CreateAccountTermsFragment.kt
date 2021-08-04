package com.sju18001.petmanagement.ui.login.createAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.databinding.FragmentCreateAccountTermsBinding
import com.sju18001.petmanagement.ui.login.LoginViewModel

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
        val loginViewModel: LoginViewModel by activityViewModels()

        // for state restore(ViewModel)
        restoreState(loginViewModel)

        // for select all check box
        binding.selectAllCheckBox.setOnClickListener {
            loginViewModel.createAccountSelectAllCheckBox = binding.selectAllCheckBox.isChecked
            selectDeselectAll(loginViewModel, loginViewModel.createAccountSelectAllCheckBox)
            checkIsAllSelected(loginViewModel)
            checkIsValid(loginViewModel)
        }

        // for terms check box
        binding.termsCheckBox.setOnClickListener {
            loginViewModel.createAccountTermsCheckBox = binding.termsCheckBox.isChecked
            checkIsAllSelected(loginViewModel)
            checkIsValid(loginViewModel)
        }

        // for privacy check box
        binding.privacyCheckBox.setOnClickListener {
            loginViewModel.createAccountPrivacyCheckBox = binding.privacyCheckBox.isChecked
            checkIsAllSelected(loginViewModel)
            checkIsValid(loginViewModel)
        }

        // for marketing check box
        binding.marketingCheckBox.setOnClickListener {
            loginViewModel.createAccountMarketingCheckBox = binding.marketingCheckBox.isChecked
            checkIsAllSelected(loginViewModel)
        }
    }

    private fun selectDeselectAll(loginViewModel: LoginViewModel, selected: Boolean) {
        loginViewModel.createAccountTermsCheckBox = selected
        loginViewModel.createAccountPrivacyCheckBox = selected
        loginViewModel.createAccountMarketingCheckBox = selected

        binding.termsCheckBox.isChecked = loginViewModel.createAccountTermsCheckBox
        binding.privacyCheckBox.isChecked = loginViewModel.createAccountPrivacyCheckBox
        binding.marketingCheckBox.isChecked = loginViewModel.createAccountMarketingCheckBox
    }

    private fun checkIsAllSelected(loginViewModel: LoginViewModel) {
        loginViewModel.createAccountSelectAllCheckBox =
            loginViewModel.createAccountTermsCheckBox && loginViewModel.createAccountPrivacyCheckBox && loginViewModel.createAccountMarketingCheckBox
        binding.selectAllCheckBox.isChecked = loginViewModel.createAccountSelectAllCheckBox
    }

    private fun checkIsValid(loginViewModel: LoginViewModel) {
        if(loginViewModel.createAccountTermsCheckBox && loginViewModel.createAccountPrivacyCheckBox) {
            (parentFragment as CreateAccountFragment).enableNextButton()
        }
        else {
            (parentFragment as CreateAccountFragment).disableNextButton()
        }
    }

    private fun restoreState(loginViewModel: LoginViewModel) {
        binding.selectAllCheckBox.isChecked = loginViewModel.createAccountSelectAllCheckBox
        binding.termsCheckBox.isChecked = loginViewModel.createAccountTermsCheckBox
        binding.privacyCheckBox.isChecked = loginViewModel.createAccountPrivacyCheckBox
        binding.marketingCheckBox.isChecked = loginViewModel.createAccountMarketingCheckBox

        (parentFragment as CreateAccountFragment).hidePreviousButton()
        checkIsValid(loginViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}