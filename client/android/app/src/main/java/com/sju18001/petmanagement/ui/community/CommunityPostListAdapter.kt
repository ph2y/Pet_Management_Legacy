package com.sju18001.petmanagement.ui.community

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.restapi.dao.CommunityPost
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.ui.myPet.petScheduleManager.PetNameListAdapter

class CommunityPostListAdapter(private var dataSet: ArrayList<CommunityPost>) : RecyclerView.Adapter<CommunityPostListAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val nicknameTextView: TextView = view.findViewById(R.id.nickname)
        val petNameTextView: TextView = view.findViewById(R.id.pet_name)
        val likeCountTextView: TextView = view.findViewById(R.id.like_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.community_post_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateDataSetToViewHolder(holder, dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    private fun updateDataSetToViewHolder(holder: ViewHolder, data: CommunityPost){
        holder.nicknameTextView.text = data.nickname
        holder.petNameTextView.text = data.petName
        holder.likeCountTextView.text = data.like.toString()
    }

    fun addItems(items: List<CommunityPost>){
        for(item in items){
            dataSet.add(item)
        }
    }
}