package com.sju18001.petmanagement.ui.myPet.petManager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet
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
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dao.Pet
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.community.post.PostFragment
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import java.time.LocalDate
import java.time.Period

class PetProfileFragment : Fragment(){
    // variables for view binding
    private var _binding: FragmentPetProfileBinding? = null
    private val binding get() = _binding!!

    // variable for ViewModel
    private val myPetViewModel: MyPetViewModel by activityViewModels()

    private var isViewDestroyed = false

    private val POST_FRAGMENT_TAG = "post_fragment"
    private val TRANSITION_DURATION = 300L

    // true: 기본 상태, false: 특정 뷰들이 GONE인 상태
    private var isViewDetailed: Boolean = true

    private var topFixedLayoutHeight = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetProfileBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        val view = binding.root

        // get fragment type
        myPetViewModel.fragmentType = requireActivity().intent.getStringExtra("fragmentType")

        // save data to ViewModel if not already loaded
        if(!myPetViewModel.loadedAuthorFromIntent) { saveAuthorDataForAuthorProfile() }
        if(!myPetViewModel.loadedPetFromIntent) { savePetDataForPetProfile() }

        // show certain views depending on the fragment type
        if(myPetViewModel.fragmentType == "pet_profile_pet_manager") {
            if (!myPetViewModel.isRepresentativePetProfile) {
                binding.setRepresentativeButton.visibility = View.VISIBLE
            }
            binding.buttonsLayout.visibility = View.VISIBLE
        }
        else {
            binding.usernameAndPetsLayout.visibility = View.VISIBLE
            binding.petInfoLayout.background = null

            // for pet spinner
            if (myPetViewModel.fragmentType == "pet_profile_community") {
                setPetSpinner()
            }
        }

