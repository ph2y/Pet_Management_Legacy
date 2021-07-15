package com.sju18001.petmanagement.ui.myPet

import android.content.ContentResolver
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.contentValuesOf
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R

class MyPetListAdapterTemporary : RecyclerView.Adapter<MyPetListAdapterTemporary.HistoryListViewHolder>() {

    private var resultList = emptyList<MyPetListItemTemporary>()

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val petImage: ImageView = itemView.findViewById(R.id.pet_image)
        val petName: TextView = itemView.findViewById(R.id.pet_name)
        val petBirth: TextView = itemView.findViewById(R.id.pet_birth)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.my_pet_list_item, parent, false)
        return HistoryListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryListViewHolder, position: Int) {
        val currentItem = resultList[position]

        // create strings to display
        var petNameInfo = ""
        petNameInfo += currentItem.getPetName() + '(' + currentItem.getPetBreed() + ", "
        petNameInfo += if(currentItem.getPetGender()!!) {
            holder.itemView.context.getString(R.string.pet_gender_female) + "/N세)"
        } else {
            holder.itemView.context.getString(R.string.pet_gender_male) + "/N세)"
        }

        var petBirth = ""
        petBirth += currentItem.getPetBirth() + '생'

        // set values to views
        currentItem.getPetPhotoUrl()?.let { holder.petImage.setImageResource(it) }
        holder.petName.text = petNameInfo
        holder.petBirth.text = petBirth
    }

    override fun getItemCount() = resultList.size

    fun setResult(result: List<MyPetListItemTemporary>){
        this.resultList = result
        notifyDataSetChanged()
    }
}