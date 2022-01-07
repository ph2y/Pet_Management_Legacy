package com.sju18001.petmanagement.ui.community.post.createUpdatePost

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import de.hdodenhof.circleimageview.CircleImageView

class PetListAdapter(private val createUpdatePostViewModel: CreateUpdatePostViewModel, private val context: Context):
    RecyclerView.Adapter<PetListAdapter.ViewHolder>() {

    private var dataSet = mutableListOf<PetListItem>()

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val parentLayout: LinearLayout = view.findViewById(R.id.create_update_post_pet_item_parent_layout)
        val petPhoto: CircleImageView = view.findViewById(R.id.pet_photo)
        val petName: TextView = view.findViewById(R.id.pet_name)
        val representativePetIcon: ImageView = view.findViewById(R.id.representative_pet_icon)
        val selectedIcon: CircleImageView = view.findViewById(R.id.selected_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.create_update_post_pet_item, parent, false)

        val holder = ViewHolder(view)
        setListenerOnView(holder)

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // set item views
        if (dataSet[position].petPhotoUrl != null) {
            holder.petPhoto.setImageBitmap(dataSet[position].petPhoto)
        } else {
            holder.petPhoto.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
        }

        if (dataSet[position].isRepresentativePet) {
            holder.representativePetIcon.visibility = View.VISIBLE
        } else {
            holder.representativePetIcon.visibility = View.INVISIBLE
        }

        if (dataSet[position].isSelected) {
            holder.parentLayout.alpha = 1f
            holder.selectedIcon.visibility = View.VISIBLE
        } else {
            holder.parentLayout.alpha = .5f
            holder.selectedIcon.visibility = View.INVISIBLE
        }

        holder.petName.text = dataSet[position].petName
    }

    override fun getItemCount(): Int = dataSet.size

    private fun setListenerOnView(holder: ViewHolder) {
        holder.parentLayout.setOnClickListener {
            val position = holder.absoluteAdapterPosition

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

    fun updateDataSet(newDataSet: MutableList<PetListItem>) {
        dataSet = newDataSet
        notifyDataSetChanged()
    }
}