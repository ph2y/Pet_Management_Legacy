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
import com.sju18001.petmanagement.databinding.FragmentCreateUpdatePetBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.PetProfileCreateRequestDto
import com.sju18001.petmanagement.restapi.dto.PetProfileCreateResponseDto
import com.sju18001.petmanagement.restapi.dto.PetProfileUpdateRequestDto
import com.sju18001.petmanagement.restapi.dto.PetProfileUpdateResponseDto
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.util.*

class CreateUpdatePetFragment : Fragment() {

    // constant variables
    private val PICK_IMAGE = 0

    // variables for view binding
    private var _binding: FragmentCreateUpdatePetBinding? = null
    private val binding get() = _binding!!

    // variable for ViewModel
    val myPetViewModel: MyPetViewModel by activityViewModels()

    // variables for storing API call(for cancel)
    private var petProfileCreateApiCall: Call<PetProfileCreateResponseDto>? = null
    private var petProfileUpdateApiCall: Call<PetProfileUpdateResponseDto>? = null

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
        _binding = FragmentCreateUpdatePetBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        // for DatePicker
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        if(myPetViewModel.petBirthYearValue == null) { myPetViewModel.petBirthYearValue = calendar.get(Calendar.YEAR) }
        if(myPetViewModel.petBirthMonthValue == null) { myPetViewModel.petBirthMonthValue = calendar.get(Calendar.MONTH) }
        if(myPetViewModel.petBirthDateValue == null) { myPetViewModel.petBirthDateValue = calendar.get(Calendar.DAY_OF_MONTH) }
        binding.petBirthInput.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
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
            if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_pet_manager") {
                updatePet()
            }
            else {
                createPet()
            }
        }

        // for back button
        binding.backButton.setOnClickListener {
            if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_pet_manager") {
                fragmentManager?.popBackStack()
            }
            else {
                activity?.finish()
            }
        }
    }

    // create pet
    private fun createPet() {
        // set api state/button to loading
        myPetViewModel.createUpdateDeletePetApiIsLoading = true
        setButtonToLoading()

        // for birth value
        val petBirthStringValue: String = if (!binding.yearOnlyCheckbox.isChecked){
            "${binding.petBirthInput.year}-${(binding.petBirthInput.month + 1).toString().padStart(2, '0')}" +
                    "-${binding.petBirthInput.dayOfMonth.toString().padStart(2, '0')}"
        } else {
            "${binding.petBirthInput.year}-01-01"
        }

        // create DTO
        val petProfileCreateRequestDto = PetProfileCreateRequestDto(
            binding.petNameInput.text.toString(),
            binding.petSpeciesInput.text.toString(),
            binding.petBreedInput.text.toString(),
            petBirthStringValue,
            binding.yearOnlyCheckbox.isChecked,
            binding.genderFemale.isChecked,
            binding.petMessageInput.text.toString(),
            null
        )

        petProfileCreateApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .petProfileCreateRequest(petProfileCreateRequestDto)
        petProfileCreateApiCall!!.enqueue(object: Callback<PetProfileCreateResponseDto> {
            override fun onResponse(
                call: Call<PetProfileCreateResponseDto>,
                response: Response<PetProfileCreateResponseDto>
            ) {
                if(response.isSuccessful) {
                    // set api state/button to normal
                    myPetViewModel.createUpdateDeletePetApiIsLoading = false
                    setButtonToNormal()

                    Toast.makeText(context, context?.getText(R.string.create_pet_successful), Toast.LENGTH_LONG).show()
                    activity?.finish()
                }
                else {
                    // set api state/button to normal
                    myPetViewModel.createUpdateDeletePetApiIsLoading = false
                    setButtonToNormal()

                    // get error message + show(Toast)
                    val errorMessage = JSONObject(response.errorBody()!!.string().trim()).getString("message")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<PetProfileCreateResponseDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> return
                if(_binding == null) {
                    return
                }

                // set api state/button to normal
                myPetViewModel.createUpdateDeletePetApiIsLoading = false
                setButtonToNormal()

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    // update pet
    private fun updatePet() {
        // set api state/button to loading
        myPetViewModel.createUpdateDeletePetApiIsLoading = true
        setButtonToLoading()

        // for birth value
        val petBirthStringValue: String = if (!binding.yearOnlyCheckbox.isChecked){
            "${binding.petBirthInput.year}-${(binding.petBirthInput.month + 1).toString().padStart(2, '0')}" +
                    "-${binding.petBirthInput.dayOfMonth.toString().padStart(2, '0')}"
        } else {
            "${binding.petBirthInput.year}-01-01"
        }

        // create DTO
        val petProfileUpdateRequestDto = PetProfileUpdateRequestDto(
            myPetViewModel.petIdValue!!,
            binding.petNameInput.text.toString(),
            binding.petSpeciesInput.text.toString(),
            binding.petBreedInput.text.toString(),
            petBirthStringValue,
            binding.yearOnlyCheckbox.isChecked,
            binding.genderFemale.isChecked,
            binding.petMessageInput.text.toString(),
            null
        )

        petProfileUpdateApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .petProfileUpdateRequest(petProfileUpdateRequestDto)
        petProfileUpdateApiCall!!.enqueue(object: Callback<PetProfileUpdateResponseDto> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<PetProfileUpdateResponseDto>,
                response: Response<PetProfileUpdateResponseDto>
            ) {
                if(response.isSuccessful) {
                    // set api state/button to normal
                    myPetViewModel.createUpdateDeletePetApiIsLoading = false
                    setButtonToNormal()

                    Toast.makeText(context, context?.getText(R.string.update_pet_successful), Toast.LENGTH_LONG).show()
                    savePetDataForPetProfile()
                    fragmentManager?.popBackStack()
                }
                else {
                    // set api state/button to normal
                    myPetViewModel.createUpdateDeletePetApiIsLoading = false
                    setButtonToNormal()

                    // get error message + show(Toast)
                    val errorMessage = JSONObject(response.errorBody()!!.string().trim()).getString("message")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<PetProfileUpdateResponseDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> return
                if(_binding == null) {
                    return
                }

                // set api state/button to normal
                myPetViewModel.createUpdateDeletePetApiIsLoading = false
                setButtonToNormal()

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    // set button to loading
    private fun setButtonToLoading() {
        binding.confirmButton.visibility = View.GONE
        binding.createPetProgressBar.visibility = View.VISIBLE
    }

    // set button to normal
    private fun setButtonToNormal() {
        binding.confirmButton.visibility = View.VISIBLE
        binding.createPetProgressBar.visibility = View.GONE
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
        if(myPetViewModel.createUpdateDeletePetApiIsLoading) {
            setButtonToLoading()
        }
        else {
            setButtonToNormal()
        }
    }

    // for restoring views
    private fun restoreState() {
        // for title
        if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_pet_manager") {
            binding.backButtonTitle.text = context?.getText(R.string.update_pet_title)
        }

        if(myPetViewModel.petImageValue != null) {
            binding.petImageInput.setImageURI(myPetViewModel.petImageValue)
        }
        binding.petNameInput.setText(myPetViewModel.petNameValue)
        binding.petMessageInput.setText(myPetViewModel.petMessageValue)
        if(myPetViewModel.petGenderValue != null) {
            if(myPetViewModel.petGenderValue == true) {
                binding.genderFemale.isChecked = true
                binding.genderMale.isChecked = false
            }
            else {
                binding.genderFemale.isChecked = false
                binding.genderMale.isChecked = true
            }
        }
        binding.petSpeciesInput.setText(myPetViewModel.petSpeciesValue)
        binding.petBreedInput.setText(myPetViewModel.petBreedValue)
        if(myPetViewModel.petBirthYearValue != null) {
            binding.petBirthInput.updateDate(myPetViewModel.petBirthYearValue!!,
                myPetViewModel.petBirthMonthValue!! - 1, myPetViewModel.petBirthDateValue!!)
        }
        binding.yearOnlyCheckbox.isChecked = myPetViewModel.petBirthIsYearOnlyValue

        checkIsValid()
        checkIsLoading()
    }

    // save pet data to ViewModel(for pet profile)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun savePetDataForPetProfile() {
        myPetViewModel.petNameValueProfile = binding.petNameInput.text.toString()
        val petBirthStringValue: String = if (!binding.yearOnlyCheckbox.isChecked){
            "${binding.petBirthInput.year}년 ${(binding.petBirthInput.month + 1).toString().padStart(2, '0')}" +
                    "월 ${binding.petBirthInput.dayOfMonth.toString().padStart(2, '0')}일생"
        } else {
            "${binding.petBirthInput.year}년생"
        }
        myPetViewModel.petBirthValueProfile = petBirthStringValue
        myPetViewModel.petSpeciesValueProfile = binding.petSpeciesInput.text.toString()
        myPetViewModel.petBreedValueProfile = binding.petBreedInput.text.toString()
        myPetViewModel.petGenderValueProfile = if(binding.genderFemale.isChecked) { "♀" } else { "♂" }
        myPetViewModel.petAgeValueProfile = (LocalDate.now().year - binding.petBirthInput.year).toString()
        myPetViewModel.petMessageValueProfile = binding.petMessageInput.text.toString()
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
        petProfileUpdateApiCall?.cancel()
        myPetViewModel.createUpdateDeletePetApiIsLoading = false
    }
}