package com.sju18001.petmanagement.ui.signIn

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.sju18001.petmanagement.MainActivity
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SignInFragment : Fragment() {

    // create view variables
    private lateinit var idEditText: EditText
    private lateinit var pwEditText: EditText
    private lateinit var signInButton: Button
    private lateinit var signInButtonProgressBar: ProgressBar
    private lateinit var registerButton: TextView
    private lateinit var idPwFindButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        // initialize view variables
        idEditText = view.findViewById(R.id.id_edit_text)
        pwEditText = view.findViewById(R.id.pw_edit_text)
        signInButton = view.findViewById(R.id.sign_in_button)
        signInButtonProgressBar = view.findViewById(R.id.sign_in_progress_bar)
        registerButton = view.findViewById(R.id.register_button)
        idPwFindButton = view.findViewById(R.id.id_pw_find_button)

        return view
    }

    override fun onStart() {
        super.onStart()

        // for sign in button
        signInButton.setOnClickListener {
            // set button status to loading
            signInButton.text = ""
            signInButtonProgressBar.visibility = View.VISIBLE
            signInButton.isEnabled = false

            Util().hideKeyboard(requireActivity(), requireView())
            signIn(idEditText.text.toString(), pwEditText.text.toString())
        }
    }

    private fun signIn(username: String, password: String) {
        // create sign in request DTO
        val accountSignInRequestDTO = AccountSignInRequestDTO(username, password)

        // call API using Retrofit
        RetrofitBuilder.accountApi.signInRequest(accountSignInRequestDTO).enqueue(object: Callback<AccountSignInResponseDTO> {
            override fun onResponse(
                call: Call<AccountSignInResponseDTO>,
                response: Response<AccountSignInResponseDTO>
            ) {
                if(response.isSuccessful) {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("token", response.body()?.token)

                    startActivity(intent)
                }
                else {
                    // create custom snack bar to display error message
                    val snackBar = Snackbar.make(view?.findViewById(R.id.fragment_sign_in_layout)!!,
                        context?.getText(R.string.sign_in_failed)!!, Snackbar.LENGTH_INDEFINITE)
                    val snackBarView = snackBar.view
                    snackBarView.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
                    snackBarView.findViewById<TextView>(R.id.snackbar_text).textAlignment = View.TEXT_ALIGNMENT_CENTER
                    snackBar.show()

                    // set button status to active
                    signInButton.text = context?.getText(R.string.sign_in_button)
                    signInButtonProgressBar.visibility = View.GONE
                    signInButton.isEnabled = true
                }
            }

            override fun onFailure(call: Call<AccountSignInResponseDTO>, t: Throwable) {
                // create custom snack bar to display error message
                val snackBar = Snackbar.make(view?.findViewById(R.id.fragment_sign_in_layout)!!,
                    t.message.toString(), Snackbar.LENGTH_INDEFINITE)
                val snackBarView = snackBar.view
                snackBarView.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
                snackBarView.findViewById<TextView>(R.id.snackbar_text).textAlignment = View.TEXT_ALIGNMENT_CENTER
                snackBar.show()

                // set button status to active
                signInButton.text = context?.getText(R.string.sign_in_button)
                signInButtonProgressBar.visibility = View.GONE
                signInButton.isEnabled = true

                // log error message
                Log.d("error", t.message.toString())
            }
        })
    }
}