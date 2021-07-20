package com.sju18001.petmanagement.ui.welcomePage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentWelcomePageProfileBinding

class WelcomePageProfileFragment : Fragment() {
    private var _binding: FragmentWelcomePageProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWelcomePageProfileBinding.inflate(layoutInflater)
        return binding.root
    }
}