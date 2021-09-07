package com.sju18001.petmanagement.ui.community.createUpdatePost

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Permission
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCreateUpdatePostBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Pet
import com.sju18001.petmanagement.restapi.dao.Post
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.restapi.global.FileMetaData
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.reflect.Type
import java.math.BigDecimal

class CreateUpdatePostFragment : Fragment() {

    // constant variables
    private val PICK_PHOTO = 0
    private val PICK_VIDEO = 1
    private var PET_LIST_ORDER: String = "pet_list_id_order"
    private var DISCLOSURE_PUBLIC: String = "PUBLIC"
    private var DISCLOSURE_PRIVATE: String = "PRIVATE"
    private var DISCLOSURE_FRIEND: String = "FRIEND"
    private var CREATE_UPDATE_POST_DIRECTORY: String = "create_update_post"

    // variable for ViewModel
    private val createUpdatePostViewModel: CreateUpdatePostViewModel by activityViewModels()

    // variables for view binding
    private var _binding: FragmentCreateUpdatePostBinding? = null
    private val binding get() = _binding!!

    // variables for pet
    private var petIdAndNameList: MutableList<Pet> = mutableListOf()

    // variables for RecyclerView
    private lateinit var photoVideoAdapter: PhotoVideoListAdapter
    private lateinit var hashtagAdapter: HashtagListAdapter

