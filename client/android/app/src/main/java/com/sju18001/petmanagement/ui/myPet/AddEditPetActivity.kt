package com.sju18001.petmanagement.ui.myPet

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sju18001.petmanagement.R
import de.hdodenhof.circleimageview.CircleImageView

// constant variables
private const val PICK_IMAGE = 0

class AddEditPetActivity : AppCompatActivity() {
    // create view variables
    private lateinit var petImageInput: CircleImageView
    private lateinit var petNameInput: EditText
    private lateinit var petSpeciesInput: EditText
    private lateinit var petBreedInput: EditText
    private lateinit var petBirthInput: DatePicker
    private lateinit var confirmButton: Button
    private lateinit var backButton: ImageView

    // create user value variables
    private var petImageValue: Uri? = null
    private var petNameValue: String? = null
    private var petGenderValue: Boolean? = null // [false -> Male / true -> Female]
    private var petSpeciesValue: String? = null
    private var petBreedValue: String? = null
    private var petBirthYearValue: Int? = null
    private var petBirthMonthValue: Int? = null
    private var petBirthDateValue: Int? = null
    private var petBirthYearOnlyValue: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_pet)

        // initialize view variables
        petImageInput = findViewById(R.id.pet_image_input)
        petNameInput = findViewById(R.id.pet_name_input)
        petSpeciesInput = findViewById(R.id.pet_species_input)
        petBreedInput = findViewById(R.id.pet_breed_input)
        petBirthInput = findViewById(R.id.pet_birth_input)
        confirmButton = findViewById(R.id.confirm_button)
        backButton = findViewById(R.id.back_button)

        // for pet image picker
        petImageInput.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_IMAGE)
        }

        // save text/Int values when the confirm button is pressed
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

            // for testing
            Log.d("AA", "image: " + petImageValue.toString())
            Log.d("AA", "name: " + petNameValue.toString())
            Log.d("AA", "gender: " + petGenderValue.toString())
            Log.d("AA", "species: " + petSpeciesValue.toString())
            Log.d("AA", "breed: " + petBreedValue.toString())
            Log.d("AA", "year: " + petBirthYearValue.toString())
            Log.d("AA", "month: " + petBirthMonthValue.toString())
            Log.d("AA", "date: " + petBirthDateValue.toString())
            Log.d("AA", "year only: " + petBirthYearOnlyValue.toString())
        }

        // for back button
        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // get + save pet image value
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            if (data != null) {
                petImageValue = data.data
                petImageInput.setImageURI(data.data)
            }
        }
    }

    // function for gender RadioButtons
    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // for checking if it is clicked
            val checked = view.isChecked

            // save pet gender value
            when (view.getId()) {
                R.id.gender_male ->
                    if (checked) {
                        petGenderValue = false
                    }
                R.id.gender_female ->
                    if (checked) {
                        petGenderValue = true
                    }
            }
        }
    }

    // function for year only CheckBox
    fun onCheckboxClicked(view: View) {
        if (view is CheckBox) {
            // see if it is checked
            val checked: Boolean = view.isChecked

            // save year only value
            petBirthYearOnlyValue = checked
        }
    }
}