        if (myPetViewModel.fragmentType == "pet_profile_pet_manager") {
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
            binding.topFixedLayout.doOnLayout {
                topFixedLayoutHeight = it.measuredHeight
                binding.buttonsLayout.doOnPreDraw {
                    setViewsForDetail(true)
                }
            }

            binding.postFragmentContainer.post{
                addListenerOnRecyclerView()
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        // for set representative button
        if (myPetViewModel.fragmentType == "pet_profile_pet_manager") {
            binding.setRepresentativeButton.setOnClickListener {
                val builder = AlertDialog.Builder(activity)
                builder.setMessage(myPetViewModel.petNameValueProfile + context?.getString(R.string.set_representative_message))
                    .setPositiveButton(
                        R.string.confirm
                    ) { _, _ ->
                        setRepresentativePet()
                    }
                    .setNegativeButton(
                        R.string.cancel
                    ) { dialog, _ ->
                        dialog.cancel()
                    }
                    .create().show()
            }
        }

        // for pet update and delete button
        if (myPetViewModel.fragmentType == "pet_profile_pet_manager") {
            binding.updatePetButton.setOnClickListener {
                // save pet data to ViewModel(for pet update)
                savePetDataForPetUpdate()

                // open update pet fragment
                activity?.supportFragmentManager?.beginTransaction()!!
                    .replace(R.id.my_pet_activity_fragment_container, CreateUpdatePetFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        // for back button
        binding.backButton.setOnClickListener {
            activity?.finish()
        }

        // for history text
        binding.textHistory.setOnClickListener {
            setViewsForDetail(!isViewDetailed)
        }

        // for pet information layout
        binding.petInfoLayout.setOnClickListener {
            setViewsForDetail(true)
        }
    }

    override fun onResume() {
        super.onResume()

        // set views with data from ViewModel
        setViewsWithPetData()
        setViewsWithAuthorData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
        myPetViewModel.petManagerApiIsLoading = false
    }

    private fun replacePetProfile(pet: Pet) {
        myPetViewModel.petIdValueProfile = myPetViewModel.petIdValue
        myPetViewModel.petPhotoUrlValueProfile = pet.photoUrl
        if (myPetViewModel.petPhotoUrlValueProfile.isNullOrEmpty()) {
            myPetViewModel.petPhotoByteArrayProfile = null
        }
        else {
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .fetchPetPhotoReq(FetchPetPhotoReqDto(myPetViewModel.petIdValueProfile!!))
            ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
                myPetViewModel.petPhotoByteArrayProfile = response.body()!!.bytes()
                setPhotoViews()
            }, {}, {})
        }
        myPetViewModel.petNameValueProfile = pet.name
        myPetViewModel.petBirthValueProfile = if(pet.yearOnly!!) pet.birth!!.substring(0,4) else pet.birth!!
        myPetViewModel.petSpeciesValueProfile = pet.species
        myPetViewModel.petBreedValueProfile = pet.breed
        myPetViewModel.petGenderValueProfile = Util.getGenderSymbol(pet.gender, requireContext())
        myPetViewModel.petAgeValueProfile = Util.getAgeFromBirth(pet.birth)
        myPetViewModel.petMessageValueProfile = pet.message.toString()
        myPetViewModel.isRepresentativePetProfile =
            myPetViewModel.petIdValueProfile == myPetViewModel.accountRepresentativePetId

        setViewsWithPetData()
    }

    private fun replacePostFragment() {
        // remove previous fragment
        childFragmentManager.findFragmentByTag(POST_FRAGMENT_TAG)?.let {
            childFragmentManager.beginTransaction().remove(it).commit()
        }

        // Fragment 추가
        val fragment = PostFragment.newInstance(myPetViewModel.petIdValue!!)
        childFragmentManager
            .beginTransaction()
            .add(R.id.post_fragment_container, fragment, POST_FRAGMENT_TAG)
            .commit()

        // Set views
        binding.postFragmentContainer.layoutParams.height = Util.getScreenHeightInPixel(requireActivity())
        binding.topFixedLayout.doOnLayout {
            topFixedLayoutHeight = it.measuredHeight
            binding.petMessage.doOnPreDraw { binding.buttonsLayout.doOnPreDraw {
                setViewsForDetail(true)
            }}
        }

        binding.postFragmentContainer.post{
            addListenerOnRecyclerView()
        }
    }

    private fun setPetSpinner() {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPetReq(FetchPetReqDto(null, myPetViewModel.accountUsernameValue))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            // get pet id and name
            val apiResponse: MutableList<Pet> = mutableListOf()
            response.body()?.petList?.map {
                val item = Pet(
                    it.id, "", it.name, it.species, it.breed, it.birth,
                    it.yearOnly, it.gender, it.message, it.photoUrl
                )
                apiResponse.add(item)
            }

            // set spinner and pet id values
            val spinnerArray: ArrayList<String> = ArrayList()
            for (pet in apiResponse) {
                if (pet.id == myPetViewModel.accountRepresentativePetId) {
                    spinnerArray.add(pet.name + " *")
                }
                else {
                    spinnerArray.add(pet.name)
                }
            }

            // set spinner adapter
            val spinnerArrayAdapter: ArrayAdapter<String> =
                ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, spinnerArray)
            binding.petNameSpinner.adapter = spinnerArrayAdapter

            // set spinner listener
            binding.petNameSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    myPetViewModel.petIdValue = apiResponse[position].id
                    replacePetProfile(apiResponse[position])
                    replacePostFragment()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            // set spinner position
            for(i in 0 until apiResponse.size) {
                if (myPetViewModel.petIdValue == apiResponse[i].id) {
                    binding.petNameSpinner.setSelection(i)
                    break
                }
            }
        }, {}, {})
    }

    private fun saveAuthorDataForAuthorProfile() {
        myPetViewModel.loadedAuthorFromIntent = true
        myPetViewModel.accountIdValue = requireActivity().intent.getLongExtra("accountId", -1)
        myPetViewModel.accountUsernameValue = requireActivity().intent.getStringExtra("accountUsername")
        myPetViewModel.accountPhotoUrlValue = requireActivity().intent.getStringExtra("accountPhotoUrl")
        if (myPetViewModel.accountPhotoUrlValue.isNullOrEmpty()) {
            myPetViewModel.accountPhotoByteArray = null
        }
        else {
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .fetchAccountPhotoReq(FetchAccountPhotoReqDto(myPetViewModel.accountIdValue))
            ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
                myPetViewModel.accountPhotoByteArray = response.body()!!.bytes()
                setPhotoViews()
            }, {}, {})
        }
        myPetViewModel.accountNicknameValue = requireActivity().intent.getStringExtra("accountNickname")
        myPetViewModel.accountRepresentativePetId = requireActivity().intent.getLongExtra("representativePetId", -1)
    }

    private fun savePetDataForPetProfile() {
        myPetViewModel.loadedPetFromIntent = true
        myPetViewModel.petIdValue = requireActivity().intent.getLongExtra("petId", -1)
        if (myPetViewModel.fragmentType == "pet_profile_pet_manager") {
            myPetViewModel.petPhotoByteArrayProfile = Util.getByteArrayFromSharedPreferences(requireContext(),
                requireContext().getString(R.string.pref_name_byte_arrays),
                requireContext().getString(R.string.data_name_my_pet_selected_pet_photo))

            setPhotoViews()
        }
        else {
            myPetViewModel.petPhotoUrlValueProfile = requireActivity().intent.getStringExtra("petPhotoUrl")
            if (myPetViewModel.petPhotoUrlValueProfile.isNullOrEmpty()) {
                myPetViewModel.petPhotoByteArrayProfile = null
            }
            else {
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                    .fetchPetPhotoReq(FetchPetPhotoReqDto(requireActivity().intent.getLongExtra("petId", -1)))
                ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
                    myPetViewModel.petPhotoByteArrayProfile = response.body()!!.bytes()
                    setPhotoViews()
                }, {}, {})
            }
        }
        myPetViewModel.petNameValueProfile = requireActivity().intent.getStringExtra("petName").toString()
        myPetViewModel.petBirthValueProfile = requireActivity().intent.getStringExtra("petBirth").toString()
        myPetViewModel.petSpeciesValueProfile = requireActivity().intent.getStringExtra("petSpecies").toString()
        myPetViewModel.petBreedValueProfile = requireActivity().intent.getStringExtra("petBreed").toString()
        myPetViewModel.petGenderValueProfile = requireActivity().intent.getStringExtra("petGender").toString()
        myPetViewModel.petAgeValueProfile = requireActivity().intent.getStringExtra("petAge").toString()
        myPetViewModel.petMessageValueProfile = requireActivity().intent.getStringExtra("petMessage").toString()
        if (myPetViewModel.fragmentType == "pet_profile_pet_manager") {
            myPetViewModel.isRepresentativePetProfile = requireActivity().intent.getBooleanExtra("isRepresentativePet", false)
        }
        else {
            myPetViewModel.petIdValueProfile = myPetViewModel.petIdValue
            myPetViewModel.isRepresentativePetProfile =
                myPetViewModel.petIdValueProfile == myPetViewModel.accountRepresentativePetId
        }
    }

    private fun setPhotoViews() {
        if(myPetViewModel.petPhotoByteArrayProfile != null) {
            val bitmap = BitmapFactory.decodeByteArray(myPetViewModel.petPhotoByteArrayProfile, 0,
                myPetViewModel.petPhotoByteArrayProfile!!.size)
            binding.petPhoto.setImageBitmap(bitmap)
        }
        else {
            binding.petPhoto.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
        }

        if(myPetViewModel.accountPhotoByteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(myPetViewModel.accountPhotoByteArray, 0,
                myPetViewModel.accountPhotoByteArray!!.size)
            binding.accountPhoto.setImageBitmap(bitmap)
        }
        else {
            binding.accountPhoto.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_account_circle_24))
        }
    }

    private fun setViewsWithPetData() {
        setPhotoViews()
        binding.petName.text = myPetViewModel.petNameValueProfile
        binding.petBirth.text = if(myPetViewModel.petBirthValueProfile.length == 4) myPetViewModel.petBirthValueProfile + "년생" else myPetViewModel.petBirthValueProfile.substring(2).replace('-', '.')
        binding.petBreed.text = myPetViewModel.petBreedValueProfile
        binding.petGender.text = myPetViewModel.petGenderValueProfile
        binding.petAge.text = myPetViewModel.petAgeValueProfile + "살"
        val message = if(myPetViewModel.petMessageValueProfile.isNullOrEmpty()) getString(R.string.filled_heart) else myPetViewModel.petMessageValueProfile
        binding.petMessage.text = "\" $message \""
    }

    private fun setViewsWithAuthorData() {
        setPhotoViews()
        val authorNickNameLabel = myPetViewModel.accountNicknameValue + "님의"
        binding.accountNickname.text = authorNickNameLabel
    }

    private fun savePetDataForPetUpdate() {
        myPetViewModel.petPhotoByteArray = myPetViewModel.petPhotoByteArrayProfile
        myPetViewModel.petPhotoPathValue = ""
        myPetViewModel.isDeletePhoto = false
        myPetViewModel.petMessageValue = myPetViewModel.petMessageValueProfile
        myPetViewModel.petNameValue = myPetViewModel.petNameValueProfile
        myPetViewModel.petGenderValue = myPetViewModel.petGenderValueProfile == "♀"
        myPetViewModel.petSpeciesValue = myPetViewModel.petSpeciesValueProfile
        myPetViewModel.petBreedValue = myPetViewModel.petBreedValueProfile
        myPetViewModel.petBirthIsYearOnlyValue = myPetViewModel.petBirthValueProfile.length == 4
        myPetViewModel.petBirthYearValue = myPetViewModel.petBirthValueProfile.substring(0, 4).toInt()
        if(!myPetViewModel.petBirthIsYearOnlyValue) {
            myPetViewModel.petBirthMonthValue = myPetViewModel.petBirthValueProfile.substring(5, 7).toInt()
            myPetViewModel.petBirthDateValue = myPetViewModel.petBirthValueProfile.substring(8, 10).toInt()
        }
    }

    private fun setRepresentativePet() {
        // set api state/button to loading
        myPetViewModel.petManagerApiIsLoading = true

        // create DTO for API call
        val accountData = SessionManager.fetchLoggedInAccount(requireContext())!!
        val updateAccountReqDto = UpdateAccountReqDto(
            accountData.email,
            accountData.phone,
            accountData.nickname,
            accountData.marketing,
            accountData.userMessage,
            myPetViewModel.petIdValue
        )

        // update account
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .updateAccountReq(updateAccountReqDto)
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            if (response.body()?._metadata?.status == true) {
                // update session(update representative pet id value)
                val account = Account(
                    accountData.id, accountData.username, accountData.email, accountData.phone, accountData.password,
                    accountData.marketing, accountData.nickname, accountData.photoUrl, accountData.userMessage, myPetViewModel.petIdValue
                )
                SessionManager.saveLoggedInAccount(requireContext(), account)

                // update flag and related views
                myPetViewModel.isRepresentativePetProfile = true
                binding.setRepresentativeButton.visibility = View.GONE

                binding.representativePetIcon.visibility = View.VISIBLE
                binding.representativePetIcon.setImageResource(R.drawable.crown)
                binding.representativePetIcon.scaleType = ImageView.ScaleType.FIT_XY

                // set api state/button to normal
                myPetViewModel.petManagerApiIsLoading = false
            }
        }, {
            // set api state/button to normal
            myPetViewModel.petManagerApiIsLoading = false
        }, {
            // set api state/button to normal
            myPetViewModel.petManagerApiIsLoading = false
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
            // pet_info_layout 애니메이션
            TransitionManager.beginDelayedTransition(
                binding.petInfoLayout,
                AutoTransition().setDuration(TRANSITION_DURATION).setInterpolator(AccelerateDecelerateInterpolator())
            )
            ConstraintSet().apply{
                clone(context, R.layout.pet_info_layout_origin)
            }.applyTo(binding.petInfoLayout)


            binding.topFixedLayout.visibility = View.VISIBLE
            if (myPetViewModel.fragmentType == "pet_profile_community") {
                binding.usernameAndPetsLayout.visibility = View.VISIBLE
            }
            if(myPetViewModel.fragmentType == "pet_profile_pet_manager"){
                binding.buttonsLayout.visibility = View.VISIBLE
            }

            if (myPetViewModel.isRepresentativePetProfile){
                binding.representativePetIcon.setImageResource(R.drawable.crown)
                binding.representativePetIcon.scaleType = ImageView.ScaleType.FIT_XY

                binding.representativePetIcon.visibility = View.VISIBLE
            }else{
                binding.representativePetIcon.visibility = View.INVISIBLE
            }

            binding.petProfileMainScrollView.scrollTo(0, 0)
            (binding.petProfileMainScrollView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = topFixedLayoutHeight
        }else{
            // pet_info_layout 애니메이션
            TransitionManager.beginDelayedTransition(
                binding.petInfoLayout,
                ChangeBounds().setDuration(TRANSITION_DURATION).setInterpolator(AccelerateDecelerateInterpolator())
            )
            ConstraintSet().apply{
                clone(context, R.layout.pet_info_layout_alter)
            }.applyTo(binding.petInfoLayout)

            binding.topFixedLayout.visibility = View.GONE
            binding.usernameAndPetsLayout.visibility = View.GONE
            binding.buttonsLayout.visibility = View.GONE

            if (myPetViewModel.isRepresentativePetProfile){
                binding.representativePetIcon.setImageResource(R.drawable.crown)
                binding.representativePetIcon.scaleType = ImageView.ScaleType.FIT_XY

                binding.representativePetIcon.visibility = View.VISIBLE
            }else{
                binding.representativePetIcon.visibility = View.INVISIBLE
            }

            (binding.petProfileMainScrollView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 0
        }
    }
}