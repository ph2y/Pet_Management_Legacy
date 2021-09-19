package com.sju18001.petmanagement.ui.myPet.petManager

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentPetProfileBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Post
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.community.CommunityPostListAdapter
import com.sju18001.petmanagement.ui.community.CommunityPostListAdapterInterface
import com.sju18001.petmanagement.ui.community.comment.CommunityCommentActivity
import com.sju18001.petmanagement.ui.community.createUpdatePost.CreateUpdatePostActivity
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import androidx.core.widget.NestedScrollView

class PetProfileFragment : Fragment(){

    // variables for view binding
    private var _binding: FragmentPetProfileBinding? = null
    private val binding get() = _binding!!

    // constant variables
    private var PET_PROFILE_DIRECTORY: String = "pet_profile"

    // 리싸이클러뷰
    private lateinit var adapter: CommunityPostListAdapter
    private var isAdapterInitialized = false

    // for account photo
    private var accountPhoto: Bitmap? = null

    // 글 새로고침
    private var topPostId: Long? = null
    private var pageIndex: Int = 1

    // variable for ViewModel
    private val myPetViewModel: MyPetViewModel by activityViewModels()

    private var isViewDestroyed = false

    // For starting update post activity
    private val startForUpdateResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK){
            result.data?.let{
                val postId = it.getLongExtra("postId", -1)
                val position = it.getIntExtra("position", -1)

                if(postId != (-1).toLong() && position != -1){
                    fetchOnePostAndInvoke(postId) { item ->
                        adapter.setPost(position, item)
                        adapter.notifyItemChanged(position)
                    }
                }
            }
        }
    }

    private fun fetchOnePostAndInvoke(postId: Long, callback: ((Post)->Unit)){
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPostReq(FetchPostReqDto(null, null, null, postId))
        call.enqueue(object: Callback<FetchPostResDto> {
            override fun onResponse(
                call: Call<FetchPostResDto>,
                response: Response<FetchPostResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful){
                    response.body()?.postList?.get(0)?.let{ item ->
                        callback.invoke(item)
                    }
                }
            }

            override fun onFailure(call: Call<FetchPostResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetProfileBinding.inflate(inflater, container, false)
        isViewDestroyed = false
        isAdapterInitialized = false

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

        // if pet message is empty -> hide view
        if(myPetViewModel.petMessageValueProfile == "") {
            binding.petMessage.visibility = View.GONE
        }

        // if fragment type is pet_profile_community -> hide username_and_pets_layout
        if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_community") {
            binding.buttonsLayout.visibility = View.GONE
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

        // get account photo + initialize RecyclerView(only if adapter not yet initialized)
        if(!isAdapterInitialized) {
            getAccountPhotoAndInitializeRecyclerView()
        }
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

    private fun deletePet() {
        // set api state/button to loading
        myPetViewModel.petManagerApiIsLoading = true
        disableButton()

        // create DTO
        val deletePetReqDto = DeletePetReqDto(
            requireActivity().intent.getLongExtra("petId", -1)
        )

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .deletePetReq(deletePetReqDto)
        call.enqueue(object: Callback<DeletePetResDto> {
            override fun onResponse(
                call: Call<DeletePetResDto>,
                response: Response<DeletePetResDto>
            ) {
                if(isViewDestroyed) return

                // set api state/button to normal
                myPetViewModel.petManagerApiIsLoading = false
                enableButton()

                if(response.isSuccessful) {
                    Toast.makeText(context, context?.getText(R.string.delete_pet_successful), Toast.LENGTH_LONG).show()
                    activity?.finish()
                }
                else {
                    Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
                }
            }

            override fun onFailure(call: Call<DeletePetResDto>, t: Throwable) {
                if(isViewDestroyed) return

                // set api state/button to normal
                myPetViewModel.petManagerApiIsLoading = false
                enableButton()

                Util.showToastAndLog(requireContext(), t.message.toString())
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

    private fun getAccountPhotoAndInitializeRecyclerView() {
        binding.postDataLoadingLayout.visibility = View.VISIBLE
        isAdapterInitialized = true

        val accountId = SessionManager.fetchLoggedInAccount(requireContext())!!.id
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchAccountPhotoReq(FetchAccountPhotoReqDto(accountId))
        call.enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful) {
                    // convert photo to byte array + get bitmap
                    val photoByteArray = response.body()!!.byteStream().readBytes()
                    val photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)

                    // save account photo
                    accountPhoto = photoBitmap

                    initializeAdapterAndSetPost()
                }
                else {
                    // get error message
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                    // 사진이 없을 때, 업데이트 없이 리싸이클러뷰를 초기화시킨다.
                    if (errorMessage == "null") {
                        initializeAdapterAndSetPost()
                    }
                    else {
                        // Toast + Log
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        Log.d("error", errorMessage)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // log error message
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun initializeAdapterAndSetPost(){
        initializeAdapter()

        // 초기 Post 추가
        resetPostData()
        updateAdapterDataSet(FetchPostReqDto(null, null,
            requireActivity().intent.getLongExtra("petId", -1), null))
    }

    private fun initializeAdapter(){
        // 빈 배열로 초기화
        adapter = CommunityPostListAdapter(arrayListOf(), arrayListOf(), arrayListOf())

        // 인터페이스 구현
        adapter.communityPostListAdapterInterface = object: CommunityPostListAdapterInterface {
            override fun startCommunityCommentActivity(postId: Long) {
                val communityCommentActivityIntent = Intent(context, CommunityCommentActivity::class.java)
                communityCommentActivityIntent.putExtra("postId", postId)

                startActivity(communityCommentActivityIntent)
                requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }

            override fun createLike(postId: Long, holder: CommunityPostListAdapter.ViewHolder, position: Int){
                val body = CreateLikeReqDto(postId, null)
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!).createLikeReq(body)
                call.enqueue(object: Callback<CreateLikeResDto> {
                    override fun onResponse(
                        call: Call<CreateLikeResDto>,
                        response: Response<CreateLikeResDto>
                    ) {
                        if(isViewDestroyed){
                            return
                        }

                        if(response.isSuccessful){
                            holder.likeCountTextView.text = ((holder.likeCountTextView.text).toString().toLong() + 1).toString()
                        }else{
                            val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        }

                        adapter.setIsPostLiked(position, true)
                        adapter.showDeleteLikeButton(holder)
                    }

                    override fun onFailure(call: Call<CreateLikeResDto>, t: Throwable) {
                        if(isViewDestroyed){
                            return
                        }

                        Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun deleteLike(postId: Long, holder: CommunityPostListAdapter.ViewHolder, position: Int) {
                val body = DeleteLikeReqDto(postId, null)
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!).deleteLikeReq(body)
                call.enqueue(object: Callback<DeleteLikeResDto> {
                    override fun onResponse(
                        call: Call<DeleteLikeResDto>,
                        response: Response<DeleteLikeResDto>
                    ) {
                        if(isViewDestroyed){
                            return
                        }

                        if(response.isSuccessful){
                            holder.likeCountTextView.text = ((holder.likeCountTextView.text).toString().toLong() - 1).toString()
                        }else{
                            val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        }

                        adapter.setIsPostLiked(position, false)
                        adapter.showCreateLikeButton(holder)
                    }

                    override fun onFailure(call: Call<DeleteLikeResDto>, t: Throwable) {
                        if(isViewDestroyed){
                            return
                        }

                        Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onClickPostFunctionButton(post: Post, position: Int) {
                if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_pet_manager") {
                    createPostDialogForAuthor(post, position)
                }

                if(requireActivity().intent.getStringExtra("fragmentType") == "pet_profile_community") {
                    createPostDialogForNonAuthor()
                }
            }

            override fun setAccountPhoto(id: Long, holder: CommunityPostListAdapter.ViewHolder){
                holder.petPhotoImage.setImageBitmap(accountPhoto)
            }

            override fun setAccountDefaultPhoto(holder: CommunityPostListAdapter.ViewHolder) {
                holder.petPhotoImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_account_circle_24))
            }

            override fun setPostMedia(
                holder: CommunityPostListAdapter.PostMediaItemCollectionAdapter.ViewPagerHolder,
                id: Long,
                index: Int,
                url: String
            ) {
                val body = FetchPostMediaReqDto(id, index)
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!).fetchPostMediaReq(body)
                call.enqueue(object: Callback<ResponseBody> {
                    @RequiresApi(Build.VERSION_CODES.R)
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(isViewDestroyed){
                            return
                        }

                        if(response.isSuccessful){
                            // 영상
                            if(Util.isUrlVideo(url)){
                                // Save file
                                val dir = File(requireContext().getExternalFilesDir(null).toString() +
                                        File.separator + PET_PROFILE_DIRECTORY)
                                if(! dir.exists()){
                                    dir.mkdir()
                                }

                                // 해당 파일 검색
                                val filePrefix = "${id}_${index}_"
                                val prevUri = dir.walk().find { it.name.startsWith(filePrefix) }

                                // 파일이 없을 때만 파일 생성
                                val uri: Uri = if(prevUri != null){
                                    prevUri.toUri()
                                }else{
                                    val file = File.createTempFile(
                                        filePrefix,
                                        ".${url.substringAfterLast(".", "")}",
                                        dir
                                    )

                                    val os = FileOutputStream(file)
                                    os.write(response.body()!!.byteStream().readBytes())
                                    os.close()

                                    Uri.fromFile(file)
                                }


                                // View
                                val postMediaVideo = holder.postMediaVideo
                                postMediaVideo.visibility = View.VISIBLE

                                // 영상의 비율을 유지한 채로, 영상의 사이즈를 가로로 꽉 채웁니다.
                                val retriever = MediaMetadataRetriever()
                                retriever.setDataSource(requireContext(), uri)

                                val videoWidth = Integer.parseInt(retriever.extractMetadata(
                                    MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                                val videoHeight = Integer.parseInt(retriever.extractMetadata(
                                    MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))

                                val screenWidth = Util.getScreenWidthInPixel(requireActivity())
                                val ratio: Float = screenWidth.toFloat() / videoWidth.toFloat()

                                postMediaVideo.layoutParams.height = (videoHeight.toFloat() * ratio).toInt()

                                // 반복 재생
                                postMediaVideo.setOnCompletionListener {
                                    postMediaVideo.start()
                                }

                                // 재생
                                postMediaVideo.setVideoURI(uri)
                                postMediaVideo.requestFocus()
                                postMediaVideo.start()
                            }
                            // 이미지
                            else{
                                // Convert photo to byte array + get bitmap
                                val photoByteArray = response.body()!!.byteStream().readBytes()
                                val photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)

                                // Set post image
                                val postMediaImage = holder.postMediaImage
                                postMediaImage.visibility = View.VISIBLE
                                postMediaImage.setImageBitmap(photoBitmap)

                                // 영상의 사이즈를 가로로 꽉 채우되, 비율을 유지합니다.
                                val screenWidth = Util.getScreenWidthInPixel(requireActivity())
                                val ratio: Float = screenWidth.toFloat() / photoBitmap.width.toFloat()
                                postMediaImage.layoutParams.height = (photoBitmap.height.toFloat() * ratio).toInt()
                            }
                        }else{
                            // get error message
                            val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                            // Toast + Log
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        if(isViewDestroyed){
                            return
                        }

                        Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                    }
                })
            }

            override fun getContext(): Context {
                return requireContext()
            }
        }

        binding.recyclerViewPost?.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)
        }

        // 스크롤하여, 최하단에 위치할 시 post 추가 로드
        binding.petProfileMainScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener {
                v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                updateAdapterDataSet(FetchPostReqDto(pageIndex, topPostId,
                    requireActivity().intent.getLongExtra("petId", -1), null))
                pageIndex += 1
            }
        })
    }

    private fun updateAdapterDataSet(body: FetchPostReqDto){
        // Fetch post
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPostReq(body)
        call.enqueue(object: Callback<FetchPostResDto> {
            override fun onResponse(
                call: Call<FetchPostResDto>,
                response: Response<FetchPostResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful){
                    response.body()!!.postList?.let {
                        if(it.isNotEmpty()){
                            // 추가로, 로딩 중에 뷰가 제거되면 오류(Inconsistency detected)가 나는데, 칼럼이 생긴 이후에도 발생 시 fix할 것

                            // Set topPostId
                            if(topPostId == null){
                                topPostId = it.first().id
                            }

                            // 데이터 추가
                            it.map { item ->
                                adapter.addItem(item)
                                setLiked(adapter.itemCount-1, item.id)
                            }
                        }
                    }

                    // hide loading screen
                    binding.postDataLoadingLayout.visibility = View.GONE
                }else{
                    Toast.makeText(context, Util.getMessageFromErrorBody(response.errorBody()!!), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<FetchPostResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setLiked(position: Int, postId: Long){
        val body = FetchLikeReqDto(postId, null)
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!).fetchLikeReq(body)
        call.enqueue(object: Callback<FetchLikeResDto> {
            override fun onResponse(
                call: Call<FetchLikeResDto>,
                response: Response<FetchLikeResDto>
            ) {
                if(response.isSuccessful){
                    adapter.setLikedCount(position, response.body()!!.likedCount!!)

                    val flag = response.body()!!.likedAccountIdList?.contains(SessionManager.fetchLoggedInAccount(requireContext())!!.id)?: false
                    adapter.setIsPostLiked(position, flag)

                    adapter.notifyItemChanged(position)
                }
            }

            override fun onFailure(call: Call<FetchLikeResDto>, t: Throwable) {
                // Do nothing
            }
        })
    }

    private fun resetPostData(){
        pageIndex = 1
        adapter.resetItem()

        // 데이터셋 변경 알림
        binding.recyclerViewPost.post{
            adapter.notifyDataSetChanged()
        }
    }

    private fun createPostDialogForAuthor(post: Post, position: Int){
        val builder = AlertDialog.Builder(requireActivity())
        builder.setItems(arrayOf("수정", "삭제"), DialogInterface.OnClickListener{ _, which ->
            when(which){
                0 -> {
                    // 수정
                    val createUpdatePostActivityIntent = Intent(context, CreateUpdatePostActivity::class.java)
                    createUpdatePostActivityIntent.putExtra("fragmentType", "update_post")
                    createUpdatePostActivityIntent.putExtra("postId", post.id)
                    createUpdatePostActivityIntent.putExtra("position", position)
                    createUpdatePostActivityIntent.putExtra("originalMediaCount", Util.getArrayFromMediaAttachments(post.mediaAttachments).size)

                    startForUpdateResult.launch(createUpdatePostActivityIntent)
                    requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
                }
                1 -> {
                    // 삭제
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setMessage(getString(R.string.delete_post_dialog))
                        .setPositiveButton(R.string.confirm,
                            DialogInterface.OnClickListener { _, _ -> deletePost(post.id, position) }
                        )
                        .setNegativeButton(R.string.cancel,
                            DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }
                        )
                        .create().show()
                }
            }
        })
            .create().show()
    }

    private fun deletePost(id: Long, position: Int){
        val body = DeletePostReqDto(id)
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!).deletePostReq(body)
        call.enqueue(object: Callback<DeletePostResDto> {
            override fun onResponse(
                call: Call<DeletePostResDto>,
                response: Response<DeletePostResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful){
                    // 데이터셋에서 삭제
                    adapter.removeItem(position)
                    adapter.notifyItemRemoved(position)
                    adapter.notifyItemRangeChanged(position, adapter.itemCount)

                    Toast.makeText(context, getString(R.string.delete_post_successful), Toast.LENGTH_LONG).show()
                }else{
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DeletePostResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun createPostDialogForNonAuthor(){
        val builder = AlertDialog.Builder(requireActivity())
        builder.setItems(arrayOf("신고"), DialogInterface.OnClickListener{ _, which ->
            when(which){
                0 -> {
                    // TODO: 기능 추가
                }
            }
        })
            .create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
        isAdapterInitialized = false
        myPetViewModel.petManagerApiIsLoading = false

        // Delete saved files
        Util.deleteCopiedFiles(requireContext(), PET_PROFILE_DIRECTORY)
    }
}