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
import de.hdodenhof.circleimageview.CircleImageView

class AddPetFragment : Fragment() {
    // constant variables
    private val PICK_IMAGE = 0

    // create view variables
    private lateinit var petImageInput: CircleImageView
    private lateinit var petImageInputButton: CircleImageView
    private lateinit var petNameInput: EditText
    private lateinit var petGenderFemaleRadioButton: RadioButton
    private lateinit var petGenderMaleRadioButton: RadioButton
    private lateinit var petSpeciesInput: EditText
    private lateinit var petBreedInput: EditText
    private lateinit var petBirthInput: DatePicker
    private lateinit var confirmButton: Button
    private lateinit var backButton: ImageView
    private lateinit var yearOnlyCheckBox: CheckBox

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_edit_pet, container, false)

        // initialize view variables
        petImageInput = view.findViewById(R.id.pet_image_input)
        petImageInputButton = view.findViewById(R.id.pet_image_input_button)
        petNameInput = view.findViewById(R.id.pet_name_input)
        petGenderFemaleRadioButton = view.findViewById(R.id.gender_female)
        petGenderMaleRadioButton = view.findViewById(R.id.gender_male)
        petSpeciesInput = view.findViewById(R.id.pet_species_input)
        petBreedInput = view.findViewById(R.id.pet_breed_input)
        petBirthInput = view.findViewById(R.id.pet_birth_input)
        confirmButton = view.findViewById(R.id.confirm_button)
        backButton = view.findViewById(R.id.back_button)
        yearOnlyCheckBox = view.findViewById(R.id.year_only_checkbox)

        return view
    }

    override fun onStart() {
        super.onStart()

        // for pet image picker
        petImageInputButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_IMAGE)
        }

        // for gender RadioButtons
        petGenderFemaleRadioButton.setOnClickListener{ petGenderValue = true }
        petGenderMaleRadioButton.setOnClickListener{ petGenderValue = false }

        // for year only CheckBox
        yearOnlyCheckBox.setOnClickListener{ petBirthYearOnlyValue = yearOnlyCheckBox.isChecked }

        // save the rest of the values when the confirm button is pressed
        confirmButton.setOnClickListener {
            petNameValue = petNameInput.text.toString()
            petSpeciesValue = petSpeciesInput.text.toString()
            petBreedValue = petBreedInput.text.toString()
            petBirthYearValue = petBirthInput.year
            if (!petBirthYearOnlyValue){
                petBirthMonthValue = petBirthInput.month
                petBirthDateValue = petBirthInput.dayOfMonth
            }
            else {
                petBirthMonthValue = null
                petBirthDateValue = null
            }



            //
            //for testing #######################################################################
            Log.d("AA", "image: " + petImageValue.toString())
            Log.d("AA", "name: " + petNameValue.toString())
            Log.d("AA", "gender: " + petGenderValue.toString())
            Log.d("AA", "species: " + petSpeciesValue.toString())
            Log.d("AA", "breed: " + petBreedValue.toString())
            Log.d("AA", "year: " + petBirthYearValue.toString())
            Log.d("AA", "month: " + petBirthMonthValue.toString())
            Log.d("AA", "date: " + petBirthDateValue.toString())
            Log.d("AA", "year only: " + petBirthYearOnlyValue.toString())
            //for testing #######################################################################
            //


        }

        // for back button
        backButton.setOnClickListener {
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
                petImageInput.setImageURI(data.data)
            }
        }
    }
}