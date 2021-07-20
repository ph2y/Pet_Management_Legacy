package com.sju18001.petmanagement.ui.signIn.findIdPw

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentFindPwBinding
import java.util.regex.Pattern

class FindPwFragment : Fragment() {
    private var _binding: FragmentFindPwBinding? = null
    private val binding get() = _binding!!

    private val INPUT_LENGTH = 2
    private val ID = 0
    private val EMAIL = 1
    private val isValidInput: HashMap<Int, Boolean> = HashMap()

    private val patternId: Pattern = Pattern.compile("^[a-z0-9]{5,16}$")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFindPwBinding.inflate(inflater, container, false)

        if(savedInstanceState?.getBoolean("is_input_code_shown") == true){
            setViewForInputCode()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // initialize valid input map
        for(i in 0 until INPUT_LENGTH) { isValidInput[i] = false }

        checkIdValidation(binding.idEditText.text)
        checkEmailValidation(binding.emailEditText.text)
        checkIsValid()
        setMessageGone()

        // 비밀번호 찾기 버튼 클릭
        binding.findPwButton.setOnClickListener{
            activity?.let { Util().hideKeyboard(it) }
            
            // API 서버를 통해, id-email이 유효한지 검사
            setViewForInputCode()
        }

        // 레이아웃 클릭
        binding.findPwLayout.setOnClickListener{
            activity?.let { Util().hideKeyboard(it) }
        }

        // ID 입력란 입력
        binding.idEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkIdValidation(s)
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // 이메일 입력란 입력
        binding.emailEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkEmailValidation(s)
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("is_input_code_shown", binding.codeEditText.visibility == View.VISIBLE)
    }


    // * 유효성 검사
    private fun checkIdValidation(s: CharSequence?) {
        if(patternId.matcher(s).matches()) {
            isValidInput[ID] = true
            binding.idMessage.visibility = View.GONE
        }
        else {
            isValidInput[ID] = false
            binding.idMessage.visibility = View.VISIBLE
        }
    }

    private fun checkEmailValidation(s: CharSequence?) {
        if(Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
            isValidInput[EMAIL] = true
            binding.emailMessage.visibility = View.GONE
        }
        else {
            isValidInput[EMAIL] = false
            binding.emailMessage.visibility = View.VISIBLE
        }
    }

    private fun checkIsValid() {
        for(i in 0 until INPUT_LENGTH) {
            if(!isValidInput[i]!!) {
                // if not valid -> disable button + return
                binding.findPwButton.isEnabled = false
                return
            }
        }

        // if all is valid -> enable button
        binding.findPwButton.isEnabled = true
    }


    private fun setViewForInputCode() {
        binding.codeEditText.visibility = View.VISIBLE
        binding.idEditText.visibility = View.GONE
        binding.emailEditText.visibility = View.GONE
        binding.findPwButton.apply{
            text = "확인"
            setOnClickListener{
                // 코드 확인
            }
        }
    }

    private fun setMessageGone() {
        binding.idMessage.visibility = View.GONE
        binding.emailMessage.visibility = View.GONE
    }
}