    // variables for storing API call(for cancel)
    private var fetchPetApiCall: Call<FetchPetResDto>? = null
    private var fetchPetPhotoApiCall: Call<ResponseBody>? = null
    private var createPostApiCall: Call<CreatePostResDto>? = null
    private var updatePostApiCall: Call<UpdatePostResDto>? = null
    private var fetchPostApiCall: Call<FetchPostResDto>? = null
    private var updatePostMediaApiCall: Call<UpdatePostMediaResDto>? = null
    private var fetchPostMediaApiCall: Call<ResponseBody>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentCreateUpdatePostBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // initialize RecyclerView(photos and videos)
        photoVideoAdapter = PhotoVideoListAdapter(createUpdatePostViewModel, requireContext(), binding)
        binding.photosAndVideosRecyclerView.adapter = photoVideoAdapter
        binding.photosAndVideosRecyclerView.layoutManager = LinearLayoutManager(activity)
        (binding.photosAndVideosRecyclerView.layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
        photoVideoAdapter.setResult(createUpdatePostViewModel.thumbnailList)

        // initialize RecyclerView(hashtags)
        hashtagAdapter = HashtagListAdapter(createUpdatePostViewModel, binding)
        binding.hashtagRecyclerView.adapter = hashtagAdapter
        binding.hashtagRecyclerView.layoutManager = LinearLayoutManager(activity)
        (binding.hashtagRecyclerView.layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
        hashtagAdapter.setResult(createUpdatePostViewModel.hashtagList)

        return root
    }

    override fun onStart() {
        super.onStart()

        // for title
        if(requireActivity().intent.getStringExtra("fragmentType") == "update_post") {
            binding.backButtonTitle.text = context?.getText(R.string.update_post_title)
        }

        // fetch post data for update(if not already fetched)
        if(requireActivity().intent.getStringExtra("fragmentType") == "update_post" &&
                !createUpdatePostViewModel.fetchedPostDataForUpdate) {
                // save post id
                createUpdatePostViewModel.postId = requireActivity().intent.getLongExtra("postId", -1)

                // show loading screen + disable button
                binding.createEditPostMainScrollView.visibility = View.INVISIBLE
                binding.postDataLoadingLayout.visibility = View.VISIBLE
                binding.confirmButton.isEnabled = false

                // fetch post data
                fetchPostData()
        }

        // for view restore and pet spinner
        else {
            setPetSpinnerAndPhoto()
            restoreState()
        }

        // for location switch
        binding.locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            createUpdatePostViewModel.isUsingLocation = isChecked
        }

        // for post photo/video picker
        binding.uploadPhotosAndVideosButton.setOnClickListener {
            if(createUpdatePostViewModel.thumbnailList.size == 10) {
                // show message(photo/video usage full)
                Toast.makeText(context, context?.getText(R.string.photo_video_usage_full_message), Toast.LENGTH_LONG).show()
            }
            else {
                val dialog = Dialog(requireActivity())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.select_photo_video_dialog)
                dialog.show()

                dialog.findViewById<ImageView>(R.id.close_button).setOnClickListener { dialog.dismiss() }
                dialog.findViewById<Button>(R.id.upload_photo_button).setOnClickListener {
                    dialog.dismiss()

                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_PHOTO)
                }
                dialog.findViewById<Button>(R.id.upload_video_button).setOnClickListener {
                    dialog.dismiss()

                    val intent = Intent()
                    intent.type = "video/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(Intent.createChooser(intent, "동영상 선택"), PICK_VIDEO)
                }
            }
        }

        // for disclosure spinner
        ArrayAdapter.createFromResource(requireContext(), R.array.disclosure_array, android.R.layout.simple_spinner_item)
            .also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.disclosureSpinner.adapter = adapter
        }
        binding.disclosureSpinner.setSelection(0, false)
        binding.disclosureSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> {
                        createUpdatePostViewModel.disclosure = DISCLOSURE_PUBLIC
                        binding.disclosureIcon.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_public_24))
                    }
                    1 -> {
                        createUpdatePostViewModel.disclosure = DISCLOSURE_PRIVATE
                        binding.disclosureIcon.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_lock_24))
                    }
                    2 -> {
                        createUpdatePostViewModel.disclosure = DISCLOSURE_FRIEND
                        binding.disclosureIcon.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_group_24))
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        when(createUpdatePostViewModel.disclosure) {
            DISCLOSURE_PUBLIC -> { binding.disclosureSpinner.setSelection(0) }
            DISCLOSURE_PRIVATE -> { binding.disclosureSpinner.setSelection(1) }
            DISCLOSURE_FRIEND -> { binding.disclosureSpinner.setSelection(2) }
        }

        // for hashtag EditText listener
        binding.hashtagInputEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                createUpdatePostViewModel.hashtagEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for hashtag input button
        binding.hashtagInputButton.setOnClickListener {
            // trim hashtag
            createUpdatePostViewModel.hashtagEditText = createUpdatePostViewModel.hashtagEditText.trim()
            binding.hashtagInputEditText.setText(createUpdatePostViewModel.hashtagEditText)

            if(binding.hashtagInputEditText.text.toString() == "") {
                // show message(hashtag empty)
                Toast.makeText(context, context?.getText(R.string.hashtag_empty_message), Toast.LENGTH_LONG).show()
            }
            else if(createUpdatePostViewModel.hashtagList.size == 5) {
                // show message(hashtag usage full)
                Toast.makeText(context, context?.getText(R.string.hashtag_usage_full_message), Toast.LENGTH_LONG).show()
            }
            else {
                // save hashtag
                val hashtag = binding.hashtagInputEditText.text.toString()
                createUpdatePostViewModel.hashtagList.add(hashtag)

                // update RecyclerView
                hashtagAdapter.notifyItemInserted(createUpdatePostViewModel.hashtagList.size)
                binding.hashtagRecyclerView.smoothScrollToPosition(createUpdatePostViewModel.hashtagList.size - 1)

                // reset hashtag EditText
                binding.hashtagInputEditText.setText("")

                // update hashtag usage
                updateHashtagUsage()
            }
        }

        // for post EditText listener
        binding.postEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                createUpdatePostViewModel.postEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for confirm button
        binding.confirmButton.setOnClickListener {
            // trim post content
            createUpdatePostViewModel.postEditText = createUpdatePostViewModel.postEditText.trim()
            binding.postEditText.setText(createUpdatePostViewModel.postEditText)

            if(createUpdatePostViewModel.thumbnailList.size == 0 &&
                createUpdatePostViewModel.postEditText == "") {
                // show message(post invalid)
                Toast.makeText(context, context?.getText(R.string.post_invalid_message), Toast.LENGTH_LONG).show()
            }
            else if(createUpdatePostViewModel.petId == null) {
                // show message(pet not selected)
                Toast.makeText(context, context?.getText(R.string.pet_not_selected_message), Toast.LENGTH_LONG).show()
            }
            else {
                if(requireActivity().intent.getStringExtra("fragmentType") == "create_post") {
                    createPost()
                }
                else {
                    updatePost()
                }
            }
        }

        // for back button
        binding.backButton.setOnClickListener {
            activity?.finish()
        }

        // for hiding keyboard
        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentCreateUpdatePostParentLayout)
    }

    // for photo/video select
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // exception
        if(resultCode != AppCompatActivity.RESULT_OK) { return }

        // get photo/video value
        when(requestCode) {
            PICK_PHOTO -> {
                if(data != null) {
                    // copy selected photo and get real path
                    createUpdatePostViewModel.photoVideoPathList
                        .add(ServerUtil.createCopyAndReturnRealPathLocal(requireActivity(), data.data!!, CREATE_UPDATE_POST_DIRECTORY))

                    // create bytearray
                    val bitmap = BitmapFactory.decodeFile(createUpdatePostViewModel.photoVideoPathList.last())
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val photoByteArray = stream.toByteArray()

                    // save thumbnail
                    val thumbnail = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)
                    createUpdatePostViewModel.thumbnailList.add(thumbnail)

                    // update RecyclerView
                    photoVideoAdapter.notifyItemInserted(createUpdatePostViewModel.thumbnailList.size)
                    binding.photosAndVideosRecyclerView.smoothScrollToPosition(createUpdatePostViewModel.thumbnailList.size - 1)

                    // update photo/video usage
                    updatePhotoVideoUsage()
                }
                else {
                    // show message(file null exception)
                    Toast.makeText(context, context?.getText(R.string.file_null_exception_message), Toast.LENGTH_LONG).show()
                }
            }
            PICK_VIDEO -> {
                if(data != null) {
                    // copy selected photo and get real path
                    createUpdatePostViewModel.photoVideoPathList
                        .add(ServerUtil.createCopyAndReturnRealPathLocal(requireActivity(), data.data!!, CREATE_UPDATE_POST_DIRECTORY))

                    // save thumbnail
                    createUpdatePostViewModel.thumbnailList.add(null)

                    // update RecyclerView
                    photoVideoAdapter.notifyItemInserted(createUpdatePostViewModel.thumbnailList.size)
                    binding.photosAndVideosRecyclerView.smoothScrollToPosition(createUpdatePostViewModel.thumbnailList.size - 1)

                    // update photo/video usage
                    updatePhotoVideoUsage()
                }
                else {
                    // show message(file null exception)
                    Toast.makeText(context, context?.getText(R.string.file_null_exception_message), Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                // show message(file type exception)
                Toast.makeText(context, context?.getText(R.string.file_type_exception_message), Toast.LENGTH_LONG).show()
            }
        }
    }

    // for pet views
    private fun setPetSpinnerAndPhoto() {
        // create DTO
        val fetchPetReqDto = FetchPetReqDto( null )

        fetchPetApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPetReq(fetchPetReqDto)
        fetchPetApiCall!!.enqueue(object: Callback<FetchPetResDto> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<FetchPetResDto>,
                response: Response<FetchPetResDto>
            ) {
                if(response.isSuccessful) {
                    // get pet id and name
                    val apiResponse: MutableList<Pet> = mutableListOf()
                    response.body()?.petList?.map {
                        val item = Pet(
                            it.id, "", it.name, "", "", null, null, false, null, null
                        )
                        apiResponse.add(item)
                    }

                    // reorder items
                    reorderPetList(apiResponse)

                    // set spinner + photo
                    setPetSpinner()
                }
                else {
                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchPetResDto>, t: Throwable) {
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

    // set pet photo
    private fun setPetPhoto() {
        // exception
        if(createUpdatePostViewModel.petId == null) { return }

        // create DTO
        val fetchPetPhotoReqDto = FetchPetPhotoReqDto(createUpdatePostViewModel.petId!!)

        fetchPetPhotoApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPetPhotoReq(fetchPetPhotoReqDto)
        fetchPetPhotoApiCall!!.enqueue(object: Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful) {
                        // set fetched photo to view
                        binding.petPhotoCircleView.setImageBitmap(BitmapFactory.decodeStream(response.body()!!.byteStream()))
                    }
                    else {
                        // get error message
                        val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                        // if null -> set default
                        if(errorMessage == "null") {
                            binding.petPhotoCircleView.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
                        }
                        // else -> show(Toast)/log error message
                        else{
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            Log.d("error", errorMessage)
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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

    // set pet spinner
    private fun setPetSpinner() {
        // set spinner values
        val spinnerArray: ArrayList<String> = ArrayList<String>()
        spinnerArray.add(requireContext().getText(R.string.pet_name_spinner_placeholder).toString())

        for(pet in petIdAndNameList) {
            spinnerArray.add(pet.name)
        }

        val spinnerArrayAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, spinnerArray)
        binding.petNameSpinner.adapter = spinnerArrayAdapter

        // set pet spinner listener
        binding.petNameSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position != 0) {
                    createUpdatePostViewModel.petId = petIdAndNameList[position - 1].id
                    setPetPhoto()
                }
                else {
                    createUpdatePostViewModel.petId = null
                    binding.petPhotoCircleView.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // set spinner position
        if(createUpdatePostViewModel.petId != null) {
            for(i in 0 until petIdAndNameList.size) {
                if(createUpdatePostViewModel.petId == petIdAndNameList[i].id) {
                    binding.petNameSpinner.setSelection(i + 1)
                    return
                }
            }

            // exception(no such pet id) -> reset pet id to null
            createUpdatePostViewModel.petId = null
        }
    }

    // reorder pet list
    private fun reorderPetList(apiResponse: MutableList<Pet>) {
        // get saved pet list order
        val petListOrder = getPetListOrder(PET_LIST_ORDER)

        // sort by order
        petIdAndNameList = mutableListOf()
        for(id in petListOrder) {
            val pet = apiResponse.find { it.id == id }
            petIdAndNameList.add(pet!!)
        }
    }

    // get pet list order
    private fun getPetListOrder(key: String?): MutableList<Long> {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val gson = Gson()
        val json: String? = prefs.getString(key, null)
        val type: Type = object : TypeToken<MutableList<Long>>() {}.type

        if(json == null) { return mutableListOf() }
        return gson.fromJson(json, type)
    }

    // get geolocation
    @SuppressLint("MissingPermission")
    private fun getGeolocation(): MutableList<BigDecimal> {
        val latAndLong: MutableList<BigDecimal> = mutableListOf()

        if(!createUpdatePostViewModel.isUsingLocation) {
            latAndLong.add(0.0.toBigDecimal())
            latAndLong.add(0.0.toBigDecimal())
        }else{
            if (Permission.isAllPermissionsGranted(requireContext(), Permission.requiredPermissionsForLocation)) {
                val location = (requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager)
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if(location != null){
                    latAndLong.add(location.latitude!!.toBigDecimal())
                    latAndLong.add(location.longitude!!.toBigDecimal())
                }else{
                    // 정보 로드 실패 예외처리
                    latAndLong.add(0.0.toBigDecimal())
                    latAndLong.add(0.0.toBigDecimal())
                }
            }else{
                // 특수 처리를 위해 (-1, -1)을 넣음
                latAndLong.add((-1.0).toBigDecimal())
                latAndLong.add((-1.0).toBigDecimal())
            }
        }

        return latAndLong
    }

    // update photo/video usage
    private fun updatePhotoVideoUsage() {
        val uploadedCount = createUpdatePostViewModel.thumbnailList.size
        if(uploadedCount != 0) { binding.uploadPhotoVideoLabel.visibility = View.GONE }
        val photoVideoUsageText = "$uploadedCount/10"
        binding.photoVideoUsage.text = photoVideoUsageText
    }

    // update hashtag usage
    private fun updateHashtagUsage() {
        val hashtagCount = createUpdatePostViewModel.hashtagList.size
        if(hashtagCount != 0) { binding.hashtagRecyclerView.visibility = View.VISIBLE }
        val hashtagUsageText = "$hashtagCount/5"
        binding.hashtagUsage.text = hashtagUsageText
    }

    // set button to loading
    private fun setButtonToLoading() {
        binding.confirmButton.visibility = View.GONE
        binding.createUpdatePostProgressBar.visibility = View.VISIBLE
    }

    // set button to normal
    private fun setButtonToNormal() {
        binding.confirmButton.visibility = View.VISIBLE
        binding.createUpdatePostProgressBar.visibility = View.GONE
    }

    // create post
    private fun createPost() {
        // set api state/button to loading
        createUpdatePostViewModel.apiIsLoading = true
        setButtonToLoading()

        // get location data(if enabled)
        val latAndLong = getGeolocation()

        // 위치 정보 사용에 동의했지만, 권한이 없는 경우
        if(latAndLong[0] == (-1.0).toBigDecimal()){
            // 권한 요청
            Permission.requestNotGrantedPermissions(requireContext(), Permission.requiredPermissionsForLocation)

            // 권한 요청이 비동기적이기 때문에, 권한 요청 이후에 CreatePost 버튼을 다시 눌러야한다.
            setButtonToNormal()
            return
        }

        // create DTO
        val createPostReqDto = CreatePostReqDto(
            createUpdatePostViewModel.petId!!,
            createUpdatePostViewModel.postEditText,
            createUpdatePostViewModel.hashtagList,
            createUpdatePostViewModel.disclosure,
            latAndLong[0],
            latAndLong[1]
        )

        createPostApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .createPostReq(createPostReqDto)
        createPostApiCall!!.enqueue(object: Callback<CreatePostResDto> {
            override fun onResponse(
                call: Call<CreatePostResDto>,
                response: Response<CreatePostResDto>
            ) {
                if (response.isSuccessful) {
                    // Pass post id to Community
                    val intent = Intent()
                    intent.putExtra("postId", response.body()!!.id)
                    requireActivity().setResult(Activity.RESULT_OK, intent)
                    
                    // get created post id + update post media
                    getIdAndUpdateMedia()
                }
                else {
                    // set api state/button to normal
                    createUpdatePostViewModel.apiIsLoading = false
                    setButtonToNormal()

                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<CreatePostResDto>, t: Throwable) {
                // set api state/button to normal
                createUpdatePostViewModel.apiIsLoading = false
                setButtonToNormal()

                // if the view was destroyed(API call canceled) -> return
                if (_binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    // create post
    private fun updatePost() {
        // set api state/button to loading
        createUpdatePostViewModel.apiIsLoading = true
        setButtonToLoading()

        // get location data(if enabled)
        val latAndLong = getGeolocation()

        // 위치 정보 사용에 동의했지만, 권한이 없는 경우
        if(latAndLong[0] == (-1.0).toBigDecimal()){
            // 권한 요청
            Permission.requestNotGrantedPermissions(requireContext(), Permission.requiredPermissionsForLocation)

            // 권한 요청이 비동기적이기 때문에, 권한 요청 이후에 CreatePost 버튼을 다시 눌러야한다.
            setButtonToNormal()
            return
        }

        // create DTO
        val updatePostReqDto = UpdatePostReqDto(
            createUpdatePostViewModel.postId!!,
            createUpdatePostViewModel.petId!!,
            createUpdatePostViewModel.postEditText,
            createUpdatePostViewModel.hashtagList,
            createUpdatePostViewModel.disclosure,
            latAndLong[0],
            latAndLong[1]
        )

        updatePostApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .updatePostReq(updatePostReqDto)
        updatePostApiCall!!.enqueue(object: Callback<UpdatePostResDto> {
            override fun onResponse(
                call: Call<UpdatePostResDto>,
                response: Response<UpdatePostResDto>
            ) {
                if (response.isSuccessful) {
                    // no media files
                    if(createUpdatePostViewModel.photoVideoPathList.size == 0) {
                        // TODO: delete all media files(server API needed)

                        // Pass post id, position to Community
                        passDataToCommunity()

                        // close after success
                        closeAfterSuccess()
                    }

                    // update post media
                    else { updatePostMedia(createUpdatePostViewModel.postId!!) }
                }
                else {
                    // set api state/button to normal
                    createUpdatePostViewModel.apiIsLoading = false
                    setButtonToNormal()

                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<UpdatePostResDto>, t: Throwable) {
                // set api state/button to normal
                createUpdatePostViewModel.apiIsLoading = false
                setButtonToNormal()

                // if the view was destroyed(API call canceled) -> return
                if (_binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    // update post media
    private fun updatePostMedia(id: Long) {
        // exception(no media files)
        if(createUpdatePostViewModel.photoVideoPathList.size == 0) { closeAfterSuccess() }

        else {
            // create file list
            val fileList: ArrayList<MultipartBody.Part> = ArrayList()
            for(i in 0 until createUpdatePostViewModel.photoVideoPathList.size) {
                val fileName = "file_$i" + createUpdatePostViewModel.photoVideoPathList[i]
                    .substring(createUpdatePostViewModel.photoVideoPathList[i].lastIndexOf("."))

                fileList.add(MultipartBody.Part.createFormData("fileList", fileName,
                    RequestBody.create(MediaType.parse("multipart/form-data"), File(createUpdatePostViewModel.photoVideoPathList[i]))))
            }

            // API call
            updatePostMediaApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .updatePostMediaReq(id, fileList)
            updatePostMediaApiCall!!.enqueue(object: Callback<UpdatePostMediaResDto> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<UpdatePostMediaResDto>,
                    response: Response<UpdatePostMediaResDto>
                ) {
                    if(response.isSuccessful) {
                        // Pass post id, position to Community
                        passDataToCommunity()

                        // close after success
                        closeAfterSuccess()
                    }
                    else {
                        // set api state/button to normal
                        createUpdatePostViewModel.apiIsLoading = false
                        setButtonToNormal()

                        // get error message + show(Toast)
                        val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                        // log error message
                        Log.d("error", errorMessage)
                    }
                }

                override fun onFailure(call: Call<UpdatePostMediaResDto>, t: Throwable) {
                    // set api state/button to normal
                    createUpdatePostViewModel.apiIsLoading = false
                    setButtonToNormal()

                    // show(Toast)/log error message
                    Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                    Log.d("error", t.message.toString())
                }
            })
        }
    }

    // get created pet id + update pet photo
    private fun getIdAndUpdateMedia() {
        // create DTO
        val fetchPostReqDto = FetchPostReqDto(0, null, createUpdatePostViewModel.petId, null)

        fetchPostApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPostReq(fetchPostReqDto)
        fetchPostApiCall!!.enqueue(object: Callback<FetchPostResDto> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<FetchPostResDto>,
                response: Response<FetchPostResDto>
            ) {
                if(response.isSuccessful) {
                    val postId = (response.body()?.postList?.get(0) as Post).id

                    // update post media
                    updatePostMedia(postId)
                }
                else {
                    // set api state/button to normal
                    createUpdatePostViewModel.apiIsLoading = false
                    setButtonToNormal()

                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchPostResDto>, t: Throwable) {
                // set api state/button to normal
                createUpdatePostViewModel.apiIsLoading = false
                setButtonToNormal()

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

    private fun fetchPostMediaData(postMedia: Array<FileMetaData>) {
        for(index in postMedia.indices) {
            // create DTO
            val fetchPostMediaReqDto = FetchPostMediaReqDto(createUpdatePostViewModel.postId!!, index)

            // API call
            fetchPostMediaApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .fetchPostMediaReq(fetchPostMediaReqDto)
            fetchPostMediaApiCall!!.enqueue(object: Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    // if the view was destroyed(API call canceled) -> return
                    if(_binding == null) {
                        return
                    }

                    if(response.isSuccessful) {
                        // get file extension
                        val extension = postMedia[index].name.split('.').last()

                        // copy file and get real path
                        val mediaByteArray = response.body()!!.byteStream().readBytes()
                        createUpdatePostViewModel.photoVideoPathList[index] =
                            ServerUtil.createCopyAndReturnRealPathServer(context!!, mediaByteArray, extension, CREATE_UPDATE_POST_DIRECTORY)

                        // check if image and save thumbnail(video thumbnails are created in the RecyclerView adapter)
                        if("image" in MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)!!) {
                            val thumbnail = BitmapFactory.decodeByteArray(mediaByteArray, 0, mediaByteArray.size)
                            createUpdatePostViewModel.thumbnailList[index] = thumbnail
                        }
                        else {
                            createUpdatePostViewModel.thumbnailList[index] = null
                        }

                        // if all is done fetching -> set RecyclerView + set usage + show main ScrollView
                        if("" !in createUpdatePostViewModel.photoVideoPathList) {
                            // update RecyclerView and photo/video usage
                            photoVideoAdapter.setResult(createUpdatePostViewModel.thumbnailList)
                            updatePhotoVideoUsage()

                            // show loading screen + disable button
                            binding.createEditPostMainScrollView.visibility = View.VISIBLE
                            binding.postDataLoadingLayout.visibility = View.GONE
                            binding.confirmButton.isEnabled = true

                            // set fetched to true
                            createUpdatePostViewModel.fetchedPostDataForUpdate = true

                            // set views with post data
                            setPetSpinnerAndPhoto()
                            restoreState()
                        }
                    }
                    else {
                        // get error message
                        val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                        // Toast + Log
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        Log.d("error", errorMessage)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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
    }

    private fun fetchPostData() {
        // create DTO
        val fetchPostReqDto = FetchPostReqDto(null, null, null, createUpdatePostViewModel.postId)

        // API call
        fetchPostApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPostReq(fetchPostReqDto)
        fetchPostApiCall!!.enqueue(object: Callback<FetchPostResDto> {
            override fun onResponse(
                call: Call<FetchPostResDto>,
                response: Response<FetchPostResDto>
            ) {
                if(response.isSuccessful) {
                    // fetch post data(excluding media) and save to ViewModel
                    val post = response.body()?.postList!![0]

                    createUpdatePostViewModel.petId = post.pet.id
                    createUpdatePostViewModel.isUsingLocation = post.geoTagLat != 0.0
                    createUpdatePostViewModel.disclosure = post.disclosure
                    if(post.serializedHashTags != "") {
                        createUpdatePostViewModel.hashtagList = post.serializedHashTags.split(',').toMutableList()
                        hashtagAdapter.setResult(createUpdatePostViewModel.hashtagList)
                    }
                    createUpdatePostViewModel.postEditText = post.contents

                    // fetch post media data
                    if(post.mediaAttachments != null) {
                        val postMedia = Gson().fromJson(post.mediaAttachments, Array<FileMetaData>::class.java)

                        // initialize lists
                        for(i in postMedia.indices) {
                            createUpdatePostViewModel.photoVideoPathList.add("")
                            createUpdatePostViewModel.thumbnailList.add(null)
                        }

                        fetchPostMediaData(postMedia)
                    }
                    else {
                        // show loading screen + disable button
                        binding.createEditPostMainScrollView.visibility = View.VISIBLE
                        binding.postDataLoadingLayout.visibility = View.GONE
                        binding.confirmButton.isEnabled = true

                        // set fetched to true
                        createUpdatePostViewModel.fetchedPostDataForUpdate = true

                        // set views with post data
                        setPetSpinnerAndPhoto()
                        restoreState()
                    }
                }
                else {
                    // close activity
                    requireActivity().finish()

                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchPostResDto>, t: Throwable) {
                // close activity
                requireActivity().finish()

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

    // Pass post id, position to Community(for post edit)
    private fun passDataToCommunity() {
        val intent = Intent()
        intent.putExtra("postId", requireActivity().intent.getLongExtra("postId", -1))
        intent.putExtra("position", requireActivity().intent.getIntExtra("position", -1))
        requireActivity().setResult(Activity.RESULT_OK, intent)
    }

    // close after success
    private fun closeAfterSuccess() {
        // set api state/button to normal
        createUpdatePostViewModel.apiIsLoading = false
        setButtonToNormal()

        // delete copied files(if any)
        if(isRemoving || requireActivity().isFinishing) {
            Util.deleteCopiedFiles(requireContext(), CREATE_UPDATE_POST_DIRECTORY)
        }

        // show message + return to previous activity
        if(requireActivity().intent.getStringExtra("fragmentType") == "create_post") {
            Toast.makeText(context, context?.getText(R.string.create_post_successful), Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(context, context?.getText(R.string.update_post_successful), Toast.LENGTH_LONG).show()
        }

        activity?.finish()
    }

    // for view restore
    private fun restoreState() {
        // restore location switch
        binding.locationSwitch.isChecked = createUpdatePostViewModel.isUsingLocation

        // restore photo/video upload layout
        updatePhotoVideoUsage()

        // restore disclosure spinner
        when(createUpdatePostViewModel.disclosure) {
            DISCLOSURE_PUBLIC -> { binding.disclosureSpinner.setSelection(0) }
            DISCLOSURE_PRIVATE -> { binding.disclosureSpinner.setSelection(1) }
            DISCLOSURE_FRIEND -> { binding.disclosureSpinner.setSelection(2) }
        }

        // restore hashtag layout
        binding.hashtagInputEditText.setText(createUpdatePostViewModel.hashtagEditText)
        updateHashtagUsage()

        // restore post EditText
        binding.postEditText.setText(createUpdatePostViewModel.postEditText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // delete copied files(if any)
        if(isRemoving || requireActivity().isFinishing) {
            Util.deleteCopiedFiles(requireContext(), CREATE_UPDATE_POST_DIRECTORY)
        }

        // stop api call when fragment is destroyed
        fetchPetApiCall?.cancel()
        fetchPetPhotoApiCall?.cancel()
        createPostApiCall?.cancel()
        fetchPostApiCall?.cancel()
        updatePostMediaApiCall?.cancel()
        fetchPostMediaApiCall?.cancel()
    }
}