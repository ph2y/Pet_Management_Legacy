package com.sju18001.petmanagement.ui.myPet.petManager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.FetchPetPhotoReqDto
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*

class PetListAdapter(private val startDragListener: OnStartDragListener, private val context: Context) :
    RecyclerView.Adapter<PetListAdapter.HistoryListViewHolder>(), PetListDragAdapter.Listener {

    private var resultList = emptyList<PetListItem>()

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val petPhoto: ImageView = itemView.findViewById(R.id.pet_photo)
        val representativePetIcon: ImageView = itemView.findViewById(R.id.representative_pet_icon)
        val petName: TextView = itemView.findViewById(R.id.pet_name)
        val petBirth: TextView = itemView.findViewById(R.id.pet_birth)
        val dragHandle: ImageView = itemView.findViewById(R.id.drag_handle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.pet_list_item, parent, false)

        return HistoryListViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: HistoryListViewHolder, position: Int) {
        val currentItem = resultList[position]

        // create strings to display
        val period = Period.between(currentItem.getPetBirth(), LocalDate.now())

        var petNameInfo = currentItem.getPetName() + " ["
        petNameInfo += if(currentItem.getPetGender()!!) {
            holder.itemView.context.getString(R.string.pet_gender_female_symbol) + ' ' + period.years.toString() + "세]"
        } else {
            holder.itemView.context.getString(R.string.pet_gender_male_symbol) + ' ' + period.years.toString() + "세]"
        }

        var petBirth = ""
        petBirth += if(currentItem.getPetYearOnly()!!) {
            currentItem.getPetBirth()?.year.toString() + "년생"
        } else {
            currentItem.getPetBirth()?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) + "생"
        }

        // set values to views
        if(currentItem.getPetPhotoUrl() != null) {
            fetchPetPhoto(currentItem.getPetId()!!, holder.petPhoto)
        }
        else {
            holder.petPhoto.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
        }
        holder.representativePetIcon.visibility = if (currentItem.getIsRepresentativePet()!!) View.VISIBLE else View.INVISIBLE
        holder.petName.text = petNameInfo
        holder.petBirth.text = petBirth

        // handle button for dragging
        holder.dragHandle.setOnLongClickListener(View.OnLongClickListener {
            this.startDragListener.onStartDrag(holder)
            return@OnLongClickListener false
        })

        // click -> open pet profile
        holder.itemView.setOnClickListener {
            // set pet values to Intent
            val petProfileIntent = Intent(holder.itemView.context, MyPetActivity::class.java)
            if(currentItem.getPetPhotoUrl() != null) {
                val bitmap = (holder.petPhoto.drawable as BitmapDrawable).bitmap
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val photoByteArray = stream.toByteArray()
                petProfileIntent.putExtra("photoByteArray", photoByteArray)
            }
            petProfileIntent.putExtra("petId", currentItem.getPetId())
            petProfileIntent.putExtra("petName", currentItem.getPetName())
            petProfileIntent.putExtra("petBirth", petBirth)
            petProfileIntent.putExtra("petSpecies", currentItem.getPetSpecies())
            petProfileIntent.putExtra("petBreed", currentItem.getPetBreed())
            val petGender = if(currentItem.getPetGender()!!) {
                holder.itemView.context.getString(R.string.pet_gender_female_symbol)
            }
            else {
                holder.itemView.context.getString(R.string.pet_gender_male_symbol)
            }
            val petAge = Period.between(currentItem.getPetBirth(), LocalDate.now()).years.toString()
            petProfileIntent.putExtra("petGender", petGender)
            petProfileIntent.putExtra("petAge", petAge)
            petProfileIntent.putExtra("petMessage", currentItem.getPetMessage())
            petProfileIntent.putExtra("isRepresentativePet", currentItem.getIsRepresentativePet())

            // open activity
            petProfileIntent.putExtra("fragmentType", "pet_profile_pet_manager")
            holder.itemView.context.startActivity(petProfileIntent)
            (context as Activity).overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    override fun getItemCount() = resultList.size

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

    // fetch pet photo
    private fun fetchPetPhoto(id: Long, view: View) {
        // create DTO
        val fetchPetPhotoReqDto = FetchPetPhotoReqDto(id)

        RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(context)!!)
            .fetchPetPhotoReq(fetchPetPhotoReqDto).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.isSuccessful) {
                    // set fetched photo to view
                    (view as ImageView).setImageBitmap(BitmapFactory.decodeStream(response.body()!!.byteStream()))
                }
                else {
                    Util.showToastAndLogForFailedResponse(context, response.errorBody())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Util.showToastAndLog(context, t.message.toString())
            }
        })
    }

    public fun setResult(result: List<PetListItem>){
        this.resultList = result
        notifyDataSetChanged()
    }
}