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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sju18001.petmanagement.R

class PhotoVideoListAdapter(private val createUpdatePostViewModel: CreateUpdatePostViewModel, private val context: Context) :
        RecyclerView.Adapter<PhotoVideoListAdapter.HistoryListViewHolder>() {

    private var resultList = emptyList<Bitmap?>()

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
            // TODO
        }
    }

    override fun getItemCount() = resultList.size

    public fun setResult(result: List<Bitmap?>){
        this.resultList = result
        notifyDataSetChanged()
    }
}