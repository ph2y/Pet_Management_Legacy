package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.PetFeedScheduleDeleteRequestDto
import com.sju18001.petmanagement.restapi.dto.PetFeedScheduleDeleteResponseDto
import com.sju18001.petmanagement.restapi.dto.PetFeedScheduleUpdateRequestDto
import com.sju18001.petmanagement.restapi.dto.PetFeedScheduleUpdateResponseDto
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalTime
import kotlin.coroutines.coroutineContext

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
        holder.petListTextView.text = setPetListString(data.petList)
        holder.memoTextView.text = data.memo
    }

    private fun setPetListString(petList: ArrayList<Long>): String{
        var result = ""
        val size = petList.size

        for(i:Int in 0 until size-1){
            val id = petList[i]
            result += if(petNameForId[id] != null) petNameForId[id] else id.toString()
            result += ", "
        }
        result += if(petNameForId[petList.last()] != null) petNameForId[petList.last()] else petList.last().toString()

        return result
    }

    fun addItem(item: PetFeedScheduleListItem){
        dataSet.add(item)
    }

    fun removeItem(index: Int){
        dataSet.removeAt(index)
    }

    fun setDataSet(_dataSet: ArrayList<PetFeedScheduleListItem>){
        dataSet = _dataSet
    }
}