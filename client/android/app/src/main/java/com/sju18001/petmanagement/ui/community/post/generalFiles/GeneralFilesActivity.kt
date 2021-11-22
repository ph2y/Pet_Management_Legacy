package com.sju18001.petmanagement.ui.community.post.generalFiles

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.ActivityGeneralFilesBinding
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.global.FileMetaData

class GeneralFilesActivity : AppCompatActivity() {

    // const
    private var GENERAL_FILES_ACTIVITY_DIRECTORY: String = "general_files_activity"

    // variable for view binding
    private lateinit var binding: ActivityGeneralFilesBinding

    // variable for ViewModel
    private val generalFilesViewModel: GeneralFilesViewModel by lazy {
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(GeneralFilesViewModel::class.java)
    }

    // variables for RecyclerView
    private lateinit var generalFilesAdapter: GeneralFilesAdapter
    private var generalFilesList: MutableList<GeneralFilesListItem> = mutableListOf()

    private var isViewDestroyed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // no title bar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        // view binding
        binding = ActivityGeneralFilesBinding.inflate(layoutInflater)
        isViewDestroyed = false

        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        // initialize RecyclerView
        generalFilesAdapter = GeneralFilesAdapter(this, generalFilesViewModel, GENERAL_FILES_ACTIVITY_DIRECTORY)
        binding.generalFilesRecyclerView.adapter = generalFilesAdapter
        binding.generalFilesRecyclerView.layoutManager = LinearLayoutManager(this)

        // set RecyclerView values
        generalFilesList = mutableListOf()
        val postId = this.intent.getLongExtra("postId", -1)
        val postGeneralFiles = Gson().fromJson(this.intent.getStringExtra("fileAttachments"), Array<FileMetaData>::class.java)
        for (i in postGeneralFiles.indices) {
            generalFilesList.add(GeneralFilesListItem(postId, postGeneralFiles[i].name.split("post_${postId}_").last(), i))
        }
        generalFilesAdapter.setResult(generalFilesList)

        // for close button
        binding.closeButton.setOnClickListener { finish() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ServerUtil.WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                // save Uri + write
                generalFilesViewModel.userSelectedUri = uri
                ServerUtil.writeFileToUri(this,
                    generalFilesViewModel.downloadedFilePath!!, generalFilesViewModel.userSelectedUri!!)

                // reset download button
                generalFilesAdapter.setResult(generalFilesList)

                Toast.makeText(this, this.getText(R.string.download_complete_message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        generalFilesAdapter.onDestroy()
        isViewDestroyed = true

        if(isFinishing) {
            Util.deleteCopiedFiles(this, GENERAL_FILES_ACTIVITY_DIRECTORY)
        }
    }
}