package com.sju18001.petmanagement.ui.myPet.scheduler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentFindIdPwBinding
import com.sju18001.petmanagement.databinding.FragmentSchedulerBinding

class SchedulerFragment : Fragment() {
    private var _binding: FragmentSchedulerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSchedulerBinding.inflate(inflater, container, false)
        return binding.root
    }
}