package com.sju18001.petmanagement.ui.signIn.findIdPw

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentFindIdBinding

class FindIdFragment : Fragment(){
    private var _binding: FragmentFindIdBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFindIdBinding.inflate(inflater, container, false)
        return binding.root
    }
}