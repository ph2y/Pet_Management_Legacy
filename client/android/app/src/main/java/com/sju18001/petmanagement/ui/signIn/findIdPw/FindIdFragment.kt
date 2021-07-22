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
import android.widget.Toast
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentFindIdBinding
import com.sju18001.petmanagement.restapi.AccountFindUsernameRequestDto
import com.sju18001.petmanagement.restapi.AccountFindUsernameResponseDto
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindIdFragment : Fragment() {
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

        // initialize valid input map
        for(i in 0 until INPUT_LENGTH) { isValidInput[i] = false }
        checkEmailValidation(binding.emailEditText.text)
        checkIsValid()
        setMessageGone()

        // 아이디 찾기 버튼 클릭
        binding.findIdButton.setOnClickListener{
            activity?.let {
                Util().hideKeyboard(it)
                findUsername(binding.emailEditText.text.toString())
            }
        }

        // 레이아웃 클릭
        binding.findIdLayout.setOnClickListener{
            activity?.let { Util().hideKeyboard(it) }
        }

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


    // * 유효성 검사
    private fun checkEmailValidation(s: CharSequence?){
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
                binding.findIdButton.isEnabled = false
                return
            }
        }

        // if all is valid -> enable button
        binding.findIdButton.isEnabled = true
    }

    private fun setMessageGone() {
        binding.emailMessage.visibility = View.GONE
    }


    // 아이디 찾기
    private fun findUsername(email: String){
        val reqBody = AccountFindUsernameRequestDto(email)
        val call = RetrofitBuilder.getServerApi().findUsernameRequest(reqBody)

        call.enqueue(object: Callback<AccountFindUsernameResponseDto> {
            override fun onResponse(
                call: Call<AccountFindUsernameResponseDto>,
                response: Response<AccountFindUsernameResponseDto>
            ) {
                if(response.isSuccessful){
                    response.body()?.let{
                        binding.resultUsername.text = it.username
                        binding.findIdLayout.visibility = View.GONE
                        binding.resultLayout.visibility = View.VISIBLE
                    }
                }else{
                    Toast.makeText(context, "해당 이메일을 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<AccountFindUsernameResponseDto>, t: Throwable) {
                Toast.makeText(context, "요청에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}