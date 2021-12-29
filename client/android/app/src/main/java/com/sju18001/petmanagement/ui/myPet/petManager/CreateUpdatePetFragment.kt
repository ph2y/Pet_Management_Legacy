package com.sju18001.petmanagement.ui.myPet.petManager

import android.app.AlertDialog
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
        else if(requireActivity().intent.getStringExtra("fragmentType") == "create_pet"){
            binding.deletePetLayout.visibility = View.GONE
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
                if (myPetViewModel.petPhotoPathValue != "") {
                    File(myPetViewModel.petPhotoPathValue).delete()
                    myPetViewModel.petPhotoPathValue = ""
                }
                myPetViewModel.isDeletePhoto = true
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

        // for delete button
        binding.deletePetButton.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(context?.getString(R.string.delete_pet_dialog_message))
                .setPositiveButton(
                    R.string.confirm
                ) { _, _ ->
                    deletePet()
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, _ ->
                    dialog.cancel()
                }
                .create().show()
        }

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
        lockViews()

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
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            getIdAndUpdatePhoto()
        }, { response ->
            // set api state/button to normal
            myPetViewModel.petManagerApiIsLoading = false
            unlockViews()

            Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
        }, {
            // set api state/button to normal
            myPetViewModel.petManagerApiIsLoading = false
            unlockViews()
        })
    }

    // update pet
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePet() {
        // set api state/button to loading
        myPetViewModel.petManagerApiIsLoading = true
        lockViews()

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
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            updatePetPhoto(myPetViewModel.petIdValue!!, myPetViewModel.petPhotoPathValue)
        }, {
            // set api state/button to normal
            myPetViewModel.petManagerApiIsLoading = false
            unlockViews()
        }, {
            // set api state/button to normal
            myPetViewModel.petManagerApiIsLoading = false
            unlockViews()
        })
    }

    // update pet photo
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePetPhoto(id: Long, path: String) {
        // exception
        if (!myPetViewModel.isDeletePhoto && path == "") {
            closeAfterSuccess()
            return
        }

        // delete photo
        if(myPetViewModel.isDeletePhoto!!) {
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
                        // get error message
                        val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                        // ignore null error(delete nothing)
                        if (errorMessage == "null") {
                            closeAfterSuccess()
                        } else {
                            // set api state/button to normal
                            myPetViewModel.petManagerApiIsLoading = false
                            unlockViews()

                            Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
                        }
                    }
                }

                override fun onFailure(call: Call<DeletePetPhotoResDto>, t: Throwable) {
                    if(isViewDestroyed) return

                    // set api state/button to normal
                    myPetViewModel.petManagerApiIsLoading = false
                    unlockViews()

                    Util.showToastAndLog(requireContext(), t.message.toString())
                }
            })
        }
        // update photo
        else {
            val file = MultipartBody.Part.createFormData("file", File(path).name, RequestBody.create(MediaType.parse("multipart/form-data"), File(path)))
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .updatePetPhotoReq(id, file)
            ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
                File(path).delete()

                closeAfterSuccess()
            }, {
                // set api state/button to normal
                myPetViewModel.petManagerApiIsLoading = false
                unlockViews()
            }, {
                myPetViewModel.petManagerApiIsLoading = false
                unlockViews()
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getIdAndUpdatePhoto() {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPetReq(FetchPetReqDto( null , null))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            val petIdList: ArrayList<Long> = ArrayList()
            response.body()?.petList?.map {
                petIdList.add(it.id)
            }

            updatePetPhoto(petIdList[petIdList.size - 1], myPetViewModel.petPhotoPathValue)
        }, {
            // set api state/button to normal
            myPetViewModel.petManagerApiIsLoading = false
            unlockViews()
        }, {
            myPetViewModel.petManagerApiIsLoading = false
            unlockViews()
        })
    }

    private fun lockViews() {
        binding.confirmButton.visibility = View.GONE
        binding.createPetProgressBar.visibility = View.VISIBLE

        binding.petPhotoInputButton.isEnabled = false
        binding.petMessageInput.isEnabled = false
        binding.petNameInput.isEnabled = false
        binding.genderFemale.isEnabled = false
        binding.genderMale.isEnabled = false
        binding.petSpeciesInput.isEnabled = false
        binding.petBreedInput.isEnabled = false
        binding.petBirthInput.isEnabled = false
        binding.yearOnlyCheckbox.isEnabled = false
        binding.backButton.isEnabled = false
        binding.petPhotoInput.borderColor = resources.getColor(R.color.gray)
        binding.petPhotoInputButton.circleBackgroundColor = resources.getColor(R.color.gray)
    }

    private fun unlockViews() {
        binding.confirmButton.visibility = View.VISIBLE
        binding.createPetProgressBar.visibility = View.GONE

        binding.petPhotoInputButton.isEnabled = true
        binding.petMessageInput.isEnabled = true
        binding.petNameInput.isEnabled = true
        binding.genderFemale.isEnabled = true
        binding.genderMale.isEnabled = true
        binding.petSpeciesInput.isEnabled = true
        binding.petBreedInput.isEnabled = true
        binding.petBirthInput.isEnabled = true
        binding.yearOnlyCheckbox.isEnabled = true
        binding.backButton.isEnabled = true
        binding.petPhotoInput.borderColor = resources.getColor(R.color.carrot)
        binding.petPhotoInputButton.circleBackgroundColor = resources.getColor(R.color.pumpkin)
    }

    private fun checkIsValid() {
        // if valid -> enable confirm button
        binding.confirmButton.isEnabled = myPetViewModel.petNameValue != "" && myPetViewModel.petGenderValue != null &&
                myPetViewModel.petSpeciesValue != "" && myPetViewModel.petBreedValue != ""
    }

    private fun checkIsLoading() {
        // if loading -> set button to loading
        if(myPetViewModel.petManagerApiIsLoading) {
            lockViews()
        }
        else {
            unlockViews()
        }
    }

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
        unlockViews()

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
        myPetViewModel.petBirthValueProfile = if (!binding.yearOnlyCheckbox.isChecked){
            "${binding.petBirthInput.year}-${(binding.petBirthInput.month + 1).toString().padStart(2, '0')}" +
                    "-${binding.petBirthInput.dayOfMonth.toString().padStart(2, '0')}"
        } else {
            "${binding.petBirthInput.year}"
        }
        myPetViewModel.petSpeciesValueProfile = binding.petSpeciesInput.text.toString()
        myPetViewModel.petBreedValueProfile = binding.petBreedInput.text.toString()
        myPetViewModel.petGenderValueProfile = if(binding.genderFemale.isChecked) { "♀" } else { "♂" }
        myPetViewModel.petAgeValueProfile = (LocalDate.now().year - binding.petBirthInput.year).toString()
        myPetViewModel.petMessageValueProfile = binding.petMessageInput.text.toString()
    }

    private fun deletePet() {
        // set api state/button to loading
        myPetViewModel.petManagerApiIsLoading = true

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .deletePetReq(DeletePetReqDto(requireActivity().intent.getLongExtra("petId", -1)))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            // set api state/button to normal
            myPetViewModel.petManagerApiIsLoading = false

            Toast.makeText(context, context?.getText(R.string.delete_pet_successful), Toast.LENGTH_LONG).show()
            activity?.finish()
        }, {
            myPetViewModel.petManagerApiIsLoading = false
        }, {
            myPetViewModel.petManagerApiIsLoading = false
        })
    }

    // for photo select
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // get + save pet photo value
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_PHOTO){
            if (data != null) {
                // get file name
                val fileName = Util.getSelectedFileName(requireContext(), data.data!!)

                // copy selected photo and get real path
                val petPhotoPathValue = ServerUtil.createCopyAndReturnRealPathLocal(requireActivity(),
                    data.data!!, CREATE_UPDATE_PET_DIRECTORY, fileName)

                // file type exception -> delete copied file + show Toast message
                if (!Util.isUrlPhoto(petPhotoPathValue)) {
                    Toast.makeText(context, context?.getText(R.string.photo_file_type_exception_message), Toast.LENGTH_LONG).show()
                    File(petPhotoPathValue).delete()
                    return
                }

                // delete previously copied file(if any)
                if(myPetViewModel.petPhotoPathValue != "") {
                    File(myPetViewModel.petPhotoPathValue).delete()
                }

                // save path to ViewModel
                myPetViewModel.petPhotoPathValue = petPhotoPathValue
                myPetViewModel.isDeletePhoto = false

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