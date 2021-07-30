package com.sju18001.petmanagement.ui.myPet.petManager

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.prefs.Preferences
import kotlin.coroutines.coroutineContext

class PetListAdapter(private val startDragListener: OnStartDragListener, private val context: Context) :
    RecyclerView.Adapter<PetListAdapter.HistoryListViewHolder>(), PetListDragAdapter.Listener {

    private var resultList = emptyList<PetListItem>()

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val petImage: ImageView = itemView.findViewById(R.id.pet_image)
        val petName: TextView = itemView.findViewById(R.id.pet_name)
        val petBirth: TextView = itemView.findViewById(R.id.pet_birth)
        val dragHandle: ImageView = itemView.findViewById(R.id.drag_handle)
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
        if(currentItem.getPetYearOnly()!!) {
            petBirth += currentItem.getPetBirth()?.year.toString() + "년생"
        }
        else {
            petBirth += currentItem.getPetBirth()?.format(DateTimeFormatter.ISO_DATE) + "생"
        }

        // set values to views
        currentItem.getPetPhotoUrl()?.let { holder.petImage.setImageResource(it) }
        holder.petName.text = petNameInfo
        holder.petBirth.text = petBirth

        // handle button for dragging
        holder.dragHandle.setOnLongClickListener(View.OnLongClickListener {
            this.startDragListener.onStartDrag(holder)
            return@OnLongClickListener false
        })
    }

    override fun getItemCount() = resultList.size

    fun setResult(result: List<PetListItem>){
        this.resultList = result
        notifyDataSetChanged()
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(resultList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(resultList, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)

        // save order to device
        val petListIdOrder: MutableList<Long> = mutableListOf()
        resultList.map {
            petListIdOrder.add(it.getPetId()!!)
        }
        PetManagerFragment().savePetListOrder(PetManagerFragment().PET_LIST_ORDER, petListIdOrder, context)
    }
    override fun onRowSelected(itemViewHolder: HistoryListViewHolder) {}
    override fun onRowClear(itemViewHolder: HistoryListViewHolder) {}
}