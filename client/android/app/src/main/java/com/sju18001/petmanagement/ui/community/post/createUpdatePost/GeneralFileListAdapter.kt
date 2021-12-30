package com.sju18001.petmanagement.ui.community.post.createUpdatePost

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentCreateUpdatePostBinding
import java.io.File

class GeneralFileListAdapter(private val createUpdatePostViewModel: CreateUpdatePostViewModel,
                             private val context: Context,
                             private val binding: FragmentCreateUpdatePostBinding) :
    RecyclerView.Adapter<GeneralFileListAdapter.HistoryListViewHolder>() {

    private var resultList = mutableListOf<String>()

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.general_files_name)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.uploaded_general_files_list_item, parent, false)
        return HistoryListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryListViewHolder, position: Int) {
        // set file name
        holder.fileName.text = resultList[position]

        // for delete button
        holder.deleteButton.setOnClickListener {
            deleteItem(position)

            // update general upload layout
            val uploadedCount = createUpdatePostViewModel.generalFileNameList.size
            if (uploadedCount == 0) {
                binding.generalRecyclerView.visibility = View.GONE
            }
            else {
                binding.generalRecyclerView.visibility = View.VISIBLE
            }
            val generalUsageText = "$uploadedCount/10"
            binding.generalUsage.text = generalUsageText
        }
    }

    override fun getItemCount() = resultList.size

    private fun deleteItem(position: Int) {
        // delete file
        File(createUpdatePostViewModel.generalFilePathList[position]).delete()

        // delete ViewModel values + RecyclerView list
        createUpdatePostViewModel.generalFilePathList.removeAt(position)
        createUpdatePostViewModel.generalFileNameList.removeAt(position)

        // for item remove animation
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.resultList.size)
    }

    public fun setResult(result: MutableList<String>){
        this.resultList = result
        notifyDataSetChanged()
    }
}