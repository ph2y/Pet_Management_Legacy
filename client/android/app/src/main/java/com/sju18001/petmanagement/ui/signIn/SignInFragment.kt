package com.sju18001.petmanagement.ui.signIn

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sju18001.petmanagement.MainActivity
import com.sju18001.petmanagement.R

class SignInFragment : Fragment() {

    // create view variables
    private lateinit var idEditText: EditText
    private lateinit var pwEditText: EditText
    private lateinit var signInButton: Button
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
        registerButton = view.findViewById(R.id.register_button)
        idPwFindButton = view.findViewById(R.id.id_pw_find_button)

        return view
    }

    override fun onStart() {
        super.onStart()

        //
//        idPwFindButton.setOnClickListener {
//            startActivity(Intent(context, MainActivity::class.java))
//        }
    }
}