package com.sju18001.petmanagement.ui.myPet.petManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentPetProfileBinding
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel

class PetProfileFragment : Fragment(){

    // variables for view binding
    private var _binding: FragmentPetProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentPetProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // set views
        setViewsWithPetData()

        return view
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setViewsWithPetData() {
        binding.petImage.setImageResource(requireActivity().intent.getIntExtra("petImage", R.drawable.sample1))
        binding.petName.text = requireActivity().intent.getStringExtra("petName")
        binding.petBirth.text = requireActivity().intent.getStringExtra("petBirth")
        val speciesAndBreed = requireActivity().intent.getStringExtra("petSpecies") +
                requireActivity().intent.getStringExtra("petBreed")
        binding.petSpeciesAndBreed.text = speciesAndBreed
        val genderAndAge = requireActivity().intent.getStringExtra("petGender") +
                requireActivity().intent.getStringExtra("petAge")
        binding.petGenderAndAge.text = genderAndAge
        binding.petMessage.text = requireActivity().intent.getStringExtra("petMessage")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}