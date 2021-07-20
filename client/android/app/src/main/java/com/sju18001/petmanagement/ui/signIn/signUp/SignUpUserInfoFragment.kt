package com.sju18001.petmanagement.ui.signIn.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sju18001.petmanagement.databinding.FragmentSignUpUserInfoBinding
import java.util.regex.Pattern

class SignUpUserInfoFragment : Fragment() {

    // pattern regex for EditTexts
    private val patternName: Pattern = Pattern.compile("^[a-zA-Z가-힣 ]{1,20}$")
    private val patternPhone: Pattern = Pattern.compile("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$")
    private val patternEmailCode: Pattern = Pattern.compile("^[0-9]{6}$")

    // variables for view binding
    private var _binding: FragmentSignUpUserInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentSignUpUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}