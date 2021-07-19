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
import com.sju18001.petmanagement.controller.ServerUtil
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentFindPwBinding
import com.sju18001.petmanagement.restapi.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class FindPwFragment : Fragment() {
    private var _binding: FragmentFindPwBinding? = null
    private val binding get() = _binding!!

    // 정규식
    private val patternUsername: Pattern = Pattern.compile("^[a-z0-9]{5,16}$")

    private var isViewDestroyed: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFindPwBinding.inflate(inflater, container, false)

<<<<<<< HEAD
        when(savedInstanceState?.getInt("page")){
            1 -> setViewForCodeInput()
            2 -> setViewForResult()
            else -> setViewForEmailInput()
=======
        if(savedInstanceState?.getBoolean("is_input_code_shown") == true){
            setViewForInputCode()
>>>>>>> 167a893 (Fix error occurs for typed edit text when fragment restart in find id pw fragment)
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var page: Int = 0
        if(binding.codeInputLayout.visibility == View.VISIBLE){
            page = 1
        }else if(binding.resultLayout.visibility == View.VISIBLE){
            page = 2
        }
        outState.putInt("page", page)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        isViewDestroyed = true
    }


    // 유효성 검사
    private fun checkEmailValidation(s: CharSequence?) {
        if(Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
            binding.emailMessage.visibility = View.GONE
            binding.emailInputButton.isEnabled = true
        }
        else {
            binding.emailMessage.visibility = View.VISIBLE
            binding.emailInputButton.isEnabled = false
        }
    }

    private fun checkUsernameValidation(s: CharSequence?) {
        if(patternUsername.matcher(s).matches()) {
            binding.usernameMessage.visibility = View.GONE
            binding.codeInputButton.isEnabled = true
        }
        else {
            binding.usernameMessage.visibility = View.VISIBLE
            binding.codeInputButton.isEnabled = false
        }
    }

    // 이메일 입력창
    private fun setViewForEmailInput(){
        // 레이아웃 전환
        binding.emailInputLayout.visibility = View.VISIBLE
        binding.codeInputLayout.visibility = View.GONE
        binding.resultLayout.visibility = View.GONE

        // 정규식 검사
        checkEmailValidation(binding.emailEditText.text)
        setMessageGone()

<<<<<<< HEAD
        // 이메일 입력란 입력
        binding.emailEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkEmailValidation(s)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // 버튼 클릭
        binding.emailInputButton.setOnClickListener{
=======
        // initialize valid input map
        for(i in 0 until INPUT_LENGTH) { isValidInput[i] = false }

        checkIdValidation(binding.idEditText.text)
        checkEmailValidation(binding.emailEditText.text)
        checkIsValid()
        setMessageGone()

        // 비밀번호 찾기 버튼 클릭
        binding.findPwButton.setOnClickListener{
>>>>>>> 167a893 (Fix error occurs for typed edit text when fragment restart in find id pw fragment)
            activity?.let { Util().hideKeyboard(it) }

            // 인증코드 전송
            sendAuthCode(binding.emailEditText.text.toString())
        }

        // 레이아웃 클릭
        binding.emailInputLayout.setOnClickListener{
            activity?.let { Util().hideKeyboard(it) }
        }
    }

    // 코드 입력창
    private fun setViewForCodeInput() {
        // 레이아웃 전환
        binding.emailInputLayout.visibility = View.GONE
        binding.codeInputLayout.visibility = View.VISIBLE
        binding.resultLayout.visibility = View.GONE

        // 정규식 검사
        checkUsernameValidation(binding.usernameEditText.text)
        setMessageGone()

        // 아이디 입력란 입력
        binding.usernameEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
<<<<<<< HEAD
                checkUsernameValidation(s)
=======
                checkIdValidation(s)
                checkIsValid()
>>>>>>> 167a893 (Fix error occurs for typed edit text when fragment restart in find id pw fragment)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

<<<<<<< HEAD
        // 버튼 클릭
        binding.codeInputButton.setOnClickListener{
            activity?.let { Util().hideKeyboard(it) }

            // 코드 확인
            findPassword(binding.usernameEditText.text.toString(), binding.codeEditText.text.toString())
        }

        // 레이아웃 클릭
        binding.codeInputLayout.setOnClickListener{
            activity?.let { Util().hideKeyboard(it) }
        }
    }

    // 비밀번호 찾기 결과
    private fun setViewForResult(){
        // 레이아웃 전환
        binding.emailInputLayout.visibility = View.GONE
        binding.codeInputLayout.visibility = View.GONE
        binding.resultLayout.visibility = View.VISIBLE
    }

    // 에러 메시지 제거
    private fun setMessageGone() {
        binding.emailMessage.visibility = View.GONE
        binding.usernameMessage.visibility = View.GONE
        binding.codeMessage.visibility = View.GONE
    }

    // 코드 전송
    private fun sendAuthCode(email: String){
        val accountSendAuthCodeRequestDto = AccountSendAuthCodeRequestDto(email)

        // 버튼 로딩 상태
        setEmailInputButtonLoading(true)

        ServerUtil.sendAuthCode(accountSendAuthCodeRequestDto,
            { response ->
                if(!isViewDestroyed){
                    // 버튼 로딩 상태 해제
                    setEmailInputButtonLoading(false)

                    if(response.isSuccessful){
                        // 코드 입력
                        setViewForCodeInput()
                    }else{
                        // 어떤 이메일이든 코드 전송은 하기 때문에, 보통 실패할 수 없다.
                        Toast.makeText(context, "알 수 없는 에러가 발생하였습니다.", Toast.LENGTH_LONG).show()
                    }
                }
            }, { t ->
                if(!isViewDestroyed) {
                    // 버튼 로딩 상태 해제
                    setEmailInputButtonLoading(false)

                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
=======
        // 이메일 입력란 입력
        binding.emailEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkEmailValidation(s)
                checkIsValid()
>>>>>>> 167a893 (Fix error occurs for typed edit text when fragment restart in find id pw fragment)
            }
        )
    }

<<<<<<< HEAD
    // 이메일 입력 버튼 프로그래스바
    private fun setEmailInputButtonLoading(isLoading: Boolean){
        if(isLoading){
            binding.emailInputButton.apply {
                text = ""
                isEnabled = false
            }
            binding.emailInputProgressBar.visibility = View.VISIBLE
        }else{
            binding.emailInputButton.apply {
                text = context?.getText(R.string.find_password_email_input_button)
                isEnabled = true
=======
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
>>>>>>> 167a893 (Fix error occurs for typed edit text when fragment restart in find id pw fragment)
            }
            binding.emailInputProgressBar.visibility = View.GONE
        }
    }


    // 코드를 통한 비밀번호 임시 변경
    private fun findPassword(username: String, code: String){
        val accountFindPasswordRequestDto = AccountFindPasswordRequestDto(username, code)

        // 버튼 로딩 상태
        setCodeInputButtonLoading(true)

        ServerUtil.findPassword(accountFindPasswordRequestDto,
            { response ->
                if(!isViewDestroyed) {
                    // 버튼 로딩 상태 해제
                    setCodeInputButtonLoading(false)

                    if (response.isSuccessful) {
                        setViewForResult()
                    } else {
                        binding.codeMessage.visibility = View.VISIBLE
                    }
                }
            }, { t ->
                if(!isViewDestroyed) {
                    // 버튼 로딩 상태 해제
                    setCodeInputButtonLoading(false)

                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

<<<<<<< HEAD
    // 코드 입력 버튼 프로그래스바
    private fun setCodeInputButtonLoading(isLoading: Boolean){
        if(isLoading){
            binding.codeInputButton.apply {
                text = ""
                isEnabled = false
            }
            binding.codeInputProgressBar.visibility = View.VISIBLE
        }else{
            binding.codeInputButton.apply {
                text = context?.getText(R.string.confirm)
                isEnabled = true
=======

    private fun setViewForInputCode() {
        binding.codeEditText.visibility = View.VISIBLE
        binding.idEditText.visibility = View.GONE
        binding.emailEditText.visibility = View.GONE
        binding.findPwButton.apply{
            text = "확인"
            setOnClickListener{
                // 코드 확인
>>>>>>> 167a893 (Fix error occurs for typed edit text when fragment restart in find id pw fragment)
            }
            binding.codeInputProgressBar.visibility = View.GONE
        }
    }

    private fun setMessageGone() {
        binding.idMessage.visibility = View.GONE
        binding.emailMessage.visibility = View.GONE
    }
}