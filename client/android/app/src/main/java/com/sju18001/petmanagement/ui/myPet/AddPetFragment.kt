package com.sju18001.petmanagement.ui.myPet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentAddEditPetBinding
import de.hdodenhof.circleimageview.CircleImageView

class AddPetFragment : Fragment() {

    // constant variables
    private val PICK_IMAGE = 0

    // create user value variables
    private var petImageValue: Uri? = null
    private var petNameValue: String? = null
    private var petGenderValue: Boolean? = null // [true -> Female / false -> Male]
    private var petSpeciesValue: String? = null
    private var petBreedValue: String? = null
    private var petBirthYearValue: Int? = null
    private var petBirthMonthValue: Int? = null
    private var petBirthDateValue: Int? = null
    private var petBirthYearOnlyValue: Boolean = false

    // variables for view binding
    private var _binding: FragmentAddEditPetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentAddEditPetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // for pet image picker
        binding.petImageInputButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_IMAGE)
        }

        // for gender RadioButtons
        binding.genderFemale.setOnClickListener{ petGenderValue = true }
        binding.genderMale.setOnClickListener{ petGenderValue = false }

        // for year only CheckBox
        binding.yearOnlyCheckbox.setOnClickListener{ petBirthYearOnlyValue = binding.yearOnlyCheckbox.isChecked }

        // save the rest of the values when the confirm button is pressed
        binding.confirmButton.setOnClickListener {
            petNameValue = binding.petNameInput.text.toString()
            petSpeciesValue = binding.petSpeciesInput.text.toString()
            petBreedValue = binding.petBreedInput.text.toString()
            petBirthYearValue = binding.petBirthInput.year
            if (!petBirthYearOnlyValue){
                petBirthMonthValue = binding.petBirthInput.month
                petBirthDateValue = binding.petBirthInput.dayOfMonth
            }
            else {
                petBirthMonthValue = null
                petBirthDateValue = null
            }


            //
            //for testing #######################################################################
            Log.d("values", "image: " + petImageValue.toString())
            Log.d("values", "name: " + petNameValue.toString())
            Log.d("values", "gender: " + petGenderValue.toString())
            Log.d("values", "species: " + petSpeciesValue.toString())
            Log.d("values", "breed: " + petBreedValue.toString())
            Log.d("values", "year: " + petBirthYearValue.toString())
            Log.d("values", "month: " + petBirthMonthValue.toString())
            Log.d("values", "date: " + petBirthDateValue.toString())
            Log.d("values", "year only: " + petBirthYearOnlyValue.toString())
            //for testing #######################################################################
            //


        }

        // for back button
        binding.backButton.setOnClickListener {
            activity?.finish()
        }
    }

    // for image select
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // get + save pet image value
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_IMAGE){
            if (data != null) {
                petImageValue = data.data
                binding.petImageInput.setImageURI(data.data)
            }
        }
    }
}