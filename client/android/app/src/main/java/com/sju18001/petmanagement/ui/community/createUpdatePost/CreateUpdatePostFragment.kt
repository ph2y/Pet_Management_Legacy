package com.sju18001.petmanagement.ui.community.createUpdatePost

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCreateUpdatePostBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Pet
import com.sju18001.petmanagement.restapi.dto.FetchPetPhotoReqDto
import com.sju18001.petmanagement.restapi.dto.FetchPetReqDto
import com.sju18001.petmanagement.restapi.dto.FetchPetResDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Type

class CreateUpdatePostFragment : Fragment() {

    // constant variables
    private val PICK_PHOTO = 0
    private val PICK_VIDEO = 1
    private var PET_LIST_ORDER: String = "pet_list_id_order"
    private var DISCLOSURE_PUBLIC: String = "PUBLIC"
    private var DISCLOSURE_PRIVATE: String = "PRIVATE"
    private var DISCLOSURE_FRIEND: String = "FRIEND"

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
    // TODO

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

        // for view restore(excluding pet and disclosure)
        restoreState()

        // for pet(+ restore)
        setPetSpinnerAndPhoto()

        // for pet spinner
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
            else -> { binding.disclosureSpinner.setSelection(0) }
        }

        // for hashtag input button
        binding.hashtagInputButton.setOnClickListener {
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
            if(!(createUpdatePostViewModel.thumbnailList.size == 0 &&
                        createUpdatePostViewModel.postEditText == "")) {
                // TODO: implement server API
            }
            else {
                // show message(post invalid)
                Toast.makeText(context, context?.getText(R.string.post_invalid_message), Toast.LENGTH_LONG).show()
            }
        }

        // for back button
        binding.backButton.setOnClickListener {
            activity?.finish()
        }

        // hide keyboard when touch outside
        binding.fragmentCreateUpdatePostLayout.setOnClickListener{ Util.hideKeyboard(requireActivity()) }
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
                        .add(ServerUtil.createCopyAndReturnRealPath(requireActivity(), data.data!!))

                    // create bytearray + add to ViewModel
                    val bitmap = BitmapFactory.decodeFile(createUpdatePostViewModel.photoVideoPathList.last())
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val photoByteArray = stream.toByteArray()
                    createUpdatePostViewModel.photoVideoByteArrayList.add(photoByteArray)

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
                        .add(ServerUtil.createCopyAndReturnRealPath(requireActivity(), data.data!!))

                    val video = FileInputStream(File(createUpdatePostViewModel.photoVideoPathList.last()))
                    val stream = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)
                    var n: Int
                    while (-1 != video.read(buffer).also { n = it }) stream.write(buffer, 0, n)
                    val videoByteArray = stream.toByteArray()
                    createUpdatePostViewModel.photoVideoByteArrayList.add(videoByteArray)

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

        fetchPetApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
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

        fetchPetPhotoApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
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
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, spinnerArray)
        binding.petNameSpinner.adapter = spinnerArrayAdapter

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
    // TODO
    private fun getGeolocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = (requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager)
                .getLastKnownLocation(LocationManager.GPS_PROVIDER)
            Log.d("lat", location?.latitude.toString())
            Log.d("long", location?.longitude.toString())
        }
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

    // for view restore
    private fun restoreState() {
        // restore location switch
        binding.locationSwitch.isChecked = createUpdatePostViewModel.isUsingLocation

        // restore photo/video upload layout
        updatePhotoVideoUsage()

        // restore hashtag layout
        updateHashtagUsage()

        // restore post EditText
        binding.postEditText.setText(createUpdatePostViewModel.postEditText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // delete copied files(if any)
        if(isRemoving || requireActivity().isFinishing) {
            for(path in createUpdatePostViewModel.photoVideoPathList) {
                File(path).delete()
            }
        }

        // stop api call when fragment is destroyed
        // TODO
    }
}