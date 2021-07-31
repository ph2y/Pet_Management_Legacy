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

    // variable for ViewModel
    private val myPetViewModel: MyPetViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentPetProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // save pet data to ViewModel(for pet profile) if not already loaded
        if(!myPetViewModel.loadedFromIntent) { savePetDataForPetProfile() }

        // if fragment type is pet_profile_pet_manager -> hide username_and_pets_layout
        if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_pet_manager") {
            binding.usernameAndPetsLayout.visibility = View.GONE
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        // for pet update button
        binding.updatePetButton.setOnClickListener {
            // save pet data to ViewModel(for pet update)
            savePetDataForPetUpdate()

            // open update pet fragment
            activity?.supportFragmentManager?.beginTransaction()!!
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.my_pet_activity_fragment_container, CreateUpdatePetFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()

        // set views with data from ViewModel
        setViewsWithPetData()
    }

    private fun savePetDataForPetProfile() {
        myPetViewModel.loadedFromIntent = true
        myPetViewModel.petImageValueProfile = requireActivity().intent.getIntExtra("petImage", R.drawable.sample1)
        myPetViewModel.petNameValueProfile = requireActivity().intent.getStringExtra("petName").toString()
        myPetViewModel.petBirthValueProfile = requireActivity().intent.getStringExtra("petBirth").toString()
        myPetViewModel.petSpeciesValueProfile = requireActivity().intent.getStringExtra("petSpecies").toString()
        myPetViewModel.petBreedValueProfile = requireActivity().intent.getStringExtra("petBreed").toString()
        myPetViewModel.petGenderValueProfile = requireActivity().intent.getStringExtra("petGender").toString()
        myPetViewModel.petAgeValueProfile = requireActivity().intent.getStringExtra("petAge").toString()
        myPetViewModel.petMessageValueProfile = requireActivity().intent.getStringExtra("petMessage").toString()
    }

    private fun setViewsWithPetData() {
        myPetViewModel.petImageValueProfile?.let { binding.petImage.setImageResource(it) }
        binding.petName.text = myPetViewModel.petNameValueProfile
        binding.petBirth.text = myPetViewModel.petBirthValueProfile
        binding.petSpeciesAndBreed.text = myPetViewModel.petSpeciesValueProfile + myPetViewModel.petBreedValueProfile
        binding.petGenderAndAge.text = myPetViewModel.petGenderValueProfile + myPetViewModel.petAgeValueProfile
        binding.petMessage.text = myPetViewModel.petMessageValueProfile
    }

    private fun savePetDataForPetUpdate() {
        myPetViewModel.petIdValue = requireActivity().intent.getLongExtra("petId", -1)
        myPetViewModel.petImageValue = null
        myPetViewModel.petMessageValue = myPetViewModel.petMessageValueProfile
        myPetViewModel.petNameValue = myPetViewModel.petNameValueProfile
        myPetViewModel.petGenderValue = myPetViewModel.petGenderValueProfile == "♀"
        myPetViewModel.petSpeciesValue = myPetViewModel.petSpeciesValueProfile
        myPetViewModel.petBreedValue = myPetViewModel.petBreedValueProfile
        myPetViewModel.petBirthIsYearOnlyValue = myPetViewModel.petBirthValueProfile.length == 6
        myPetViewModel.petBirthYearValue = myPetViewModel.petBirthValueProfile.substring(0, 4).toInt()
        if(!myPetViewModel.petBirthIsYearOnlyValue) {
            myPetViewModel.petBirthMonthValue = myPetViewModel.petBirthValueProfile.substring(6, 8).toInt()
            myPetViewModel.petBirthDateValue = myPetViewModel.petBirthValueProfile.substring(10, 12).toInt()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}