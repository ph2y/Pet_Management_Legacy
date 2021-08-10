package com.sju18001.petmanagement.ui.community.createUpdatePost

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCreateUpdatePostBinding
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class CreateUpdatePostFragment : Fragment() {

    // constant variables
    private val PICK_PHOTO = 0
    private val PICK_VIDEO = 1

    // variable for ViewModel
    private val createUpdatePostViewModel: CreateUpdatePostViewModel by activityViewModels()

    // variables for view binding
    private var _binding: FragmentCreateUpdatePostBinding? = null
    private val binding get() = _binding!!

    // variables for RecyclerView
    private lateinit var adapter: PhotoVideoListAdapter

    // variables for storing API call(for cancel)
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

        // initialize RecyclerView
        adapter = PhotoVideoListAdapter(createUpdatePostViewModel, requireContext(), binding)
        binding.photosAndVideosRecyclerView.adapter = adapter
        binding.photosAndVideosRecyclerView.layoutManager = LinearLayoutManager(activity)
        (binding.photosAndVideosRecyclerView.layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
        adapter.setResult(createUpdatePostViewModel.thumbnailList)

        return root
    }

    override fun onStart() {
        super.onStart()

        // for view restore
        restoreState()

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

        // for EditText listener
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

        // get + save pet photo value
        if(resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_PHOTO) {
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
                adapter.notifyItemInserted(createUpdatePostViewModel.thumbnailList.size)
                binding.photosAndVideosRecyclerView.smoothScrollToPosition(createUpdatePostViewModel.thumbnailList.size - 1)

                // update photo/video usage
                updatePhotoVideoUsage()
            }
        }
        else if(resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_VIDEO) {
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
                adapter.notifyItemInserted(createUpdatePostViewModel.thumbnailList.size)
                binding.photosAndVideosRecyclerView.smoothScrollToPosition(createUpdatePostViewModel.thumbnailList.size - 1)

                // update photo/video usage
                updatePhotoVideoUsage()
            }
        }
        else if(resultCode == AppCompatActivity.RESULT_OK) {
            // show message(file type exception)
            Toast.makeText(context, context?.getText(R.string.file_type_exception_message), Toast.LENGTH_LONG).show()
        }
    }

    // update photo/video usage
    private fun updatePhotoVideoUsage() {
        val uploadedCount = createUpdatePostViewModel.thumbnailList.size
        if(uploadedCount != 0) { binding.uploadPhotoVideoLabel.visibility = View.GONE }
        val photoVideoUsageText = "$uploadedCount/10"
        binding.photoVideoUsage.text = photoVideoUsageText
    }

    // for view restore
    private fun restoreState() {
        // restore photo/video upload layout
        updatePhotoVideoUsage()

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