package com.sju18001.petmanagement.ui.community.post.createUpdatePost

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

    class HistoryListViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val hashtag: TextView = view.findViewById(R.id.hashtag_text_view)
        val deleteButton: ImageView = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashtagListAdapter.HistoryListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.created_hashtags_item_list, parent, false)

        val holder = HashtagListAdapter.HistoryListViewHolder(view)
        setListenerOnView(holder)

        return holder
    }

    override fun onBindViewHolder(holder: HashtagListAdapter.HistoryListViewHolder, position: Int) {
        // set TextView
        val hashtag = '#' + resultList[position]
        holder.hashtag.text = hashtag
    }

    override fun getItemCount() = resultList.size

    private fun setListenerOnView(holder: HashtagListAdapter.HistoryListViewHolder) {
        holder.deleteButton.setOnClickListener {
            val position = holder.absoluteAdapterPosition
            deleteItem(position)
        }
    }

    private fun deleteItem(position: Int) {
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