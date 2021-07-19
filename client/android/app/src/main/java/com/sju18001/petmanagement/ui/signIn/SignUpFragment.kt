package com.sju18001.petmanagement.ui.signIn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentSignUpBinding
import com.sju18001.petmanagement.restapi.*
import retrofit2.Call
import java.util.regex.Pattern

class SignUpFragment : Fragment() {

    // variables for view binding
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // for close button
        binding.closeButton.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        // for child fragment(open terms fragment)
        if(childFragmentManager.findFragmentById(R.id.child_fragment_container) == null) {
            val signUpTermsFragment = SignUpTermsFragment()
            childFragmentManager.beginTransaction()
                .add(R.id.child_fragment_container, signUpTermsFragment)
                .commit()
        }

        // hide keyboard when touched signUpLayout
        binding.fragmentSignUpLayout.setOnClickListener{ Util().hideKeyboard(requireActivity()) }
    }

    // show previous step button
    public fun showPreviousButton() {
        binding.previousStepButton.visibility = View.VISIBLE
    }

    // hide previous step button
    public fun hidePreviousButton() {
        binding.previousStepButton.visibility = View.INVISIBLE
    }

    // enable next step button
    public fun enableNextButton() {
        binding.nextStepButton.isEnabled = true
    }

    // disable next step button
    public fun disableNextButton() {
        binding.nextStepButton.isEnabled = false
    }

    // return to previous fragment + send sign up result data
    private fun toPreviousFragment(result: Boolean, message: String?) {
        // set result
        setFragmentResult("signUpResult", bundleOf("isSuccessful" to mutableListOf(result, message)))

        // return to previous fragment
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}