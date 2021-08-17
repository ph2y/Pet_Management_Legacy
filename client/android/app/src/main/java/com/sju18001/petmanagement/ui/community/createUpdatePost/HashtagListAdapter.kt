package com.sju18001.petmanagement.ui.community.createUpdatePost

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentCreateUpdatePostBinding

class HashtagListAdapter(private val createUpdatePostViewModel: CreateUpdatePostViewModel,
                         private val binding: FragmentCreateUpdatePostBinding) :
    RecyclerView.Adapter<HashtagListAdapter.HistoryListViewHolder>() {

    private var resultList = mutableListOf<String>()

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val hashtag: TextView = itemView.findViewById(R.id.hashtag_text_view)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashtagListAdapter.HistoryListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.created_hashtags_item_list, parent, false)
        return HashtagListAdapter.HistoryListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HashtagListAdapter.HistoryListViewHolder, position: Int) {
        // set TextView
        val hashtag = '#' + resultList[position]
        holder.hashtag.text = hashtag

        // for delete button
        holder.deleteButton.setOnClickListener {
            deleteItem(position)

            // update hashtag layout
            val hashtagCount = createUpdatePostViewModel.hashtagList.size
            if(hashtagCount == 0) { binding.hashtagRecyclerView.visibility = View.GONE }
            val hashtagUsageText = "$hashtagCount/5"
            binding.hashtagUsage.text = hashtagUsageText
        }
    }

    override fun getItemCount() = resultList.size

    private fun deleteItem(position: Int) {
        // delete ViewModel RecyclerView list values
        createUpdatePostViewModel.hashtagList.removeAt(position)

        // for item remove animation
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.resultList.size)
    }

    public fun setResult(result: MutableList<String>){
        this.resultList = result
        notifyDataSetChanged()
    }
}