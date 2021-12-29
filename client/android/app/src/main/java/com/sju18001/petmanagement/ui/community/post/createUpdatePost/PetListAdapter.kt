package com.sju18001.petmanagement.ui.community.post.createUpdatePost

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import de.hdodenhof.circleimageview.CircleImageView

class PetListAdapter(private val createUpdatePostViewModel: CreateUpdatePostViewModel, private val context: Context):
    RecyclerView.Adapter<PetListAdapter.ViewHolder>() {

    private var dataSet = mutableListOf<PetListItem>()

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val parentLayout: CardView = view.findViewById(R.id.create_update_post_pet_item_parent_layout)
        val petPhoto: CircleImageView = view.findViewById(R.id.pet_photo)
        val petName: TextView = view.findViewById(R.id.pet_name)
        val selectedIcon: CircleImageView = view.findViewById(R.id.selected_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.create_update_post_pet_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // set item views
        if (dataSet[position].petPhotoUrl != null) {
            holder.petPhoto.setImageBitmap(dataSet[position].petPhoto)
        } else {
            holder.petPhoto.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
        }

        if (dataSet[position].isSelected) {
            holder.selectedIcon.visibility = View.VISIBLE
        } else {
            holder.selectedIcon.visibility = View.INVISIBLE
        }

        holder.petName.text = dataSet[position].petName

        // for pet select
        holder.parentLayout.setOnClickListener {
            val previousSelectedIndex: Int = createUpdatePostViewModel.selectedPetIndex

            // update selected pet values
            createUpdatePostViewModel.selectedPetId = dataSet[position].petId
            createUpdatePostViewModel.selectedPetIndex = position

            // update previous and current pet's flags
            if (previousSelectedIndex != -1) {
                dataSet[previousSelectedIndex].isSelected = false
            }
            dataSet[position].isSelected = true

            // update view changes
            if (previousSelectedIndex != -1) {
                // passed object to not show the animation
                notifyItemChanged(previousSelectedIndex, dataSet[previousSelectedIndex])
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = dataSet.size

    fun updateDataSet(newDataSet: MutableList<PetListItem>) {
        dataSet = newDataSet
        notifyDataSetChanged()
    }
}