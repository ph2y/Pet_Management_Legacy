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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.snackbar.Snackbar
import com.sju18001.petmanagement.MainActivity
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentLoginBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.login.createAccount.CreateAccountFragment
import com.sju18001.petmanagement.ui.login.findIdPw.FindIdPwFragment
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

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    // variable for storing API call(for cancel)
    private var loginApiCall: Call<LoginResDto>? = null
    private var profileLookupApiCall: Call<AccountProfileLookupResponseDto>? = null

    // Snackbar variable(for dismiss)
    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

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

        // for pw text change listener
        binding.pwEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginViewModel.loginPwEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for login button
        binding.loginButton.setOnClickListener {
            // disable buttons
            disableButtons()

            // hide keyboard
            Util().hideKeyboard(requireActivity())

            // call login function
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

        // for find id pw button
        binding.findIdPwButton.setOnClickListener {
            val findIdPwFragment = FindIdPwFragment()
            activity?.supportFragmentManager?.beginTransaction()!!
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.login_activity_fragment_container, findIdPwFragment)
                .addToBackStack(null)
                .commit()
        }

        // hide keyboard when touch loginLayout
        binding.fragmentLoginLayout.setOnClickListener{ Util().hideKeyboard(requireActivity()) }
    }

    private fun login(username: String, password: String) {
        // create login request Dto
        val accountLoginRequestDto = LoginReqDto(username, password)

        // call API using Retrofit
        loginApiCall = RetrofitBuilder.getServerApi().loginReq(accountLoginRequestDto)
        loginApiCall!!.enqueue(object: Callback<LoginResDto> {
            override fun onResponse(
                call: Call<LoginResDto>,
                response: Response<LoginResDto>
            ) {
                if(response.isSuccessful) {
                    checkIsFirstLoginAndSwitchActivity(response.body()!!.token)
                }
                else {
                    // create custom snack bar to display error message
                    displayErrorMessage(context?.getText(R.string.login_failed)!!.toString())

                    // enable buttons
                    enableButtons()
                }
            }

            override fun onFailure(call: Call<LoginResDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> do nothing
                if(_binding == null) { return }

                // create custom snack bar to display error message
                displayErrorMessage(t.message.toString())

                // enable buttons
                enableButtons()

                // log error message
                Log.d("error", t.message.toString())
            }
        })
    }

    // TODO: apply Hanbit-Kang's code when merging(login currently does not work due to this function)
    // 첫 로그인인지 체킹 후 액티비티 전환
    private fun checkIsFirstLoginAndSwitchActivity(token: String){
        val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

        profileLookupApiCall = RetrofitBuilder.getServerApiWithToken(token).profileLookupRequest(body)
        profileLookupApiCall!!.enqueue(object: Callback<AccountProfileLookupResponseDto> {
            override fun onResponse(
                call: Call<AccountProfileLookupResponseDto>,
                response: Response<AccountProfileLookupResponseDto>
            ) {
                if(response.isSuccessful){
                    // 첫 로그인일 시
                    if(response.body()!!.photo.isNullOrEmpty()){
                        // photo -> default, nickname -> username
                        val accountProfileUpdateRequestDto = AccountProfileUpdateRequestDto(
                            response.body()!!.username, response.body()!!.email, response.body()!!.username, response.body()!!.phone,
                            "default", response.body()!!.marketing, response.body()!!.userMessage
                        )

                        val call = RetrofitBuilder.getServerApiWithToken(token).profileUpdateRequest(accountProfileUpdateRequestDto)
                        call.enqueue(object: Callback<AccountProfileUpdateResponseDto> {
                            override fun onResponse(
                                call: Call<AccountProfileUpdateResponseDto>,
                                response: Response<AccountProfileUpdateResponseDto>
                            ) {
                            }

                            override fun onFailure(call: Call<AccountProfileUpdateResponseDto>, t: Throwable) {
                                Log.e("ServerUtil", t.message.toString())
                            }
                        })

                        // 웰컴 페이지 호출
                        val intent = Intent(context, WelcomePageActivity::class.java)
                        sessionManager.saveUserToken(token)

                        startActivity(intent)
                        activity?.finish()
                    }else{
                        // 첫 로그인이 아닐 시
                        // start main activity + send token
                        val intent = Intent(context, MainActivity::class.java)
                        // token is now stored in session
                        // intent.putExtra("token", response.body()?.token)
                        sessionManager.saveUserToken(token)

                        startActivity(intent)
                        activity?.finish()
                    }
                }
            }

            override fun onFailure(call: Call<AccountProfileLookupResponseDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> do nothing
                if(_binding == null) { return }

                // create custom snack bar to display error message
                displayErrorMessage(t.message.toString())

                // enable buttons
                enableButtons()

                // log error message
                Log.d("error", t.message.toString())
            }
        })
    }

    // disable buttons
    private fun disableButtons() {
        // set login button status to loading
        binding.loginButton.text = ""
        binding.loginProgressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        // disable create account, find id/pw buttons
        binding.createAccountButton.isEnabled = false
        binding.findIdPwButton.isEnabled = false
    }

    // enable buttons
    private fun enableButtons() {
        // set login button status to active
        binding.loginButton.text = context?.getText(R.string.login_button)
        binding.loginProgressBar.visibility = View.GONE
        binding.loginButton.isEnabled = true

        // enable create account, find id/pw buttons
        binding.createAccountButton.isEnabled = true
        binding.findIdPwButton.isEnabled = true
    }

    // display error message(Snackbar)
    private fun displayErrorMessage(message: String) {
        snackBar = Snackbar.make(view?.findViewById(R.id.fragment_login_layout)!!,
            message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar!!.view
        snackBarView.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
        snackBarView.findViewById<TextView>(R.id.snackbar_text).textAlignment = View.TEXT_ALIGNMENT_CENTER
        snackBar!!.show()
    }

    // display success message(Snackbar)
    private fun displaySuccessMessage(message: String) {
        snackBar = Snackbar.make(view?.findViewById(R.id.fragment_login_layout)!!,
            message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar!!.view
        snackBarView.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
        snackBarView.findViewById<TextView>(R.id.snackbar_text).textAlignment = View.TEXT_ALIGNMENT_CENTER
        snackBar!!.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // dismiss Snackbar
        snackBar?.dismiss()

        // stop api call when fragment is destroyed
        loginApiCall?.cancel()
        profileLookupApiCall?.cancel()
    }
}
