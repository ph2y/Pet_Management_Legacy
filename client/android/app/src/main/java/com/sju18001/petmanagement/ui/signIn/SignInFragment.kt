package com.sju18001.petmanagement.ui.signIn

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
import com.sju18001.petmanagement.controller.ServerUtil
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentSignInBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.AccountProfileLookupResponseDto
import com.sju18001.petmanagement.restapi.dto.AccountProfileUpdateRequestDto
import com.sju18001.petmanagement.restapi.dto.AccountSignInRequestDto
import com.sju18001.petmanagement.restapi.dto.AccountSignInResponseDto
import com.sju18001.petmanagement.ui.signIn.signUp.SignUpFragment
import com.sju18001.petmanagement.ui.signIn.findIdPw.FindIdPwFragment
import com.sju18001.petmanagement.ui.welcomePage.WelcomePageActivity
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInFragment : Fragment() {

    // variables for view binding
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    // variable for storing API call(for cancel)
    private var signInApiCall: Call<AccountSignInResponseDto>? = null
    private var profileLookupApiCall: Call<AccountProfileLookupResponseDto>? = null

    // Snackbar variable(for dismiss)
    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        // get sign up result
        setFragmentResultListener("signUpResult") { _, bundle ->
            val result: Boolean = bundle.get("isSuccessful") as Boolean

            // if successful -> show success message
            if(result) {
                displaySuccessMessage(context?.getText(R.string.sign_up_success)!!.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // variable for ViewModel
        val signInViewModel: SignInViewModel by activityViewModels()

        // restore EditText values after view destruction
        binding.idEditText.setText(signInViewModel.signInIdEditText)
        binding.pwEditText.setText(signInViewModel.signInPwEditText)

        // reset sign up values in ViewModel
        signInViewModel.resetSignUpValues()

        // for id text change listener
        binding.idEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                signInViewModel.signInIdEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for pw text change listener
        binding.pwEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                signInViewModel.signInPwEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for sign in button
        binding.signInButton.setOnClickListener {
            // disable buttons
            disableButtons()

            // hide keyboard
            Util().hideKeyboard(requireActivity())

            // call signIn function
            signIn(binding.idEditText.text.toString(), binding.pwEditText.text.toString())
        }

        // for sign up button
        binding.signUpButton.setOnClickListener {
            val signUpFragment = SignUpFragment()
            activity?.supportFragmentManager?.beginTransaction()!!
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.sign_in_activity_fragment_container, signUpFragment)
                .addToBackStack(null)
                .commit()
        }

        // for find id pw button
        binding.findIdPwButton.setOnClickListener {
            val findIdPwFragment = FindIdPwFragment()
            activity?.supportFragmentManager?.beginTransaction()!!
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.sign_in_activity_fragment_container, findIdPwFragment)
                .addToBackStack(null)
                .commit()
        }

        // hide keyboard when touch signInLayout
        binding.fragmentSignInLayout.setOnClickListener{ Util().hideKeyboard(requireActivity()) }
    }

    private fun signIn(username: String, password: String) {
        // create sign in request Dto
        val accountSignInRequestDto = AccountSignInRequestDto(username, password)

        // call API using Retrofit
        signInApiCall = RetrofitBuilder.getServerApi().signInRequest(accountSignInRequestDto)
        signInApiCall!!.enqueue(object: Callback<AccountSignInResponseDto> {
            override fun onResponse(
                call: Call<AccountSignInResponseDto>,
                response: Response<AccountSignInResponseDto>
            ) {
                if(response.isSuccessful) {
                    checkIsFirstLoginAndSwitchActivity(response.body()!!.token)
                }
                else {
                    // create custom snack bar to display error message
                    displayErrorMessage(context?.getText(R.string.sign_in_failed)!!.toString())

                    // enable buttons
                    enableButtons()
                }
            }

            override fun onFailure(call: Call<AccountSignInResponseDto>, t: Throwable) {
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
                        ServerUtil.updateProfile(
                            token,
                            AccountProfileUpdateRequestDto(
                                response.body()!!.username, response.body()!!.email, response.body()!!.username, response.body()!!.phone,
                                "default", response.body()!!.marketing, response.body()!!.userMessage
                            )
                        )

                        // 웰컴 페이지 호출
                        val intent = Intent(context, WelcomePageActivity::class.java)

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
        // set sign in button status to loading
        binding.signInButton.text = ""
        binding.signInProgressBar.visibility = View.VISIBLE
        binding.signInButton.isEnabled = false

        // disable sign up, find id/pw buttons
        binding.signUpButton.isEnabled = false
        binding.findIdPwButton.isEnabled = false
    }

    // enable buttons
    private fun enableButtons() {
        // set sign in button status to active
        binding.signInButton.text = context?.getText(R.string.sign_in_button)
        binding.signInProgressBar.visibility = View.GONE
        binding.signInButton.isEnabled = true

        // enable sign up, find id/pw buttons
        binding.signUpButton.isEnabled = true
        binding.findIdPwButton.isEnabled = true
    }

    // display error message(Snackbar)
    private fun displayErrorMessage(message: String) {
        snackBar = Snackbar.make(view?.findViewById(R.id.fragment_sign_in_layout)!!,
            message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar!!.view
        snackBarView.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
        snackBarView.findViewById<TextView>(R.id.snackbar_text).textAlignment = View.TEXT_ALIGNMENT_CENTER
        snackBar!!.show()
    }

    // display success message(Snackbar)
    private fun displaySuccessMessage(message: String) {
        snackBar = Snackbar.make(view?.findViewById(R.id.fragment_sign_in_layout)!!,
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
        signInApiCall?.cancel()
        profileLookupApiCall?.cancel()
    }
}
