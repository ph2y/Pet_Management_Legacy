package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R

interface PetFeedScheduleListAdapterInterface{
    fun startEditPetFeedScheduleFragmentForUpdate(data: PetFeedScheduleListItem)
    fun askForDeleteItem(position: Int, id: Long)
    fun deletePetFeedSchedule(id: Long)
    fun updatePetFeedSchedule(data: PetFeedScheduleListItem)
}

class PetFeedScheduleListAdapter(private var dataSet: ArrayList<PetFeedScheduleListItem>, private val petNameForId: HashMap<Long, String>) : RecyclerView.Adapter<PetFeedScheduleListAdapter.ViewHolder>(){
    lateinit var petFeedScheduleListAdapterInterface: PetFeedScheduleListAdapterInterface

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val noonTextView: TextView = view.findViewById(R.id.noon_text_view)
        val feedTimeTextView: TextView = view.findViewById(R.id.feed_time_text_view)
        val isTurnedOnSwitch: Switch = view.findViewById(R.id.is_turned_on_switch)
        val petListTextView: TextView = view.findViewById(R.id.pet_list_text_view)
        val memoTextView: TextView = view.findViewById(R.id.memo_text_view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pet_feed_schedule_list_item, parent, false)

        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateDataSetToViewHolder(holder, dataSet[position])

        // 아이템 click
        holder.itemView.setOnClickListener {
            petFeedScheduleListAdapterInterface.startEditPetFeedScheduleFragmentForUpdate(dataSet[position])
        }

        // 아이템 Long click
        holder.itemView.setOnLongClickListener { _ ->
            petFeedScheduleListAdapterInterface.askForDeleteItem(position, dataSet[position].id)
            true
        }

        // 스위치
        holder.isTurnedOnSwitch.setOnClickListener {
            petFeedScheduleListAdapterInterface.updatePetFeedSchedule(dataSet[position])

            dataSet[position].isTurnedOn = !dataSet[position].isTurnedOn
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = dataSet.size

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDataSetToViewHolder(holder: ViewHolder, data: PetFeedScheduleListItem){
        holder.noonTextView.text = if(data.feedTime!!.hour <= 12) "오전" else "오후"
        holder.feedTimeTextView.text = data.feedTime!!.hour.toString().padStart(2, '0') + ":" + data.feedTime!!.minute.toString().padStart(2, '0')
        holder.isTurnedOnSwitch.isChecked = data.isTurnedOn!!
        holder.petListTextView.text = getPetNamesFromPetIdList(data.petIdList)
        holder.memoTextView.text = data.memo
    }

    private fun getPetNamesFromPetIdList(petIdList: String?): String{
        if(petIdList.isNullOrEmpty()) return ""

        var petNames = ""

        val petIdListOfString: List<String> = petIdList.split(",")

        for(id in petIdListOfString) {
            id.toLongOrNull()?.let{
                petNames += petNameForId[it] ?: id
                petNames += ", "
            }
        }
        if(petNames.length >= 2){
            petNames = petNames.dropLast(2)
        }

        return petNames
    }

    fun removeItem(index: Int){
        dataSet.removeAt(index)
    }

    fun setDataSet(_dataSet: ArrayList<PetFeedScheduleListItem>){
        dataSet = _dataSet
    }
}