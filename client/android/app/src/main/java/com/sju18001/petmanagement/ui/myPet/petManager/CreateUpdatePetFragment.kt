package com.sju18001.petmanagement.ui.myPet.petManager

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCreateUpdatePetBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.util.*

class CreateUpdatePetFragment : Fragment() {

    // constant variables
    private val PICK_PHOTO = 0
    private var CREATE_UPDATE_PET_DIRECTORY: String = "create_update_pet"

    // variables for view binding
    private var _binding: FragmentCreateUpdatePetBinding? = null
    private val binding get() = _binding!!

    // variable for ViewModel
    val myPetViewModel: MyPetViewModel by activityViewModels()

    private var isViewDestroyed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateUpdatePetBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        // for title
        if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_pet_manager") {
            binding.backButtonTitle.text = context?.getText(R.string.update_pet_title)
        }

        // for DatePicker
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        if(myPetViewModel.petBirthYearValue == null) { myPetViewModel.petBirthYearValue = calendar.get(Calendar.YEAR) }
        if(myPetViewModel.petBirthMonthValue == null) { myPetViewModel.petBirthMonthValue = calendar.get(Calendar.MONTH) }
        if(myPetViewModel.petBirthDateValue == null) { myPetViewModel.petBirthDateValue = calendar.get(Calendar.DAY_OF_MONTH) }
        binding.petBirthInput.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        ) { _, year, monthOfYear, dayOfMonth ->
            myPetViewModel.petBirthYearValue = year
            myPetViewModel.petBirthMonthValue = monthOfYear + 1
            myPetViewModel.petBirthDateValue = dayOfMonth
        }

        // for view restore
        restoreState()

        // for pet photo picker
        binding.petPhotoInputButton.setOnClickListener {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

            dialog.setContentView(R.layout.select_photo_dialog)
            dialog.show()

            dialog.findViewById<ImageView>(R.id.close_button2).setOnClickListener { dialog.dismiss() }
            dialog.findViewById<Button>(R.id.upload_photo_button).setOnClickListener {
                dialog.dismiss()

                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_PHOTO)
            }
            dialog.findViewById<Button>(R.id.use_default_image).setOnClickListener {
                dialog.dismiss()

                binding.petPhotoInput.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
                myPetViewModel.petPhotoByteArray = null
                myPetViewModel.petPhotoPathValue = ""
            }
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

        // for hiding keyboard
        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentCreateUpdatePetParentLayout)
    }

    // trim text values
    private fun trimTextValues() {
        myPetViewModel.petMessageValue = myPetViewModel.petMessageValue.trim()
        myPetViewModel.petNameValue = myPetViewModel.petNameValue.trim()
        myPetViewModel.petSpeciesValue = myPetViewModel.petSpeciesValue.trim()
        myPetViewModel.petBreedValue = myPetViewModel.petBreedValue.trim()

        binding.petMessageInput.setText(myPetViewModel.petMessageValue)
        binding.petNameInput.setText(myPetViewModel.petNameValue)
        binding.petSpeciesInput.setText(myPetViewModel.petSpeciesValue)
        binding.petBreedInput.setText(myPetViewModel.petBreedValue)
    }

    // create pet
    private fun createPet() {
        // set api state/button to loading
        myPetViewModel.petManagerApiIsLoading = true
        setButtonToLoading()

        // trim text values
        trimTextValues()

        // for birth value
        val petBirthStringValue: String = if (!binding.yearOnlyCheckbox.isChecked){
            "${binding.petBirthInput.year}-${(binding.petBirthInput.month + 1).toString().padStart(2, '0')}" +
                    "-${binding.petBirthInput.dayOfMonth.toString().padStart(2, '0')}"
        } else {
            "${binding.petBirthInput.year}-01-01"
        }

        // create DTO
        val createPetRequestDto = CreatePetReqDto(
            binding.petNameInput.text.toString(),
            binding.petSpeciesInput.text.toString(),
            binding.petBreedInput.text.toString(),
            petBirthStringValue,
            binding.yearOnlyCheckbox.isChecked,
            binding.genderFemale.isChecked,
            binding.petMessageInput.text.toString()
        )

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .createPetReq(createPetRequestDto)
        call.enqueue(object: Callback<CreatePetResDto> {
            override fun onResponse(
                call: Call<CreatePetResDto>,
                response: Response<CreatePetResDto>
            ) {
                if(isViewDestroyed) return

                if(response.isSuccessful) {
                    // get created pet id + update pet photo
                    getIdAndUpdatePhoto()
                }
                else {
                    // set api state/button to normal
                    myPetViewModel.petManagerApiIsLoading = false
                    setButtonToNormal()

                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<CreatePetResDto>, t: Throwable) {
                if(isViewDestroyed) return

                // set api state/button to normal
                myPetViewModel.petManagerApiIsLoading = false
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
        myPetViewModel.petManagerApiIsLoading = true
        setButtonToLoading()

        // trim text values
        trimTextValues()

        // for birth value
        val petBirthStringValue: String = if (!binding.yearOnlyCheckbox.isChecked){
            "${binding.petBirthInput.year}-${(binding.petBirthInput.month + 1).toString().padStart(2, '0')}" +
                    "-${binding.petBirthInput.dayOfMonth.toString().padStart(2, '0')}"
        } else {
            "${binding.petBirthInput.year}-01-01"
        }

        // create DTO
        val updatePetReqDto = UpdatePetReqDto(
            myPetViewModel.petIdValue!!,
            binding.petNameInput.text.toString(),
            binding.petSpeciesInput.text.toString(),
            binding.petBreedInput.text.toString(),
            petBirthStringValue,
            binding.yearOnlyCheckbox.isChecked,
            binding.genderFemale.isChecked,
            binding.petMessageInput.text.toString()
        )

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .updatePetReq(updatePetReqDto)
        call.enqueue(object: Callback<UpdatePetResDto> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<UpdatePetResDto>,
                response: Response<UpdatePetResDto>
            ) {
                if(isViewDestroyed) return

                if(response.isSuccessful) {
                    // update pet photo(if selected)
                    updatePetPhoto(myPetViewModel.petIdValue!!, myPetViewModel.petPhotoPathValue)
                }
                else {
                    // set api state/button to normal
                    myPetViewModel.petManagerApiIsLoading = false
                    setButtonToNormal()

                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<UpdatePetResDto>, t: Throwable) {
                if(isViewDestroyed) return

                // set api state/button to normal
                myPetViewModel.petManagerApiIsLoading = false
                setButtonToNormal()

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    // update pet photo
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePetPhoto(id: Long, path: String) {
        // If basic photo selected
        if(path == "") {
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .deletePetPhotoReq(DeletePetPhotoReqDto(id))
            call.enqueue(object: Callback<DeletePetPhotoResDto> {
                override fun onResponse(
                    call: Call<DeletePetPhotoResDto>,
                    response: Response<DeletePetPhotoResDto>
                ) {
                    if(isViewDestroyed) return

                    if(response.isSuccessful){
                        closeAfterSuccess()
                    }else{
                        // set api state/button to normal
                        myPetViewModel.petManagerApiIsLoading = false
                        setButtonToNormal()

                        // get error message + show(Toast)
                        val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                        // log error message
                        Log.d("error", errorMessage)
                    }
                }

                override fun onFailure(call: Call<DeletePetPhotoResDto>, t: Throwable) {
                    if(isViewDestroyed) return

                    // set api state/button to normal
                    myPetViewModel.petManagerApiIsLoading = false
                    setButtonToNormal()

                    // show(Toast)/log error message
                    Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                    Log.d("error", t.message.toString())
                }
            }
            )
        } else {
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .updatePetPhotoReq(id, MultipartBody.Part.createFormData("file", "file.png",
                    RequestBody.create(MediaType.parse("multipart/form-data"), File(path))))
            call.enqueue(object: Callback<UpdatePetPhotoResDto> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<UpdatePetPhotoResDto>,
                    response: Response<UpdatePetPhotoResDto>
                ) {
                    if(isViewDestroyed) return

                    if(response.isSuccessful) {
                        // delete copied file
                        File(path).delete()

                        // close after success
                        closeAfterSuccess()
                    }
                    else {
                        // set api state/button to normal
                        myPetViewModel.petManagerApiIsLoading = false
                        setButtonToNormal()

                        // get error message + show(Toast)
                        val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                        // log error message
                        Log.d("error", errorMessage)
                    }
                }

                override fun onFailure(call: Call<UpdatePetPhotoResDto>, t: Throwable) {
                    if(isViewDestroyed) return

                    // set api state/button to normal
                    myPetViewModel.petManagerApiIsLoading = false
                    setButtonToNormal()

                    // show(Toast)/log error message
                    Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                    Log.d("error", t.message.toString())
                }
            })
        }
    }

    // get created pet id + update pet photo
    private fun getIdAndUpdatePhoto() {
        // create DTO
        val fetchPetReqDto = FetchPetReqDto( null )

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPetReq(fetchPetReqDto)
        call.enqueue(object: Callback<FetchPetResDto> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<FetchPetResDto>,
                response: Response<FetchPetResDto>
            ) {
                if(isViewDestroyed) return

                if(response.isSuccessful) {
                    val petIdList: ArrayList<Long> = ArrayList()
                    response.body()?.petList?.map {
                        petIdList.add(it.id)
                    }

                    // update pet photo
                    updatePetPhoto(petIdList[petIdList.size - 1], myPetViewModel.petPhotoPathValue)
                }
                else {
                    // set api state/button to normal
                    myPetViewModel.petManagerApiIsLoading = false
                    setButtonToNormal()

                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchPetResDto>, t: Throwable) {
                if(isViewDestroyed) return

                // set api state/button to normal
                myPetViewModel.petManagerApiIsLoading = false
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
        if(myPetViewModel.petManagerApiIsLoading) {
            setButtonToLoading()
        }
        else {
            setButtonToNormal()
        }
    }

    // for restoring views
    private fun restoreState() {
        // set selected photo(if any)
        if(myPetViewModel.petPhotoPathValue != "") {
            binding.petPhotoInput.setImageBitmap(BitmapFactory.decodeFile(myPetViewModel.petPhotoPathValue))
        }
        // if photo not selected, and is in update mode
        else if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_pet_manager") {
            // if photo is not null -> fetch photo else set default
            if(myPetViewModel.petPhotoByteArray != null) {
                val bitmap = BitmapFactory.decodeByteArray(myPetViewModel.petPhotoByteArray, 0,
                    myPetViewModel.petPhotoByteArray!!.size)
                binding.petPhotoInput.setImageBitmap(bitmap)
            }
            else {
                binding.petPhotoInput.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
            }
        }
        // if photo not selected and is in create mode -> set default
        else {
            binding.petPhotoInput.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
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

    // close fragment/activity after create/update success
    @RequiresApi(Build.VERSION_CODES.O)
    private fun closeAfterSuccess() {
        // set api state/button to normal
        myPetViewModel.petManagerApiIsLoading = false
        setButtonToNormal()

        // show message + return to previous activity/fragment
        if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_pet_manager") {
            Toast.makeText(context, context?.getText(R.string.update_pet_successful), Toast.LENGTH_LONG).show()
            savePetDataForPetProfile()
            fragmentManager?.popBackStack()
        }
        else {
            Toast.makeText(context, context?.getText(R.string.create_pet_successful), Toast.LENGTH_LONG).show()
            activity?.finish()
        }
    }

    // save pet data to ViewModel(for pet profile)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun savePetDataForPetProfile() {
        // 사진이 기본 이미지일 때 예외 처리
        try{
            val bitmap = (binding.petPhotoInput.drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val photoByteArray = stream.toByteArray()
            myPetViewModel.petPhotoByteArrayProfile = photoByteArray
        }catch(e: Exception){
            myPetViewModel.petPhotoByteArrayProfile = null
        }

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

    // for photo select
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // get + save pet photo value
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_PHOTO){
            if (data != null) {
                // delete previously copied file(if any)
                if(myPetViewModel.petPhotoPathValue != "") {
                    File(myPetViewModel.petPhotoPathValue).delete()
                }

                // copy selected photo and get real path
                myPetViewModel.petPhotoPathValue = ServerUtil.createCopyAndReturnRealPathLocal(requireActivity(),
                    data.data!!, CREATE_UPDATE_PET_DIRECTORY)

                // set photo to view
                binding.petPhotoInput.setImageBitmap(BitmapFactory.decodeFile(myPetViewModel.petPhotoPathValue))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true

        // delete copied file(if any)
        if(isRemoving || requireActivity().isFinishing) {
            Util.deleteCopiedFiles(requireContext(), CREATE_UPDATE_PET_DIRECTORY)
        }

        myPetViewModel.petManagerApiIsLoading = false
    }
}