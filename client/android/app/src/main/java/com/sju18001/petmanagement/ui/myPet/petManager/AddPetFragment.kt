package com.sju18001.petmanagement.ui.myPet.petManager

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentAddEditPetBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.PetProfileCreateRequestDto
import com.sju18001.petmanagement.restapi.dto.PetProfileCreateResponseDto
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AddPetFragment : Fragment() {

    // constant variables
    private val PICK_IMAGE = 0

    // variables for view binding
    private var _binding: FragmentAddEditPetBinding? = null
    private val binding get() = _binding!!

    // variable for ViewModel
    val myPetViewModel: MyPetViewModel by activityViewModels()

    // variable for storing API call(for cancel)
    private var petProfileCreateApiCall: Call<PetProfileCreateResponseDto>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentAddEditPetBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        // for DatePicker
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        binding.petBirthInput.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            myPetViewModel.petBirthYearValue = year
            myPetViewModel.petBirthMonthValue = monthOfYear
            myPetViewModel.petBirthDateValue = dayOfMonth
        }

        // for view restore
        restoreState()

        // for pet image picker
        binding.petImageInputButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_IMAGE)
        }

        // for EditText text change listeners
        binding.petMessageInput.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myPetViewModel.petMessageValue = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.petNameInput.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myPetViewModel.petNameValue = s.toString()
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.petSpeciesInput.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myPetViewModel.petSpeciesValue = s.toString()
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.petBreedInput.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myPetViewModel.petBreedValue = s.toString()
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for gender RadioButtons
        binding.genderFemale.setOnClickListener{
            if(binding.genderFemale.isChecked) {
                myPetViewModel.petGenderValue = true
                checkIsValid()
            }
        }
        binding.genderMale.setOnClickListener{
            if(binding.genderMale.isChecked) {
                myPetViewModel.petGenderValue = false
                checkIsValid()
            }
        }

        // for year only CheckBox
        binding.yearOnlyCheckbox.setOnClickListener{
            myPetViewModel.petBirthIsYearOnlyValue = binding.yearOnlyCheckbox.isChecked
        }

        // for confirm button
        binding.confirmButton.setOnClickListener {
            // set api state/button to loading
            myPetViewModel.addPetApiIsLoading = true
            setButtonToLoading()

            // for birth value
            val petBirthStringValue: String = if (!binding.yearOnlyCheckbox.isChecked){
                if(binding.petBirthInput.month < 10) {
                    "${binding.petBirthInput.year}-0${binding.petBirthInput.month}-${binding.petBirthInput.dayOfMonth}"
                } else {
                    "${binding.petBirthInput.year}-${binding.petBirthInput.month}-${binding.petBirthInput.dayOfMonth}"
                }
            } else {
                "${binding.petBirthInput.year}-01-01"
            }

            // for gender value
            val petGenderValue = binding.genderFemale.isChecked

            // create DTO
            val petProfileCreateRequestDto = PetProfileCreateRequestDto(
                binding.petNameInput.text.toString(),
                binding.petSpeciesInput.text.toString(),
                binding.petBreedInput.text.toString(),
                petBirthStringValue,
                petGenderValue,
                binding.petMessageInput.text.toString(),
                null
            )

            petProfileCreateApiCall = RetrofitBuilder.getServerApi().petProfileCreateRequest(token = "Bearer ${sessionManager.fetchUserToken()!!}", petProfileCreateRequestDto)
            petProfileCreateApiCall!!.enqueue(object: Callback<PetProfileCreateResponseDto> {
                override fun onResponse(
                    call: Call<PetProfileCreateResponseDto>,
                    response: Response<PetProfileCreateResponseDto>
                ) {
                    if(response.isSuccessful) {
                        // set api state/button to normal
                        myPetViewModel.addPetApiIsLoading = false
                        setButtonToNormal()

                        Toast.makeText(context, context?.getText(R.string.add_pet_successful), Toast.LENGTH_LONG).show()
                        activity?.finish()
                    }
                    else {
                        // set api state/button to normal
                        myPetViewModel.addPetApiIsLoading = false
                        setButtonToNormal()

                        // get error message(overlap)
                        val errorMessage = JSONObject(response.errorBody()!!.string().trim()).getString("message")

                        // show error message(toast)
                        Log.d("error", errorMessage)
                    }
                }

                override fun onFailure(call: Call<PetProfileCreateResponseDto>, t: Throwable) {
                    // if the view was destroyed(API call canceled) -> return
                    if(_binding == null) {
                        return
                    }

                    // set api state/button to normal
                    myPetViewModel.addPetApiIsLoading = false
                    setButtonToNormal()

                    Log.d("error", t.message.toString())
                }
            })
        }

        // for back button
        binding.backButton.setOnClickListener {
            activity?.finish()
        }
    }

    // set button to loading
    private fun setButtonToLoading() {
        binding.confirmButton.visibility = View.GONE
        binding.addPetProgressBar.visibility = View.VISIBLE
    }

    // set button to normal
    private fun setButtonToNormal() {
        binding.confirmButton.visibility = View.VISIBLE
        binding.addPetProgressBar.visibility = View.GONE
    }

    // for valid check
    private fun checkIsValid() {
        // if valid -> enable confirm button
        binding.confirmButton.isEnabled = myPetViewModel.petNameValue != "" && myPetViewModel.petGenderValue != null &&
                myPetViewModel.petSpeciesValue != "" && myPetViewModel.petBreedValue != ""
    }

    // for loading check
    private fun checkIsLoading() {
        // if loading -> set button to loading
        if(myPetViewModel.addPetApiIsLoading) {
            setButtonToLoading()
        }
        else {
            setButtonToNormal()
        }
    }

    // for restoring views
    private fun restoreState() {
        if(myPetViewModel.petImageValue != null) {
            binding.petImageInput.setImageURI(myPetViewModel.petImageValue)
        }
        binding.petNameInput.setText(myPetViewModel.petNameValue)
        binding.petMessageInput.setText(myPetViewModel.petMessageValue)
        if(myPetViewModel.petGenderValue != null) {
            if(myPetViewModel.petGenderValue == true) {
                binding.genderFemale.isSelected = true
                binding.genderMale.isSelected = false
            }
            else {
                binding.genderFemale.isSelected = false
                binding.genderMale.isSelected = true
            }
        }
        binding.petSpeciesInput.setText(myPetViewModel.petSpeciesValue)
        binding.petBreedInput.setText(myPetViewModel.petBreedValue)
        if(myPetViewModel.petBirthYearValue != null) {
            binding.petBirthInput.updateDate(myPetViewModel.petBirthYearValue!!,
                myPetViewModel.petBirthMonthValue!!, myPetViewModel.petBirthDateValue!!)
        }
        binding.yearOnlyCheckbox.isChecked = myPetViewModel.petBirthIsYearOnlyValue

        checkIsValid()
        checkIsLoading()
    }

    // for image select
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // get + save pet image value
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_IMAGE){
            if (data != null) {
                myPetViewModel.petImageValue = data.data
                binding.petImageInput.setImageURI(data.data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // stop api call when fragment is destroyed
        petProfileCreateApiCall?.cancel()
        myPetViewModel.addPetApiIsLoading = false
    }
}