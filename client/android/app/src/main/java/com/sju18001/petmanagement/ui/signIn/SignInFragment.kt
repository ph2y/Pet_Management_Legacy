package com.sju18001.petmanagement.ui.signIn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.sju18001.petmanagement.MainActivity
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentSignInBinding
import com.sju18001.petmanagement.restapi.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInFragment : Fragment() {

    // variables for view binding
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

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

        // for sign in button
        binding.signInButton.setOnClickListener {
            // set button status to loading
            binding.signInButton.text = ""
            binding.signInProgressBar.visibility = View.VISIBLE
            binding.signInButton.isEnabled = false

            Util().hideKeyboard(requireActivity(), requireView())
            signIn(binding.idEditText.text.toString(), binding.pwEditText.text.toString())
        }

        // 키보드 
        binding.fragmentSignInLayout.setOnClickListener{
            Util().hideKeyboard(requireActivity(), binding.idEditText)
            Util().hideKeyboard(requireActivity(), binding.pwEditText)
        }
    }

    private fun signIn(username: String, password: String) {
        // create sign in request Dto
        val accountSignInRequestDto = AccountSignInRequestDto(username, password)

        // call API using Retrofit
        RetrofitBuilder.serverApi.signInRequest(accountSignInRequestDto).enqueue(object: Callback<AccountSignInResponseDto> {
            override fun onResponse(
                call: Call<AccountSignInResponseDto>,
                response: Response<AccountSignInResponseDto>
            ) {
                if(response.isSuccessful) {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("token", response.body()?.token)

                    startActivity(intent)
                }
                else {
                    // create custom snack bar to display error message
                    displayErrorMessage(context?.getText(R.string.sign_in_failed)!!.toString())

                    // set button status to active
                    binding.signInButton.text = context?.getText(R.string.sign_in_button)
                    binding.signInProgressBar.visibility = View.GONE
                    binding.signInButton.isEnabled = true
                }
            }

            override fun onFailure(call: Call<AccountSignInResponseDto>, t: Throwable) {
                // create custom snack bar to display error message
                displayErrorMessage(t.message.toString())

                // set button status to active
                binding.signInButton.text = context?.getText(R.string.sign_in_button)
                binding.signInProgressBar.visibility = View.GONE
                binding.signInButton.isEnabled = true

                // log error message
                Log.d("error", t.message.toString())
            }
        })
    }

    // display error message(Snackbar)
    private fun displayErrorMessage(message: String) {
        val snackBar = Snackbar.make(view?.findViewById(R.id.fragment_sign_in_layout)!!,
            message, Snackbar.LENGTH_INDEFINITE)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
        snackBarView.findViewById<TextView>(R.id.snackbar_text).textAlignment = View.TEXT_ALIGNMENT_CENTER
        snackBar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}