package com.sju18001.petmanagement.ui.myPet.petScheduleManager

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R

interface PetNameListAdapterInterface{
    fun setViewModelForCheckBox(position: Int)
    fun setCheckBoxForViewModel(checkBox: CheckBox, position: Int)
}

class PetNameListAdapter(private val dataSet: ArrayList<PetNameListItem>) : RecyclerView.Adapter<PetNameListAdapter.ViewHolder>(){
    lateinit var petNameListAdapterInterface: PetNameListAdapterInterface

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val petNameCheckBox: CheckBox = view.findViewById(R.id.pet_name_check_box)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pet_name_list_item, parent, false)

        val holder = ViewHolder(view)
        setListenerOnView(holder)

        return holder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateDataSetToViewHolder(holder, dataSet[position])

        petNameListAdapterInterface.setCheckBoxForViewModel(holder.petNameCheckBox, position)
    }

    override fun getItemCount(): Int = dataSet.size

    private fun setListenerOnView(holder: ViewHolder) {
        holder.petNameCheckBox.setOnClickListener {
            val position = holder.absoluteAdapterPosition
            petNameListAdapterInterface.setViewModelForCheckBox(position)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDataSetToViewHolder(holder: ViewHolder, data: PetNameListItem){
        holder.petNameCheckBox.text = data.name
    }

    fun addItem(item: PetNameListItem){
        dataSet.add(item)
    }

    fun getItem(position: Int): PetNameListItem{
        return dataSet[position]
    }
}