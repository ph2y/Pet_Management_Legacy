package com.sju18001.petmanagement.ui.community.post.createUpdatePost

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Permission
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCreateUpdatePostBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.restapi.global.FileMetaData
import com.sju18001.petmanagement.restapi.global.FileType
import com.sju18001.petmanagement.ui.myPet.petManager.PetManagerFragment
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigDecimal

class CreateUpdatePostFragment : Fragment() {

    // constant variables
    private val PICK_PHOTO = 0
    private val PICK_VIDEO = 1
    private val PICK_GENERAL_FILE = 2
    private var DISCLOSURE_PUBLIC: String = "PUBLIC"
    private var DISCLOSURE_PRIVATE: String = "PRIVATE"
    private var DISCLOSURE_FRIEND: String = "FRIEND"
    private var CREATE_UPDATE_POST_DIRECTORY: String = "create_update_post"

    // variable for ViewModel
    private val createUpdatePostViewModel: CreateUpdatePostViewModel by activityViewModels()

    // variables for view binding
    private var _binding: FragmentCreateUpdatePostBinding? = null
    private val binding get() = _binding!!

    private var isViewDestroyed = false

    // variables for RecyclerView
    private lateinit var petAdapter: PetListAdapter
    private lateinit var photoAdapter: PhotoListAdapter
    private lateinit var generalFilesAdapter: GeneralFileListAdapter
    private lateinit var hashtagAdapter: HashtagListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateUpdatePostBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerViews()
    }

    override fun onStart() {
        super.onStart()

        // for title
        if(requireActivity().intent.getStringExtra("fragmentType") == "update_post") {
            binding.backButtonTitle.text = context?.getText(R.string.update_post_title)
        }

        // fetch post data for update (if not already fetched)
        if(requireActivity().intent.getStringExtra("fragmentType") == "update_post"
            && (!createUpdatePostViewModel.fetchedPostDataForUpdate || !createUpdatePostViewModel.fetchedPetData)) {
                // save post id
                createUpdatePostViewModel.postId = requireActivity().intent.getLongExtra("postId", -1)

                showLoadingScreen()
                fetchPostData()
        }

        // fetch pet data + set pet recyclerview (if not already fetched)
        else if (!createUpdatePostViewModel.fetchedPetData) {
            showLoadingScreen()
            fetchPetDataAndSetRecyclerView()
        }

        restoreState()

        // for location switch
        binding.locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            createUpdatePostViewModel.isUsingLocation = isChecked
        }

        // for upload file button
        binding.uploadFileButton.setOnClickListener {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.select_file_type_dialog)
            dialog.show()

            dialog.findViewById<ImageView>(R.id.close_button).setOnClickListener { dialog.dismiss() }
            dialog.findViewById<Button>(R.id.upload_photo_button).setOnClickListener {
                if(createUpdatePostViewModel.photoThumbnailList.size == 10) {
                    Toast.makeText(
                        context,
                        context?.getText(R.string.photo_video_usage_full_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else {
                    dialog.dismiss()

                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_PHOTO)
                }
            }
            dialog.findViewById<Button>(R.id.upload_video_button).setOnClickListener {
                dialog.dismiss()

                // TODO: implement logic for uploading videos
//                val intent = Intent()
//                intent.type = "video/*"
//                intent.action = Intent.ACTION_GET_CONTENT
//                startActivityForResult(Intent.createChooser(intent, "동영상 선택"), PICK_VIDEO)
            }
            dialog.findViewById<Button>(R.id.upload_general_button).setOnClickListener {
                dialog.dismiss()

                val intent = Intent()
                intent.type = "*/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "파일 선택"), PICK_GENERAL_FILE)
            }
            dialog.findViewById<Button>(R.id.upload_audio_button).setOnClickListener {
                dialog.dismiss()

                // TODO: implement logic for uploading audio files
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
        binding.hashtagInputEditText.setOnEditorActionListener{ _, _, _ ->
            addHashtag()
            true
        }
        binding.hashtagInputEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                createUpdatePostViewModel.hashtagEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for hashtag input button
        binding.hashtagInputButton.setOnClickListener {
            addHashtag()
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

            if(isPostEmpty()) {
                Toast.makeText(context, context?.getText(R.string.post_invalid_message), Toast.LENGTH_LONG).show()
            }
            else if(createUpdatePostViewModel.selectedPetId == null) {
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

        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentCreateUpdatePostParentLayout)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // exception
        if(resultCode != AppCompatActivity.RESULT_OK) { return }

        // get selected file value
        when(requestCode) {
            PICK_PHOTO -> {
                if(data != null) {
                    // get file name
                    val fileName = Util.getSelectedFileName(requireContext(), data.data!!)

                    // copy selected photo and get real path
                    val postPhotoPathValue = ServerUtil.createCopyAndReturnRealPathLocal(requireActivity(),
                        data.data!!, CREATE_UPDATE_POST_DIRECTORY, fileName)

                    // no extension exception
                    if (postPhotoPathValue.isEmpty()) {
                        Toast.makeText(context, context?.getText(R.string.photo_file_type_exception_message), Toast.LENGTH_LONG).show()
                        return
                    }

                    // file type exception -> delete copied file + show Toast message
                    if (!Util.isUrlPhoto(postPhotoPathValue)) {
                        Toast.makeText(context, context?.getText(R.string.photo_file_type_exception_message), Toast.LENGTH_LONG).show()
                        File(postPhotoPathValue).delete()
                        return
                    }

                    // add path to list
                    createUpdatePostViewModel.photoPathList.add(postPhotoPathValue)

                    // create bytearray for thumbnail
                    val bitmap = BitmapFactory.decodeFile(createUpdatePostViewModel.photoPathList.last())
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val photoByteArray = stream.toByteArray()

                    // save thumbnail
                    val thumbnail = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)
                    createUpdatePostViewModel.photoThumbnailList.add(thumbnail)

                    // update RecyclerView
                    photoAdapter.notifyItemInserted(createUpdatePostViewModel.photoThumbnailList.size)
                    binding.photosRecyclerView.smoothScrollToPosition(createUpdatePostViewModel.photoThumbnailList.size - 1)

                    updatePhotoUsage()
                }
                else {
                    Toast.makeText(context, context?.getText(R.string.file_null_exception_message), Toast.LENGTH_LONG).show()
                }
            }
            // TODO: implement logic for uploading videos
//            PICK_VIDEO -> {
//                if(data != null) {
//                    // copy selected video and get real path
//                    val postVideoPathValue = ServerUtil.createCopyAndReturnRealPathLocal(requireActivity(),
//                        data.data!!, CREATE_UPDATE_POST_DIRECTORY)
//
//                    // file type exception -> delete copied file + show Toast message
//                    if (!Util.isUrlVideo(postVideoPathValue)) {
//                        Toast.makeText(context, context?.getText(R.string.video_file_type_exception_message), Toast.LENGTH_LONG).show()
//                        File(postVideoPathValue).delete()
//                        return
//                    }
//
//                    // add path to list
//                    createUpdatePostViewModel.photoPathList.add(postVideoPathValue)
//
//                    // save thumbnail
//                    createUpdatePostViewModel.thumbnailList.add(null)
//
//                    // update RecyclerView
//                    photoAdapter.notifyItemInserted(createUpdatePostViewModel.thumbnailList.size)
//                    binding.photosRecyclerView.smoothScrollToPosition(createUpdatePostViewModel.thumbnailList.size - 1)
//
//                    updatePhotoVideoUsage()
//                }
//                else {
//                    Toast.makeText(context, context?.getText(R.string.file_null_exception_message), Toast.LENGTH_LONG).show()
//                }
//            }
//            else -> {
//                Toast.makeText(context, context?.getText(R.string.file_type_exception_message), Toast.LENGTH_LONG).show()
//            }
            PICK_GENERAL_FILE -> {
                if(data != null) {
                    // get file name
                    val fileName = Util.getSelectedFileName(requireContext(), data.data!!)

                    // duplicate file exception
                    if (fileName in createUpdatePostViewModel.generalFileNameList) {
                        Toast.makeText(requireContext(), requireContext()
                            .getText(R.string.duplicate_file_exception_message), Toast.LENGTH_SHORT).show()
                        return
                    }

                    // copy selected general file and get real path
                    val postGeneralFilePathValue = ServerUtil.createCopyAndReturnRealPathLocal(requireActivity(),
                        data.data!!, CREATE_UPDATE_POST_DIRECTORY,fileName)

                    // file type exception -> delete copied file + show Toast message
                    if (!Util.isUrlGeneralFile(postGeneralFilePathValue)) {
                        Toast.makeText(context, context?.getText(R.string.general_file_type_exception_message), Toast.LENGTH_LONG).show()
                        File(postGeneralFilePathValue).delete()
                        return
                    }

                    // add path to list
                    createUpdatePostViewModel.generalFilePathList.add(postGeneralFilePathValue)

                    // save file name
                    val generalFileName = createUpdatePostViewModel.generalFilePathList.last()
                        .substring(createUpdatePostViewModel.generalFilePathList.last().lastIndexOf("/") + 1)
                    createUpdatePostViewModel.generalFileNameList.add(generalFileName)

                    // update RecyclerView
                    generalFilesAdapter.notifyItemInserted(createUpdatePostViewModel.generalFileNameList.size)
                    binding.generalRecyclerView.smoothScrollToPosition(createUpdatePostViewModel.generalFileNameList.size - 1)

                    updateGeneralUsage()
                }
                else {
                    Toast.makeText(context, context?.getText(R.string.file_null_exception_message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initializeRecyclerViews() {
        // initialize RecyclerView (for pet)
        petAdapter = PetListAdapter(createUpdatePostViewModel, requireContext())
        binding.petRecyclerView.adapter = petAdapter
        binding.petRecyclerView.layoutManager = LinearLayoutManager(activity)
        (binding.petRecyclerView.layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
        petAdapter.updateDataSet(createUpdatePostViewModel.petList)

        // initialize RecyclerView (for photos)
        photoAdapter = PhotoListAdapter(createUpdatePostViewModel, requireContext(), binding)
        binding.photosRecyclerView.adapter = photoAdapter
        binding.photosRecyclerView.layoutManager = LinearLayoutManager(activity)
        (binding.photosRecyclerView.layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
        photoAdapter.setResult(createUpdatePostViewModel.photoThumbnailList)

        // TODO: initialize RecyclerView (for videos)

        // initialize RecyclerView (for general files)
        generalFilesAdapter = GeneralFileListAdapter(createUpdatePostViewModel, requireContext(), binding)
        binding.generalRecyclerView.adapter = generalFilesAdapter
        binding.generalRecyclerView.layoutManager = LinearLayoutManager(activity)
        (binding.generalRecyclerView.layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
        generalFilesAdapter.setResult(createUpdatePostViewModel.generalFileNameList)

        // TODO: initialize RecyclerView (for audio files)

        // initialize RecyclerView (for hashtags)
        hashtagAdapter = HashtagListAdapter(createUpdatePostViewModel, binding)
        binding.hashtagRecyclerView.adapter = hashtagAdapter
        binding.hashtagRecyclerView.layoutManager = LinearLayoutManager(activity)
        (binding.hashtagRecyclerView.layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
        hashtagAdapter.setResult(createUpdatePostViewModel.hashtagList)
    }

    private fun fetchPetDataAndSetRecyclerView() {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPetReq(FetchPetReqDto( null , null))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            // fetch pet info (unsorted)
            val unsortedPetList: MutableList<PetListItem> = mutableListOf()
            response.body()?.petList?.map {
                val item = PetListItem(
                    it.id, it.photoUrl, null, it.name, it.id == createUpdatePostViewModel.selectedPetId
                )
                unsortedPetList.add(item)
            }

            reorderPetList(unsortedPetList)
            fetchPetPhotos()
        }, {}, {})
    }

    private fun reorderPetList(apiResponse: MutableList<PetListItem>) {
        // get saved pet list order
        val petListOrder = PetManagerFragment()
            .getPetListOrder(requireContext().getString(R.string.data_name_pet_list_id_order), requireContext())

        // sort by order
        for (id in petListOrder) {
            val item = apiResponse.find { it.petId == id }

            if (item!!.isSelected) {
                createUpdatePostViewModel.selectedPetIndex = createUpdatePostViewModel.petList.size
            }
            createUpdatePostViewModel.petList.add(item)
        }
    }

    private fun fetchPetPhotos() {
        val fetchedFlags = BooleanArray(createUpdatePostViewModel.petList.size) { false }

        for (i in 0 until createUpdatePostViewModel.petList.size) {
            // if no photo
            if (createUpdatePostViewModel.petList[i].petPhotoUrl == null) {
                fetchedFlags[i] = true
                continue
            }

            // fetch pet photo
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .fetchPetPhotoReq(FetchPetPhotoReqDto(createUpdatePostViewModel.petList[i].petId))
            ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
                createUpdatePostViewModel.petList[i].petPhoto = BitmapFactory.decodeStream(response.body()!!.byteStream())
                fetchedFlags[i] = true

                if (fetchedFlags.all{true}) {
                    createUpdatePostViewModel.fetchedPetData = true
                    petAdapter.updateDataSet(createUpdatePostViewModel.petList)

                    hideLoadingScreen()
                    restoreState()
                }
            }, {}, {})
        }
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

    // update photo usage
    private fun updatePhotoUsage() {
        val uploadedCount = createUpdatePostViewModel.photoThumbnailList.size
        if (uploadedCount == 0) {
            binding.uploadPhotoLayout.visibility = View.GONE
        }
        else {
            binding.uploadPhotoLayout.visibility = View.VISIBLE
        }
        val photoUsageText = "$uploadedCount/10"
        binding.photoUsage.text = photoUsageText
    }

    // update general usage
    private fun updateGeneralUsage() {
        val uploadedCount = createUpdatePostViewModel.generalFileNameList.size

        if (uploadedCount == 0) {
            binding.uploadGeneralLayout.visibility = View.GONE
        }
        else {
            binding.uploadGeneralLayout.visibility = View.VISIBLE
        }
        val generalUsageText = "$uploadedCount/10"
        binding.generalUsage.text = generalUsageText
    }

    // update hashtag usage
    private fun updateHashtagUsage() {
        val hashtagCount = createUpdatePostViewModel.hashtagList.size
        if(hashtagCount != 0) { binding.hashtagRecyclerView.visibility = View.VISIBLE }
        val hashtagUsageText = "$hashtagCount/5"
        binding.hashtagUsage.text = hashtagUsageText
    }

    private fun addHashtag(){
        createUpdatePostViewModel.hashtagEditText = createUpdatePostViewModel.hashtagEditText.trim()
        binding.hashtagInputEditText.setText(createUpdatePostViewModel.hashtagEditText)

        if(binding.hashtagInputEditText.text.toString() == "") {
            Toast.makeText(context, context?.getText(R.string.hashtag_empty_message), Toast.LENGTH_LONG).show()
        }
        else if(createUpdatePostViewModel.hashtagList.size == 5) {
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

            updateHashtagUsage()
        }
    }

    private fun showLoadingScreen() {
        binding.createUpdatePostMainScrollView.visibility = View.INVISIBLE
        binding.postDataLoadingLayout.visibility = View.VISIBLE
        binding.confirmButton.isEnabled = false
    }

    private fun hideLoadingScreen() {
        binding.createUpdatePostMainScrollView.visibility = View.VISIBLE
        binding.postDataLoadingLayout.visibility = View.GONE
        binding.confirmButton.isEnabled = true
    }

    private fun lockViews() {
        binding.confirmButton.visibility = View.GONE
        binding.createUpdatePostProgressBar.visibility = View.VISIBLE

        binding.petRecyclerView.let {
            for(i in 0..petAdapter.itemCount) {
                it.findViewHolderForLayoutPosition(i)?.itemView?.isClickable = false
            }
        }
        binding.locationSwitch.isEnabled = false
        binding.uploadFileButton.isEnabled = false
        binding.disclosureSpinner.isEnabled = false
        binding.hashtagInputEditText.isEnabled = false
        binding.hashtagInputButton.isEnabled = false
        binding.postEditText.isEnabled = false
        binding.photosRecyclerView.let {
            for(i in 0..photoAdapter.itemCount) {
                it.findViewHolderForLayoutPosition(i)?.itemView?.findViewById<ImageView>(R.id.delete_button)?.visibility = View.GONE
            }
        }
        binding.generalRecyclerView.let {
            for(i in 0..generalFilesAdapter.itemCount) {
                it.findViewHolderForLayoutPosition(i)?.itemView?.findViewById<ImageView>(R.id.delete_button)?.visibility = View.GONE
            }
        }
        binding.hashtagRecyclerView.let {
            for(i in 0..hashtagAdapter.itemCount) {
                it.findViewHolderForLayoutPosition(i)?.itemView?.findViewById<ImageView>(R.id.delete_button)?.visibility = View.GONE
            }
        }
        binding.backButton.isEnabled = false
    }

    private fun unlockViews() {
        binding.confirmButton.visibility = View.VISIBLE
        binding.createUpdatePostProgressBar.visibility = View.GONE

        binding.petRecyclerView.let {
            for(i in 0..petAdapter.itemCount) {
                it.findViewHolderForLayoutPosition(i)?.itemView?.isClickable = true
            }
        }
        binding.locationSwitch.isEnabled = true
        binding.uploadFileButton.isEnabled = true
        binding.disclosureSpinner.isEnabled = true
        binding.hashtagInputEditText.isEnabled = true
        binding.hashtagInputButton.isEnabled = true
        binding.postEditText.isEnabled = true
        binding.photosRecyclerView.let {
            for(i in 0..photoAdapter.itemCount) {
                it.findViewHolderForLayoutPosition(i)?.itemView?.findViewById<ImageView>(R.id.delete_button)?.visibility = View.VISIBLE
            }
        }
        binding.generalRecyclerView.let {
            for(i in 0..generalFilesAdapter.itemCount) {
                it.findViewHolderForLayoutPosition(i)?.itemView?.findViewById<ImageView>(R.id.delete_button)?.visibility = View.VISIBLE
            }
        }
        binding.hashtagRecyclerView.let {
            for(i in 0..hashtagAdapter.itemCount) {
                it.findViewHolderForLayoutPosition(i)?.itemView?.findViewById<ImageView>(R.id.delete_button)?.visibility = View.VISIBLE
            }
        }
        binding.backButton.isEnabled = true
    }

    private fun isPostEmpty(): Boolean {
        return createUpdatePostViewModel.photoThumbnailList.size == 0 &&
                createUpdatePostViewModel.generalFileNameList.size == 0 &&
                createUpdatePostViewModel.postEditText.trim().isEmpty()
    }

    private fun isApiLoadComplete(): Boolean {
        return createUpdatePostViewModel.updatedPostPhotoData && createUpdatePostViewModel.updatedPostGeneralFileData &&
                createUpdatePostViewModel.deletedPostPhotoData && createUpdatePostViewModel.deletedPostGeneralFileData
    }

    private fun createPost() {
        // set api state/button to loading
        createUpdatePostViewModel.apiIsLoading = true
        lockViews()

        // get location data(if enabled)
        val latAndLong = getGeolocation()

        // 위치 정보 사용에 동의했지만, 권한이 없는 경우
        if(latAndLong[0] == (-1.0).toBigDecimal()){
            Permission.requestNotGrantedPermissions(requireContext(), Permission.requiredPermissionsForLocation)

            // 권한 요청이 비동기적이기 때문에, 권한 요청 이후에 CreatePost 버튼을 다시 눌러야한다.
            unlockViews()
            return
        }

        // create DTO
        val createPostReqDto = CreatePostReqDto(
            createUpdatePostViewModel.selectedPetId!!,
            createUpdatePostViewModel.postEditText,
            createUpdatePostViewModel.hashtagList,
            createUpdatePostViewModel.disclosure,
            latAndLong[0],
            latAndLong[1]
        )

        // set deleted to true (because there is nothing to delete)
        createUpdatePostViewModel.deletedPostPhotoData = true
        createUpdatePostViewModel.deletedPostGeneralFileData = true

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .createPostReq(createPostReqDto)
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            requireActivity().intent.putExtra("postId", response.body()!!.id)
            updatePostMedia(response.body()!!.id)
            // TODO: create logic for updating video files
            updatePostGeneralFiles(response.body()!!.id)
            // TODO: create logic for updating audio files
        }, {
            // set api state/button to normal
            createUpdatePostViewModel.apiIsLoading = false
            unlockViews()
        }, {
            // set api state/button to normal
            createUpdatePostViewModel.apiIsLoading = false
            unlockViews()
        })
    }

    private fun updatePost() {
        // set api state/button to loading
        createUpdatePostViewModel.apiIsLoading = true
        lockViews()

        // get location data(if enabled)
        val latAndLong = getGeolocation()

        // 위치 정보 사용에 동의했지만, 권한이 없는 경우
        if(latAndLong[0] == (-1.0).toBigDecimal()){
            Permission.requestNotGrantedPermissions(requireContext(), Permission.requiredPermissionsForLocation)

            // 권한 요청이 비동기적이기 때문에, 권한 요청 이후에 CreatePost 버튼을 다시 눌러야한다.
            unlockViews()
            return
        }

        // create DTO
        val updatePostReqDto = UpdatePostReqDto(
            createUpdatePostViewModel.postId!!,
            createUpdatePostViewModel.selectedPetId!!,
            createUpdatePostViewModel.postEditText,
            createUpdatePostViewModel.hashtagList,
            createUpdatePostViewModel.disclosure,
            latAndLong[0],
            latAndLong[1]
        )

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .updatePostReq(updatePostReqDto)
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            // update media (photos and videos) TODO: create logic for updating videos
            if(createUpdatePostViewModel.photoPathList.size == 0) {
                // 기존에 Media가 0개였다면 FileType.IMAGE_FILE에 대해 DeletePostFile를 호출하지 않는다
                if(requireActivity().intent.getIntExtra("originalMediaCount", 0) > 0){
                    createUpdatePostViewModel.updatedPostPhotoData = true
                    deletePostFile(createUpdatePostViewModel.postId!!, FileType.IMAGE_FILE)
                }else{
                    createUpdatePostViewModel.updatedPostPhotoData = true
                    createUpdatePostViewModel.deletedPostPhotoData = true

                    if (isApiLoadComplete()) {
                        passDataToCommunity()
                        closeAfterSuccess()
                    }
                }
            } else {
                createUpdatePostViewModel.deletedPostPhotoData = true
                updatePostMedia(createUpdatePostViewModel.postId!!)
            }

            // update general files
            if(createUpdatePostViewModel.generalFilePathList.size == 0) {
                // 기존에 General Files가 0개였다면 FileType.GENERAL_FILE에 대해 DeletePostFile를 호출하지 않는다
                if(requireActivity().intent.getIntExtra("originalGeneralFilesCount", 0) > 0){
                    createUpdatePostViewModel.updatedPostGeneralFileData = true
                    deletePostFile(createUpdatePostViewModel.postId!!, FileType.GENERAL_FILE)
                }else{
                    createUpdatePostViewModel.updatedPostGeneralFileData = true
                    createUpdatePostViewModel.deletedPostGeneralFileData = true

                    if (isApiLoadComplete()) {
                        passDataToCommunity()
                        closeAfterSuccess()
                    }
                }
            } else {
                createUpdatePostViewModel.deletedPostGeneralFileData = true
                updatePostGeneralFiles(createUpdatePostViewModel.postId!!)
            }

            // TODO: create logic for updating audio files
        }, {
            // set api state/button to normal
            createUpdatePostViewModel.apiIsLoading = false
            unlockViews()
        }, {
            createUpdatePostViewModel.apiIsLoading = false
            unlockViews()
        })
    }

    private fun deletePostFile(id: Long, fileType: String) {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .deletePostFileReq(DeletePostFileReqDto(id, fileType))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            if (fileType == FileType.IMAGE_FILE) {
                createUpdatePostViewModel.deletedPostPhotoData = true
            }
            if (fileType == FileType.GENERAL_FILE) {
                createUpdatePostViewModel.deletedPostGeneralFileData = true
            }

            if (isApiLoadComplete()) {
                passDataToCommunity()
                closeAfterSuccess()
            }
        }, {
            // set api state/button to normal
            createUpdatePostViewModel.apiIsLoading = false
            unlockViews()
        }, {
            createUpdatePostViewModel.apiIsLoading = false
            unlockViews()
        })
    }

    private fun updatePostMedia(id: Long) {
        // exception (no photo files)
        if(createUpdatePostViewModel.photoPathList.size == 0) {
            createUpdatePostViewModel.updatedPostPhotoData = true

            if (isApiLoadComplete()) {
                passDataToCommunity()
                closeAfterSuccess()
            }
        } else {
            // create file list
            val fileList: ArrayList<MultipartBody.Part> = ArrayList()
            for(i in 0 until createUpdatePostViewModel.photoPathList.size) {
                val fileName = "file_$i" + createUpdatePostViewModel.photoPathList[i]
                    .substring(createUpdatePostViewModel.photoPathList[i].lastIndexOf("."))

                fileList.add(MultipartBody.Part.createFormData("fileList", fileName,
                    RequestBody.create(MediaType.parse("multipart/form-data"), File(createUpdatePostViewModel.photoPathList[i]))))
            }

            // API call
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .updatePostFileReq(id, fileList, FileType.IMAGE_FILE)
            ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
                createUpdatePostViewModel.updatedPostPhotoData = true

                if (isApiLoadComplete()) {
                    passDataToCommunity()
                    closeAfterSuccess()
                }
            }, {
                // set api state/button to normal
                createUpdatePostViewModel.apiIsLoading = false
                unlockViews()
            }, {
                createUpdatePostViewModel.apiIsLoading = false
                unlockViews()
            })
        }
    }

    private fun updatePostGeneralFiles(id: Long) {
        // exception (no general files)
        if (createUpdatePostViewModel.generalFilePathList.size == 0) {
            createUpdatePostViewModel.updatedPostGeneralFileData = true

            if (isApiLoadComplete()) {
                passDataToCommunity()
                closeAfterSuccess()
            }
        } else {
            // create file list
            val fileList: ArrayList<MultipartBody.Part> = ArrayList()
            for(i in 0 until createUpdatePostViewModel.generalFilePathList.size) {
                val fileName = createUpdatePostViewModel.generalFileNameList[i]

                fileList.add(MultipartBody.Part.createFormData("fileList", fileName,
                    RequestBody.create(MediaType.parse("multipart/form-data"), File(createUpdatePostViewModel.generalFilePathList[i]))))
            }

            // API call
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .updatePostFileReq(id, fileList, FileType.GENERAL_FILE)
            ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
                createUpdatePostViewModel.updatedPostGeneralFileData = true

                if (isApiLoadComplete()) {
                    passDataToCommunity()
                    closeAfterSuccess()
                }
            }, {
                // set api state/button to normal
                createUpdatePostViewModel.apiIsLoading = false
                unlockViews()
            }, {
                createUpdatePostViewModel.apiIsLoading = false
                unlockViews()
            })
        }
    }

    private fun fetchPostMediaData(postMedia: Array<FileMetaData>) {
        for(index in postMedia.indices) {
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .fetchPostMediaReq(FetchPostMediaReqDto(createUpdatePostViewModel.postId!!, index))
            ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
                // get file extension
                val extension = postMedia[index].url.split('.').last()

                // copy file and get real path
                val mediaByteArray = response.body()!!.byteStream().readBytes()
                createUpdatePostViewModel.photoPathList[index] =
                    ServerUtil.createCopyAndReturnRealPathServer(requireContext(), mediaByteArray, extension, CREATE_UPDATE_POST_DIRECTORY)

                // save photo thumbnail
                val thumbnail = BitmapFactory.decodeByteArray(mediaByteArray, 0, mediaByteArray.size)
                createUpdatePostViewModel.photoThumbnailList[index] = thumbnail

                // if all is done fetching -> set RecyclerView + set usage + show main ScrollView
                if("" !in createUpdatePostViewModel.photoPathList) {
                    // update RecyclerView and photo usage
                    photoAdapter.setResult(createUpdatePostViewModel.photoThumbnailList)
                    updatePhotoUsage()

                    // set fetched to true
                    createUpdatePostViewModel.fetchedPostPhotoDataForUpdate = true
                    if (createUpdatePostViewModel.fetchedPostPhotoDataForUpdate &&
                        createUpdatePostViewModel.fetchedPostGeneralFileDataForUpdate) {
                        createUpdatePostViewModel.fetchedPostDataForUpdate = true
                    }

                    // set views with post data
                    if (createUpdatePostViewModel.fetchedPostDataForUpdate) {
                        fetchPetDataAndSetRecyclerView()
                    }
                }
            }, {}, {})
        }
    }

    private fun fetchPostGeneralData(postId: Long, postGeneral: Array<FileMetaData>) {
        for(index in postGeneral.indices) {
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .fetchPostFileReq(FetchPostFileReqDto(createUpdatePostViewModel.postId!!, index))
            ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
                // get file extension and name
                val extension = postGeneral[index].url.split('.').last()

                // copy file and get real path
                val generalFileByteArray = response.body()!!.byteStream().readBytes()
                createUpdatePostViewModel.generalFilePathList[index] =
                    ServerUtil.createCopyAndReturnRealPathServer(requireContext(), generalFileByteArray, extension, CREATE_UPDATE_POST_DIRECTORY)

                // save general file name
                val generalFileName = postGeneral[index].url.split("post_${postId}_").last()
                createUpdatePostViewModel.generalFileNameList[index] = generalFileName

                // if all is done fetching -> set RecyclerView + set usage + show main ScrollView
                if("" !in createUpdatePostViewModel.generalFilePathList) {
                    // update RecyclerView and general usage
                    generalFilesAdapter.setResult(createUpdatePostViewModel.generalFileNameList)
                    updateGeneralUsage()

                    // set fetched to true
                    createUpdatePostViewModel.fetchedPostGeneralFileDataForUpdate = true
                    if (createUpdatePostViewModel.fetchedPostPhotoDataForUpdate &&
                        createUpdatePostViewModel.fetchedPostGeneralFileDataForUpdate) {
                        createUpdatePostViewModel.fetchedPostDataForUpdate = true
                    }

                    // set views with post data
                    if (createUpdatePostViewModel.fetchedPostDataForUpdate) {
                        fetchPetDataAndSetRecyclerView()
                    }
                }
            }, {}, {})
        }
    }

    private fun fetchPostData() {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPostReq(FetchPostReqDto(null, null, null, createUpdatePostViewModel.postId))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            // fetch post data (excluding files) and save to ViewModel
            val post = response.body()?.postList!![0]

            createUpdatePostViewModel.selectedPetId = post.pet.id
            createUpdatePostViewModel.isUsingLocation = post.geoTagLat != 0.0
            createUpdatePostViewModel.disclosure = post.disclosure
            if(post.serializedHashTags != "") {
                createUpdatePostViewModel.hashtagList = post.serializedHashTags.split(',').toMutableList()
                hashtagAdapter.setResult(createUpdatePostViewModel.hashtagList)
            }
            createUpdatePostViewModel.postEditText = post.contents

            // fetch post media (photos) data
            if(post.mediaAttachments != null) {
                val postMedia =
                    Gson().fromJson(post.mediaAttachments, Array<FileMetaData>::class.java)

                // initialize lists
                for (i in postMedia.indices) {
                    createUpdatePostViewModel.photoPathList.add("")
                    createUpdatePostViewModel.photoThumbnailList.add(null)
                }

                fetchPostMediaData(postMedia)
            }
            else {
                createUpdatePostViewModel.fetchedPostPhotoDataForUpdate = true
            }

            // TODO: fetch post media (videos) data

            // fetch post general data
            if (post.fileAttachments != null) {
                val postGeneral = Gson().fromJson(post.fileAttachments, Array<FileMetaData>::class.java)

                // initialize lists
                for(i in postGeneral.indices) {
                    createUpdatePostViewModel.generalFilePathList.add("")
                    createUpdatePostViewModel.generalFileNameList.add("")
                }

                fetchPostGeneralData(post.id, postGeneral)
            }
            else {
                createUpdatePostViewModel.fetchedPostGeneralFileDataForUpdate = true
            }

            // if no attachments
            if (post.mediaAttachments == null && post.fileAttachments == null) {
                // set fetched to true
                createUpdatePostViewModel.fetchedPostDataForUpdate = true

                fetchPetDataAndSetRecyclerView()
            }
        }, {
            requireActivity().finish()
        }, {
            requireActivity().finish()
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
        unlockViews()

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

        // restore usages
        updatePhotoUsage()
        updateGeneralUsage()

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

        isViewDestroyed = true

        // delete copied files(if any)
        if(isRemoving || requireActivity().isFinishing) {
            Util.deleteCopiedFiles(requireContext(), CREATE_UPDATE_POST_DIRECTORY)
        }
    }
}