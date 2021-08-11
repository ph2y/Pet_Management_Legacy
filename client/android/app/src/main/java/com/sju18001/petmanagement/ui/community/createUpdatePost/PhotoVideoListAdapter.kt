package com.sju18001.petmanagement.ui.community.createUpdatePost

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentCreateUpdatePostBinding
import java.io.File

class PhotoVideoListAdapter(private val createUpdatePostViewModel: CreateUpdatePostViewModel,
                            private val context: Context,
                            private val binding: FragmentCreateUpdatePostBinding) :
        RecyclerView.Adapter<PhotoVideoListAdapter.HistoryListViewHolder>() {

    private var resultList = mutableListOf<Bitmap?>()

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.photos_and_videos_thumbnail)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.uploaded_photos_and_videos_list_item, parent, false)
        return HistoryListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryListViewHolder, position: Int) {
        // set thumbnail
        if(resultList[position] != null) { holder.thumbnail.setImageBitmap(resultList[position]) }
        else {
            Glide.with(context)
                .load(createUpdatePostViewModel.photoVideoPathList[position])
                .placeholder(R.drawable.ic_baseline_video_library_36)
                .into(holder.thumbnail)
        }

        // for delete button
        holder.deleteButton.setOnClickListener {
            deleteItem(position)

            // update photo/video upload layout
            val uploadedCount = createUpdatePostViewModel.thumbnailList.size
            if(uploadedCount == 0) { binding.uploadPhotoVideoLabel.visibility = View.VISIBLE }
            val photoVideoUsageText = "$uploadedCount/10"
            binding.photoVideoUsage.text = photoVideoUsageText
        }
    }

    override fun getItemCount() = resultList.size

    private fun deleteItem(position: Int) {
        // delete file
        File(createUpdatePostViewModel.photoVideoPathList[position]).delete()

        // delete ViewModel values + RecyclerView list
        createUpdatePostViewModel.photoVideoByteArrayList.removeAt(position)
        createUpdatePostViewModel.photoVideoPathList.removeAt(position)
        createUpdatePostViewModel.thumbnailList.removeAt(position)

        // for item remove animation
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.resultList.size)
    }

    public fun setResult(result: MutableList<Bitmap?>){
        this.resultList = result
        notifyDataSetChanged()
    }
}