package com.sju18001.petmanagement.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.snackbar.Snackbar
import com.sju18001.petmanagement.MainActivity
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentLoginBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.login.createAccount.CreateAccountFragment
import com.sju18001.petmanagement.ui.login.recovery.RecoveryFragment
import com.sju18001.petmanagement.ui.welcomePage.WelcomePageActivity
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    // variables for view binding
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private var isViewDestroyed = false

    // Snackbar variable(for dismiss)
    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get create account result
        setFragmentResultListener("createAccountResult") { _, bundle ->
            val result: Boolean = bundle.get("isSuccessful") as Boolean

            // if successful -> show success message
            if(result) {
                displaySuccessMessage(context?.getText(R.string.create_account_success)!!.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // variable for ViewModel
        val loginViewModel: LoginViewModel by activityViewModels()

        // restore EditText values after view destruction
        binding.usernameEditText.setText(loginViewModel.loginUsernameEditText)
        binding.pwEditText.setText(loginViewModel.loginPwEditText)

        // reset create account values in ViewModel
        loginViewModel.resetCreateAccountValues()

        // for id text change listener
        binding.usernameEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginViewModel.loginUsernameEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // pwEditText Listener
        binding.pwEditText.setOnEditorActionListener { _, _, _ ->
            Util.hideKeyboard(requireActivity())

            lockViews()
            login(binding.usernameEditText.text.toString(), binding.pwEditText.text.toString())

            true
        }
        binding.pwEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginViewModel.loginPwEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for login button
        binding.loginButton.setOnClickListener {
            lockViews()
            login(binding.usernameEditText.text.toString(), binding.pwEditText.text.toString())
        }

        // for create account button
        binding.createAccountButton.setOnClickListener {
            val createAccountFragment = CreateAccountFragment()
            activity?.supportFragmentManager?.beginTransaction()!!
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.login_activity_fragment_container, createAccountFragment)
                .addToBackStack(null)
                .commit()
        }

        // for recovery button
        binding.recoveryButton.setOnClickListener {
            val recoveryFragment = RecoveryFragment()
            activity?.supportFragmentManager?.beginTransaction()!!
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.login_activity_fragment_container, recoveryFragment)
                .addToBackStack(null)
                .commit()
        }

        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentLoginParentLayout)
    }

    private fun login(username: String, password: String) {
        val call = RetrofitBuilder.getServerApi().loginReq(LoginReqDto(username, password))
        call.enqueue(object: Callback<LoginResDto> {
            override fun onResponse(
                call: Call<LoginResDto>,
                response: Response<LoginResDto>
            ) {
                if(isViewDestroyed) return

                if(response.isSuccessful) {
                    checkIsFirstLoginAndSwitchActivity(response.body()!!.token!!)
                }
                else {
                    // create custom snack bar to display error message
                    displayErrorMessage(context?.getText(R.string.login_failed)!!.toString())

                    // enable buttons & editText
                    unlockViews()
                }
            }

            override fun onFailure(call: Call<LoginResDto>, t: Throwable) {
                if(isViewDestroyed) return

                // create custom snack bar to display error message
                displayErrorMessage(context?.getText(R.string.default_error_message)!!.toString())

                unlockViews()

                // manually log error message
                Util.log(context!!, t.message.toString())
                Log.d("error", t.message.toString())
            }
        })
    }

    // 첫 로그인인지 체킹 후 액티비티 전환
    private fun checkIsFirstLoginAndSwitchActivity(token: String){
        val call = RetrofitBuilder.getServerApiWithToken(token).fetchAccountReq(ServerUtil.getEmptyBody())
        call.enqueue(object: Callback<FetchAccountResDto> {
            override fun onResponse(
                call: Call<FetchAccountResDto>,
                response: Response<FetchAccountResDto>
            ) {
                if(isViewDestroyed) return

                response.body()?.let{
                    // 조회 성공
                    if(response.isSuccessful){
                        // 첫 로그인일 시
                        if(it.nickname == "#"){
                            // nickname => username 변경
                            val call = RetrofitBuilder.getServerApiWithToken(token)
                                .updateAccountReq(UpdateAccountReqDto(it.email, it.phone, it.username, it.marketing, it.userMessage, it.representativePetId))
                            ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {}, {}, {})

                            // 웰컴 페이지 호출
                            val intent = Intent(context, WelcomePageActivity::class.java)
                            SessionManager.saveUserToken(requireContext(), token)
                            response.body()?.run{
                                // nickname에 username을 넣은 것에 유의할 것
                                val account = Account(id, username, email, phone, null, marketing, username, photoUrl, userMessage, representativePetId)
                                SessionManager.saveLoggedInAccount(requireContext(), account)
                            }

                            startActivity(intent)
                            activity?.finish()
                        }
                        // 첫 로그인이 아닐 시
                        else{
                            // start main activity + send token
                            val intent = Intent(context, MainActivity::class.java)
                            SessionManager.saveUserToken(requireContext(), token)
                            response.body()?.run{
                                val account = Account(id, username, email, phone, null, marketing, nickname, photoUrl, userMessage, representativePetId)
                                SessionManager.saveLoggedInAccount(requireContext(), account)
                            }

                            startActivity(intent)
                            activity?.finish()
                        }
                    }else{
                        Toast.makeText(context, it._metadata.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<FetchAccountResDto>, t: Throwable) {
                if(isViewDestroyed) return

                unlockViews()

                // create custom snack bar to display error message
                displayErrorMessage(t.message.toString())
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun lockViews() {
        // set login button status to loading
        binding.loginButton.text = ""
        binding.loginProgressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        // disable create account, recovery buttons
        binding.createAccountButton.isEnabled = false
        binding.recoveryButton.isEnabled = false

        // disable editText
        binding.usernameEditText.isEnabled = false
        binding.pwEditText.isEnabled = false
    }

    private fun unlockViews() {
        // set login button status to active
        binding.loginButton.text = context?.getText(R.string.login_button)
        binding.loginProgressBar.visibility = View.GONE
        binding.loginButton.isEnabled = true

        // enable create account, recovery buttons
        binding.createAccountButton.isEnabled = true
        binding.recoveryButton.isEnabled = true

        // enable editText
        binding.usernameEditText.isEnabled = true
        binding.pwEditText.isEnabled = true
    }

    private fun displayErrorMessage(message: String) {
        snackBar = Snackbar.make(view?.findViewById(R.id.fragment_login_parent_layout)!!,
            message, Snackbar.LENGTH_SHORT)
        val snackBarView = snackBar!!.view
        snackBarView.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
        snackBarView.findViewById<TextView>(R.id.snackbar_text).textAlignment = View.TEXT_ALIGNMENT_CENTER
        snackBar!!.show()
    }

    private fun displaySuccessMessage(message: String) {
        snackBar = Snackbar.make(view?.findViewById(R.id.fragment_login_parent_layout)!!,
            message, Snackbar.LENGTH_SHORT)
        val snackBarView = snackBar!!.view
        snackBarView.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
        snackBarView.findViewById<TextView>(R.id.snackbar_text).textAlignment = View.TEXT_ALIGNMENT_CENTER
        snackBar!!.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true

        snackBar?.dismiss()
    }
}