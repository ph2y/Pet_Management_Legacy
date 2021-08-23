package com.sju18001.petmanagement.ui.community

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.restapi.dao.Post

class PostTagListAdapter(private var dataSet: ArrayList<String>) : RecyclerView.Adapter<PostTagListAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val parentView: View = view
        val textTag: TextView = view.findViewById(R.id.text_tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tag_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Set text
        holder.textTag.text = dataSet[position]

        // Click tag
        holder.parentView.setOnClickListener {
            // TODO: 태그 기능
        }
    }

    override fun getItemCount(): Int = dataSet.size
}