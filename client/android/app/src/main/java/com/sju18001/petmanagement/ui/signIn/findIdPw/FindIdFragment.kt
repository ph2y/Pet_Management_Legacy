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
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.dto.AccountFindUsernameRequestDto
import com.sju18001.petmanagement.restapi.dto.AccountFindUsernameResponseDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindIdFragment : Fragment() {
    private var _binding: FragmentFindIdBinding? = null
    private val binding get() = _binding!!

    private val INPUT_LENGTH = 1
    private val EMAIL = 0
    private val isValidInput: HashMap<Int, Boolean> = HashMap()

    private var isViewDestroyed: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFindIdBinding.inflate(inflater, container, false)

        if(savedInstanceState?.getBoolean("is_result_shown") == true){
            val username = savedInstanceState.getString("result_username")
            setViewForResult(username)
        }

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
            activity?.let { Util().hideKeyboard(it) }

            findUsername(binding.emailEditText.text.toString())
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if(binding.resultLayout.visibility == View.VISIBLE){
            outState.putBoolean("is_result_shown", true)
            outState.putString("result_username", binding.resultUsername.text.toString())
        }else{
            outState.putBoolean("is_result_shown", false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        isViewDestroyed = true
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

        // 버튼 로딩 상태
        setButtonLoading(true)

        call.enqueue(object: Callback<AccountFindUsernameResponseDto> {
            override fun onResponse(
                call: Call<AccountFindUsernameResponseDto>,
                response: Response<AccountFindUsernameResponseDto>
            ) {
                if(!isViewDestroyed){
                    // 버튼 로딩 상태 해제
                    setButtonLoading(false)

                    if(response.isSuccessful){
                        response.body()?.let{
                            setViewForResult(it.username)
                        }
                    }else{
                        Toast.makeText(context, "해당 이메일을 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<AccountFindUsernameResponseDto>, t: Throwable) {
                if(!isViewDestroyed){
                    // 버튼 로딩 상태 해제
                    setButtonLoading(false)

                    Toast.makeText(context, "요청에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setButtonLoading(isLoading: Boolean){
        if(isLoading){
            binding.findIdButton.apply {
                text = ""
                isEnabled = false
            }
            binding.findIdProgressBar.visibility = View.VISIBLE
        }else{
            binding.findIdButton.apply {
                text = context?.getText(R.string.find_username)
                isEnabled = true
            }
            binding.findIdProgressBar.visibility = View.GONE
        }
    }

    // 아이디 찾기 결과
    private fun setViewForResult(username: String?){
        binding.resultUsername.text = username
        binding.findIdLayout.visibility = View.GONE
        binding.resultLayout.visibility = View.VISIBLE
    }
}
