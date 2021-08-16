package com.sju18001.petmanagement.ui.myPet.petManager

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentPetProfileBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.DeletePetReqDto
import com.sju18001.petmanagement.restapi.dto.DeletePetResDto
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PetProfileFragment : Fragment(){

    // variables for view binding
    private var _binding: FragmentPetProfileBinding? = null
    private val binding get() = _binding!!

    // variable for ViewModel
    private val myPetViewModel: MyPetViewModel by activityViewModels()

    // variable for storing API call(for cancel)
    private var deletePetApiCall: Call<DeletePetResDto>? = null

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
        _binding = FragmentPetProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // save pet data to ViewModel(for pet profile) if not already loaded
        if(!myPetViewModel.loadedFromIntent) { savePetDataForPetProfile() }

        // if fragment type is pet_profile_pet_manager -> hide username_and_pets_layout
        if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_pet_manager") {
            binding.usernameAndPetsLayout.visibility = View.GONE
        }

        // if pet message is empty -> hide view
        if(myPetViewModel.petMessageValueProfile == "") {
            binding.petMessage.visibility = View.GONE
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        // check if API is loading
        checkIsLoading()

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

        // for pet delete button
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

        // for back button
        binding.backButton.setOnClickListener {
            activity?.finish()
        }
    }

    override fun onResume() {
        super.onResume()

        // set views with data from ViewModel
        setViewsWithPetData()
    }

    // set button to loading
    private fun disableButton() {
        binding.deletePetButton.isEnabled = false
    }

    // set button to normal
    private fun enableButton() {
        binding.deletePetButton.isEnabled = true
    }

    // for loading check
    private fun checkIsLoading() {
        // if loading -> set button to loading
        if(myPetViewModel.petManagerApiIsLoading) {
            disableButton()
        }
        else {
            enableButton()
        }
    }

    // delete pet
    private fun deletePet() {
        // set api state/button to loading
        myPetViewModel.petManagerApiIsLoading = true
        disableButton()

        // create DTO
        val deletePetReqDto = DeletePetReqDto(
            requireActivity().intent.getLongExtra("petId", -1)
        )

        deletePetApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .deletePetReq(deletePetReqDto)
        deletePetApiCall!!.enqueue(object: Callback<DeletePetResDto> {
            override fun onResponse(
                call: Call<DeletePetResDto>,
                response: Response<DeletePetResDto>
            ) {
                // set api state/button to normal
                myPetViewModel.petManagerApiIsLoading = false
                enableButton()

                if(response.isSuccessful) {
                    Toast.makeText(context, context?.getText(R.string.delete_pet_successful), Toast.LENGTH_LONG).show()
                    activity?.finish()
                }
                else {
                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<DeletePetResDto>, t: Throwable) {
                // set api state/button to normal
                myPetViewModel.petManagerApiIsLoading = false
                enableButton()

                // if the view was destroyed(API call canceled) -> return
                if(_binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun savePetDataForPetProfile() {
        myPetViewModel.loadedFromIntent = true
        myPetViewModel.petPhotoByteArrayProfile = requireActivity().intent.getByteArrayExtra("photoByteArray")
        myPetViewModel.petNameValueProfile = requireActivity().intent.getStringExtra("petName").toString()
        myPetViewModel.petBirthValueProfile = requireActivity().intent.getStringExtra("petBirth").toString()
        myPetViewModel.petSpeciesValueProfile = requireActivity().intent.getStringExtra("petSpecies").toString()
        myPetViewModel.petBreedValueProfile = requireActivity().intent.getStringExtra("petBreed").toString()
        myPetViewModel.petGenderValueProfile = requireActivity().intent.getStringExtra("petGender").toString()
        myPetViewModel.petAgeValueProfile = requireActivity().intent.getStringExtra("petAge").toString()
        myPetViewModel.petMessageValueProfile = requireActivity().intent.getStringExtra("petMessage").toString()
    }

    private fun setViewsWithPetData() {
        if(myPetViewModel.petPhotoByteArrayProfile != null) {
            val bitmap = BitmapFactory.decodeByteArray(myPetViewModel.petPhotoByteArrayProfile, 0,
                myPetViewModel.petPhotoByteArrayProfile!!.size)
            binding.petPhoto.setImageBitmap(bitmap)
        }
        else {
            binding.petPhoto.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
        }
        binding.petName.text = myPetViewModel.petNameValueProfile
        binding.petBirth.text = myPetViewModel.petBirthValueProfile
        binding.petSpecies.text = myPetViewModel.petSpeciesValueProfile
        val petBreed = '(' + myPetViewModel.petBreedValueProfile + ')'
        binding.petBreed.text = petBreed
        val petGenderAndAge = myPetViewModel.petGenderValueProfile + " / " + myPetViewModel.petAgeValueProfile + '세'
        binding.petGenderAndAge.text = petGenderAndAge
        binding.petMessage.text = myPetViewModel.petMessageValueProfile
    }

    private fun savePetDataForPetUpdate() {
        myPetViewModel.petIdValue = requireActivity().intent.getLongExtra("petId", -1)
        myPetViewModel.petPhotoByteArray = myPetViewModel.petPhotoByteArrayProfile
        myPetViewModel.petPhotoPathValue = ""
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

        // stop api call when fragment is destroyed
        deletePetApiCall?.cancel()
        myPetViewModel.petManagerApiIsLoading = false
    }
}