package com.sju18001.petmanagement.ui.community.post.createUpdatePost

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentCreateUpdatePostBinding
import java.io.File

class PhotoListAdapter(private val createUpdatePostViewModel: CreateUpdatePostViewModel,
                       private val context: Context,
                       private val binding: FragmentCreateUpdatePostBinding) :
        RecyclerView.Adapter<PhotoListAdapter.HistoryListViewHolder>() {

    private var resultList = mutableListOf<Bitmap?>()

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.photos_thumbnail)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.uploaded_photos_list_item, parent, false)
        return HistoryListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryListViewHolder, position: Int) {
        // set thumbnail
        holder.thumbnail.setImageBitmap(resultList[position])

        // TODO: delete this (the below code was used for creating video thumbnails)
//        else {
//            Glide.with(context)
//                .load(createUpdatePostViewModel.photoPathList[position])
//                .placeholder(R.drawable.ic_baseline_video_library_36)
//                .into(holder.thumbnail)
//        }

        // for delete button
        holder.deleteButton.setOnClickListener {
            deleteItem(position)

            // update photo upload layout
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
    }

    override fun getItemCount() = resultList.size

    private fun deleteItem(position: Int) {
        // delete file
        File(createUpdatePostViewModel.photoPathList[position]).delete()

        // delete ViewModel values + RecyclerView list
        createUpdatePostViewModel.photoPathList.removeAt(position)
        createUpdatePostViewModel.photoThumbnailList.removeAt(position)

        // for item remove animation
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.resultList.size)
    }

    public fun setResult(result: MutableList<Bitmap?>){
        this.resultList = result
        notifyDataSetChanged()
    }
}