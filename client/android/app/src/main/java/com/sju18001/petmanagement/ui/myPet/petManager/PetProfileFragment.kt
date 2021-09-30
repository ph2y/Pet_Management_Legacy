package com.sju18001.petmanagement.ui.myPet.petManager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentPetProfileBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.DeletePetReqDto
import com.sju18001.petmanagement.restapi.dto.DeletePetResDto
import com.sju18001.petmanagement.ui.community.post.PostFragment
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

    private var isViewDestroyed = false

    // true: 기본 상태, false: 특정 뷰들이 GONE인 상태
    private var isViewDetailed: Boolean = true

    private var backButtonLayoutHeight = 0

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetProfileBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        val view = binding.root

        // save pet data to ViewModel(for pet profile) if not already loaded
        if(!myPetViewModel.loadedFromIntent) { savePetDataForPetProfile() }

        // if fragment type is pet_profile_pet_manager -> hide username_and_pets_layout
        if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_pet_manager") {
            binding.usernameAndPetsLayout.visibility = View.GONE
        }
        else {
            // TODO: implement logic for username_and_pets_layout
        }


        // Fragment 추가
        if(childFragmentManager.findFragmentById(R.id.post_fragment_container) == null){
            val fragment = PostFragment.newInstance(requireActivity().intent.getLongExtra("petId", -1))
            childFragmentManager
                .beginTransaction()
                .add(R.id.post_fragment_container, fragment)
                .commit()
        }

        // Set views
        binding.postFragmentContainer.layoutParams.height = Util.getScreenHeightInPixel(requireActivity())
        binding.backButtonLayout.doOnLayout {
            backButtonLayoutHeight = it.measuredHeight
            binding.petMessage.doOnPreDraw { binding.buttonsLayout.doOnPreDraw {
                setViewsForDetail(true)
            }}
        }

        binding.postFragmentContainer.post{
            addListenerOnRecyclerView()
        }

        return view
    }

    override fun onStart() {
        super.onStart()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
        myPetViewModel.petManagerApiIsLoading = false
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
        myPetViewModel.isDeletePhoto = false
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

    private fun deletePet() {
        // set api state/button to loading
        myPetViewModel.petManagerApiIsLoading = true
        disableButton()

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .deletePetReq(DeletePetReqDto(requireActivity().intent.getLongExtra("petId", -1)))
        ServerUtil.enqueueApiCall(call, isViewDestroyed, requireContext(), {
            // set api state/button to normal
            myPetViewModel.petManagerApiIsLoading = false
            enableButton()

            Toast.makeText(context, context?.getText(R.string.delete_pet_successful), Toast.LENGTH_LONG).show()
            activity?.finish()
        }, {
            myPetViewModel.petManagerApiIsLoading = false
            enableButton()
        }, {
            myPetViewModel.petManagerApiIsLoading = false
            enableButton()
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    private fun addListenerOnRecyclerView(){
        val recyclerView = binding.postFragmentContainer.findViewById<RecyclerView>(R.id.recycler_view_post)

        // 터치를 시작할 때의 좌표를 기록함
        var x = 0f
        var y = 0f
        
        recyclerView.setOnTouchListener { v, event ->
            when (event.action){
                MotionEvent.ACTION_DOWN -> {
                    x = event.x
                    y = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // 클릭 시(== 터치 이동 반경이 짧을 때)
                    if(kotlin.math.abs(x - event.x) < 10 && kotlin.math.abs(y - event.y) < 10){
                        setViewsForDetail(!isViewDetailed)
                    }
                    // 스크롤 다운
                    else if(y > event.y){
                        setViewsForDetail(false)
                    }
                    true
                }
            }

            v.performClick()
            v.onTouchEvent(event) ?: true
        }

        // 최상단에 위치할 시 VISIBLE
        recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            if(!recyclerView.canScrollVertically(-1)){
                setViewsForDetail(true)
            }
        }
    }

    private fun setViewsForDetail(flag: Boolean){
        isViewDetailed = flag

        if(isViewDetailed){
            binding.backButtonLayout.visibility = View.VISIBLE
            if(myPetViewModel.petMessageValueProfile.isNotEmpty()) {
                binding.petMessage.visibility = View.VISIBLE
            }
            if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_pet_manager"){
                binding.buttonsLayout.visibility = View.VISIBLE
            }

            binding.petProfileMainScrollView.scrollTo(0, 0)
            (binding.petProfileMainScrollView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = backButtonLayoutHeight

        }else{
            binding.backButtonLayout.visibility = View.GONE
            binding.petMessage.visibility = View.GONE
            binding.buttonsLayout.visibility = View.GONE

            (binding.petProfileMainScrollView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 0
        }
    }
}