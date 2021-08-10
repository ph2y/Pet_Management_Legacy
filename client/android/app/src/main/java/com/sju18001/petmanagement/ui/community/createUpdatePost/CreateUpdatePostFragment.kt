package com.sju18001.petmanagement.ui.community.createUpdatePost

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
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
        adapter = PhotoVideoListAdapter(createUpdatePostViewModel, requireContext())
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
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.select_photo_video_dialog)
            dialog.show()

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
            }
        }
    }

    private fun restoreState() {
        // TODO
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