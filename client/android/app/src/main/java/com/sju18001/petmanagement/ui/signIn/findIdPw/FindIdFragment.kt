package com.sju18001.petmanagement.ui.signIn.findIdPw

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentFindIdBinding

class FindIdFragment : Fragment(){
    private var _binding: FragmentFindIdBinding? = null
    private val binding get() = _binding!!

    private val INPUT_LENGTH = 1
    private val EMAIL = 0
    private val isValidInput: HashMap<Int, Boolean> = HashMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFindIdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // 아이디 찾기 버튼 클릭
        binding.findIdButton.setOnClickListener{
            activity?.let { Util().hideKeyboard(it) }
        }

        // 레이아웃 클릭
        binding.findIdLayout.setOnClickListener{
            activity?.let { Util().hideKeyboard(it) }
        }

        // 이메일 입력란 입력
        binding.emailEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    isValidInput[EMAIL] = true
                    binding.emailMessage.visibility = View.GONE
                }
                else {
                    isValidInput[EMAIL] = false
                    binding.emailMessage.visibility = View.VISIBLE
                }
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 유효성 검사
    private fun checkIsValid() {
        for(i in 0 until INPUT_LENGTH) {
            if(!isValidInput[i]!!) {
                // if not valid -> disable button + return
                binding.findIdButton.isEnabled = false
                return
            }
        }

        // if all is valid -> enable button
        binding.findIdButton.isEnabled = true
    }
}