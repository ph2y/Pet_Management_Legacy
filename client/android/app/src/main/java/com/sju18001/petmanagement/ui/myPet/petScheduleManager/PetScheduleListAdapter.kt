package com.sju18001.petmanagement.ui.myPet.petScheduleManager

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.restapi.dao.PetSchedule
import java.time.LocalTime

interface PetScheduleListAdapterInterface{
    fun startCreateUpdatePetScheduleFragmentForUpdate(data: PetSchedule)
    fun askForDeleteItem(position: Int, item: PetSchedule)
    fun deletePetSchedule(id: Long)
    fun updatePetSchedule(data: PetSchedule)
    fun getContext(): Context
}

class PetScheduleListAdapter(private var dataSet: ArrayList<PetSchedule>, private val petNameForId: HashMap<Long, String>) : RecyclerView.Adapter<PetScheduleListAdapter.ViewHolder>(){
    lateinit var petScheduleListAdapterInterface: PetScheduleListAdapterInterface

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val noonTextView: TextView = view.findViewById(R.id.noon_text_view)
        val timeTextView: TextView = view.findViewById(R.id.time_text_view)
        val enabledSwitch: Switch = view.findViewById(R.id.enabled_switch)
        val petListTextView: TextView = view.findViewById(R.id.pet_list_text_view)
        val memoTextView: TextView = view.findViewById(R.id.memo_text_view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pet_schedule_list_item, parent, false)

        val holder = ViewHolder(view)
        setListenerOnView(holder)

        return holder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateDataSetToViewHolder(holder, dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    private fun setListenerOnView(holder: ViewHolder) {
        // 아이템 Click
        holder.itemView.setOnClickListener {
            val position = holder.absoluteAdapterPosition
            petScheduleListAdapterInterface.startCreateUpdatePetScheduleFragmentForUpdate(dataSet[position])
        }

        // 아이템 Long click
        holder.itemView.setOnLongClickListener { _ ->
            val position = holder.absoluteAdapterPosition
            petScheduleListAdapterInterface.askForDeleteItem(position, dataSet[position])
            true
        }

        // 스위치
        holder.enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            val position = holder.absoluteAdapterPosition
            dataSet[position].enabled = isChecked
            petScheduleListAdapterInterface.updatePetSchedule(dataSet[position])

            if(isChecked){
                // Notification ON
                PetScheduleNotification.enqueueNotificationWorkManager(petScheduleListAdapterInterface.getContext(), dataSet[position].time, dataSet[position].memo)
            }else{
                // Notification OFF
                PetScheduleNotification.cancelNotificationWorkManager(petScheduleListAdapterInterface.getContext(), dataSet[position].time)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDataSetToViewHolder(holder: ViewHolder, data: PetSchedule){
        val localTime = LocalTime.parse(data.time)
        holder.noonTextView.text = if(localTime.hour <= 12) "오전" else "오후"
        holder.timeTextView.text = localTime.hour.toString().padStart(2, '0') + ":" + localTime.minute.toString().padStart(2, '0')
        holder.enabledSwitch.isChecked = data.enabled!!
        holder.petListTextView.text = getPetNamesFromPetIdList(data.petIdList)
        holder.memoTextView.text = data.memo
    }

    private fun getPetNamesFromPetIdList(petIdList: String?): String{
        if(petIdList.isNullOrEmpty()) return ""

        var petNames = ""
        val petIdListOfString: List<String> = petIdList.replace(" ", "").split(",")

        // "이름, 이름, " 식으로 String 추가함
        for(id in petIdListOfString) {
            id.toLongOrNull()?.let{
                petNames += petNameForId[it] ?: id
                petNames += ", "
            }
        }
        
        // ", " 제거
        if(petNames.length >= 2){
            petNames = petNames.dropLast(2)
        }

        return petNames
    }

    fun removeItem(index: Int){
        dataSet.removeAt(index)
    }

    fun setDataSet(_dataSet: ArrayList<PetSchedule>){
        dataSet = _dataSet
    }
}