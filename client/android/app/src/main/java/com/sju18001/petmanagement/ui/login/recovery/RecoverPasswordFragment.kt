package com.sju18001.petmanagement.ui.login.recovery

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.PatternRegex
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentRecoverPasswordBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.dto.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecoverPasswordFragment : Fragment() {
    private var _binding: FragmentRecoverPasswordBinding? = null
    private val binding get() = _binding!!
    private var isViewDestroyed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecoverPasswordBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        when(savedInstanceState?.getInt("page")){
            1 -> setViewForCodeInput()
            2 -> setViewForResult()
            else -> setViewForEmailInput()
        }

        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentRecoverPasswordParentLayout)

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
        _binding = null

        isViewDestroyed = true
    }


    // 유효성 검사
    private fun checkEmailValidation(s: CharSequence?) {
        if(PatternRegex.checkEmailRegex(s)) {
            binding.emailMessage.visibility = View.GONE
            binding.emailInputButton.isEnabled = true
        }
        else {
            binding.emailMessage.visibility = View.VISIBLE
            binding.emailInputButton.isEnabled = false
        }
    }

    private fun checkUsernameValidation(s: CharSequence?) {
        if(PatternRegex.checkUsernameRegex(s)) {
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

        // 이메일 입력란 입력
        binding.emailEditText.setOnEditorActionListener{ _, _, _ ->
            if(binding.emailInputButton.isEnabled){
                Util.hideKeyboard(requireActivity())
                sendAuthCode(binding.emailEditText.text.toString())
            }

            true
        }
        binding.emailEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkEmailValidation(s)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // 버튼 클릭
        binding.emailInputButton.setOnClickListener{
            sendAuthCode(binding.emailEditText.text.toString())
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
                checkUsernameValidation(s)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // 인증 코드 입력
        binding.codeEditText.setOnEditorActionListener{ _, _, _ ->
            if(binding.codeInputButton.isEnabled){
                Util.hideKeyboard(requireActivity())
                recoverPassword(binding.usernameEditText.text.toString(), binding.codeEditText.text.toString())
            }

            true
        }

        // 버튼 클릭
        binding.codeInputButton.setOnClickListener{
            recoverPassword(binding.usernameEditText.text.toString(), binding.codeEditText.text.toString())
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
        // 버튼 로딩 상태
        setEmailInputButtonLoading(true)
        lockViews()

        val call = RetrofitBuilder.getServerApi().sendAuthCodeReq(SendAuthCodeReqDto(email))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            // 버튼 로딩 상태 해제
            setEmailInputButtonLoading(false)
            unlockViews()

            setViewForCodeInput()
        }, {
            // 버튼 로딩 상태 해제
            setEmailInputButtonLoading(false)
            unlockViews()
        }, {
            setEmailInputButtonLoading(false)
            unlockViews()
        })
    }

    private fun setEmailInputButtonLoading(isLoading: Boolean){
        if(isLoading){
            binding.emailInputButton.apply {
                text = ""
                isEnabled = false
            }
            binding.emailInputProgressBar.visibility = View.VISIBLE
        }else{
            binding.emailInputButton.apply {
                text = context?.getText(R.string.recover_password_email_input_button)
                isEnabled = true
            }
            binding.emailInputProgressBar.visibility = View.GONE
        }
    }


    private fun recoverPassword(username: String, code: String){
        setCodeInputButtonLoading(true)
        lockViews()

        val call = RetrofitBuilder.getServerApi().recoverPasswordReq(RecoverPasswordReqDto(username, code))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            setCodeInputButtonLoading(false)
            unlockViews()

            setViewForResult()
        }, {
            setCodeInputButtonLoading(false)
            unlockViews()

            binding.codeMessage.visibility = View.VISIBLE
        }, {
            setCodeInputButtonLoading(false)
            unlockViews()
        })
    }

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
            }
            binding.codeInputProgressBar.visibility = View.GONE
        }
    }

    private fun lockViews() {
        binding.emailEditText.isEnabled = false
        binding.usernameEditText.isEnabled = false
        binding.codeEditText.isEnabled = false
    }

    private fun unlockViews() {
        binding.emailEditText.isEnabled = true
        binding.usernameEditText.isEnabled = true
        binding.codeEditText.isEnabled = true
    }
}
