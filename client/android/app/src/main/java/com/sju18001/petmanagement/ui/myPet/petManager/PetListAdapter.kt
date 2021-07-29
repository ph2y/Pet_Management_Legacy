package com.sju18001.petmanagement.ui.myPet.petManager

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class PetListAdapter : RecyclerView.Adapter<PetListAdapter.HistoryListViewHolder>() {

    private var resultList = emptyList<PetListItem>()

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val petImage: ImageView = itemView.findViewById(R.id.pet_image)
        val petName: TextView = itemView.findViewById(R.id.pet_name)
        val petBirth: TextView = itemView.findViewById(R.id.pet_birth)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.my_pet_list_item, parent, false)
        return HistoryListViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: HistoryListViewHolder, position: Int) {
        val currentItem = resultList[position]

        // create strings to display
        val period = Period.between(currentItem.getPetBirth(), LocalDate.now())

        var petNameInfo = ""
        petNameInfo += currentItem.getPetName() + '(' + currentItem.getPetBreed() + ", "
        petNameInfo += if(currentItem.getPetGender()!!) {
            holder.itemView.context.getString(R.string.pet_gender_female) + "/" + period.years.toString() + "세)"
        } else {
            holder.itemView.context.getString(R.string.pet_gender_male) + "/"  + period.years.toString() + "세)"
        }

        var petBirth = ""
        petBirth += currentItem.getPetBirth()?.format(DateTimeFormatter.ISO_DATE) + '생'

        // set values to views
        currentItem.getPetPhotoUrl()?.let { holder.petImage.setImageResource(it) }
        holder.petName.text = petNameInfo
        holder.petBirth.text = petBirth
    }

    override fun getItemCount() = resultList.size

    fun setResult(result: List<PetListItem>){
        this.resultList = result
        notifyDataSetChanged()
    }
}