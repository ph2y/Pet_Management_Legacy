package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentAddEditPetBinding
import com.sju18001.petmanagement.databinding.FragmentEditPetFeedScheduleBinding

class EditPetFeedScheduleFragment : Fragment() {
    private var _binding: FragmentEditPetFeedScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditPetFeedScheduleBinding.inflate(inflater, container, false)

        binding.backButton.setOnClickListener {
            activity?.finish()
        }

        return binding.root
    }
}