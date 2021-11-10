package com.sju18001.petmanagement.ui.login.recovery

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.PatternRegex
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentRecoverUsernameBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.dto.RecoverUsernameReqDto
import com.sju18001.petmanagement.restapi.dto.RecoverUsernameResDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecoverUsernameFragment : Fragment() {
    private var _binding: FragmentRecoverUsernameBinding? = null
    private val binding get() = _binding!!

    private val INPUT_LENGTH = 1
    private val EMAIL = 0
    private val isValidInput: HashMap<Int, Boolean> = HashMap()

    private var isViewDestroyed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecoverUsernameBinding.inflate(inflater, container, false)
        isViewDestroyed = false

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
        binding.recoverUsernameButton.setOnClickListener{
            recoverUsername(binding.emailEditText.text.toString())
        }

        // 이메일 입력란 입력
        binding.emailEditText.setOnEditorActionListener{ _, _, _ ->
            if(binding.recoverUsernameButton.isEnabled){
                Util.hideKeyboard(requireActivity())
                recoverUsername(binding.emailEditText.text.toString())
            }

            true
        }
        binding.emailEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkEmailValidation(s)
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentRecoveryUsernameParentLayout)
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
        _binding = null

        isViewDestroyed = true
    }


    // * 유효성 검사
    private fun checkEmailValidation(s: CharSequence?){
        if(PatternRegex.checkEmailRegex(s)) {
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
                binding.recoverUsernameButton.isEnabled = false
                return
            }
        }

        // if all is valid -> enable button
        binding.recoverUsernameButton.isEnabled = true
    }

    private fun setMessageGone() {
        binding.emailMessage.visibility = View.GONE
    }


    private fun recoverUsername(email: String){
        // 버튼 로딩 상태
        setButtonLoading(true)
        lockViews()

        val call = RetrofitBuilder.getServerApi().recoverUsernameReq(RecoverUsernameReqDto(email))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            // 버튼 로딩 상태 해제
            setButtonLoading(false)
            unlockViews()

            response.body()?.let{
                setViewForResult(it.username)
            }
        }, {
            // 버튼 로딩 상태 해제
            setButtonLoading(false)
            unlockViews()
        }, {
            setButtonLoading(false)
            unlockViews()
        })
    }

    private fun setButtonLoading(isLoading: Boolean){
        if(isLoading){
            binding.recoverUsernameButton.apply {
                text = ""
                isEnabled = false
            }
            binding.recoverUsernameProgressBar.visibility = View.VISIBLE
        }else{
            binding.recoverUsernameButton.apply {
                text = context?.getText(R.string.recover_username)
                isEnabled = true
            }
            binding.recoverUsernameProgressBar.visibility = View.GONE
        }
    }

    private fun lockViews() {
        binding.emailEditText.isEnabled = false
    }

    private fun unlockViews() {
        binding.emailEditText.isEnabled = true
    }

    private fun setViewForResult(username: String?){
        binding.resultUsername.text = username
        binding.recoverUsernameLayout.visibility = View.GONE
        binding.resultLayout.visibility = View.VISIBLE
    }
}
