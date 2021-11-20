package com.sju18001.petmanagement.ui.community.post

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.FetchPostFileReqDto

class GeneralFilesAdapter(private val activity: Activity, private val generalFilesViewModel: GeneralFilesViewModel):
    RecyclerView.Adapter<GeneralFilesAdapter.HistoryListViewHolder>() {

    private var GENERAL_FILES_ADAPTER_DIRECTORY: String = "general_files_adapter"

    private var resultList = mutableListOf<GeneralFilesListItem>()

    private var isViewDestroyed = false

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val generalFileName: TextView = itemView.findViewById(R.id.general_file_name)
        val downloadButton: ImageButton = itemView.findViewById(R.id.download_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralFilesAdapter.HistoryListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_general_file_item, parent, false)
        return HistoryListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryListViewHolder, position: Int) {
        holder.generalFileName.text = resultList[position].name
        // TODO: check if downloaded and set image to downloaded icon + clickable

        holder.downloadButton.setOnClickListener {
            // fetch file + write
            fetchGeneralFile(resultList[position].postId, resultList[position].fileId, resultList[position].name)
            // TODO: implement file download logic
        }
    }

    private fun fetchGeneralFile(postId: Long, fileId: Int, fileName: String) {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(activity)!!)
            .fetchPostFileReq(FetchPostFileReqDto(postId, fileId))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, activity, { response ->
            // download file + save downloaded path -> write file
            val extension = fileName.split('.').last()
            generalFilesViewModel.downloadedFilePath = ServerUtil
                .createCopyAndReturnRealPathServer(activity, response.body()!!.byteStream().readBytes(), extension, GENERAL_FILES_ADAPTER_DIRECTORY)

            // get Uri from user + write file
            ServerUtil.getUriFromUser(activity, fileName)
        }, {}, {})
    }

    override fun getItemCount() = resultList.size

    public fun setResult(result: MutableList<GeneralFilesListItem>){
        this.resultList = result
        notifyDataSetChanged()
    }

    public fun onDestroy() {
        isViewDestroyed = true
    }
}