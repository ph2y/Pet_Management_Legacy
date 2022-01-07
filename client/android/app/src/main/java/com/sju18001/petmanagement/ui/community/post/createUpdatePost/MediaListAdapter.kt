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

class MediaListAdapter(private val createUpdatePostViewModel: CreateUpdatePostViewModel,
                       private val context: Context,
                       private val binding: FragmentCreateUpdatePostBinding) :
        RecyclerView.Adapter<MediaListAdapter.HistoryListViewHolder>() {

    private var resultList = mutableListOf<Bitmap?>()

    class HistoryListViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.photos_thumbnail)
        val deleteButton: ImageView = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryListViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.uploaded_media_list_item, parent, false)

        val holder = HistoryListViewHolder(view)
        setListenerOnView(holder)

        return holder
    }

    override fun onBindViewHolder(holder: HistoryListViewHolder, position: Int) {
        // set thumbnail
        holder.thumbnail.setImageBitmap(resultList[position])

        // TODO: add logic for video thumbnails (the below code was used for creating video thumbnails)
//        else {
//            Glide.with(context)
//                .load(createUpdatePostViewModel.photoPathList[position])
//                .placeholder(R.drawable.ic_baseline_video_library_36)
//                .into(holder.thumbnail)
//        }
    }

    override fun getItemCount() = resultList.size

    private fun setListenerOnView(holder: HistoryListViewHolder) {
        holder.deleteButton.setOnClickListener {
            val position = holder.absoluteAdapterPosition

            deleteItem(position)

            // update photo upload layout
            val uploadedCount = createUpdatePostViewModel.photoPathList.size
            if (uploadedCount == 0) {
                binding.mediaRecyclerView.visibility = View.GONE
            }
            else {
                binding.mediaRecyclerView.visibility = View.VISIBLE
            }
            val photoUsageText = "$uploadedCount/10"
            binding.photoUsage.text = photoUsageText

            // TODO: add logic for video usage
        }
    }

    private fun deleteItem(position: Int) {
        // delete file
        File(createUpdatePostViewModel.photoPathList[position]).delete()

        // delete ViewModel values + RecyclerView list
        createUpdatePostViewModel.photoPathList.removeAt(position)
        createUpdatePostViewModel.mediaThumbnailList.removeAt(position)

        // for item remove animation
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.resultList.size)
    }

    public fun setResult(result: MutableList<Bitmap?>){
        this.resultList = result
        notifyDataSetChanged()
    }
}