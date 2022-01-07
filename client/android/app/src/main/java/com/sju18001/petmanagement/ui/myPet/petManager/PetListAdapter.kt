package com.sju18001.petmanagement.ui.myPet.petManager

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Pet
import com.sju18001.petmanagement.restapi.dto.FetchPetPhotoReqDto
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.Period
import java.util.*

class PetListAdapter(
        private val startDragListener: OnStartDragListener,
        private val context: Context,
        private val onClickCreateButton: ()->Unit,
        private val restoreScroll: ()->Unit
    ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), PetListDragAdapter.Listener {

    private var resultList = emptyList<Pet>()

    private var clickable: Boolean = true

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val petPhoto: ImageView = itemView.findViewById(R.id.pet_photo)
        val representativePetIcon: ImageView = itemView.findViewById(R.id.representative_pet_icon)
        val petName: TextView = itemView.findViewById(R.id.pet_name)
        val petBreed: TextView = itemView.findViewById(R.id.pet_breed)
        val petAge: TextView = itemView.findViewById(R.id.pet_age)
        val petBirthLayout: LinearLayout = itemView.findViewById(R.id.layout_pet_birth)
        val petBirth: TextView = itemView.findViewById(R.id.pet_birth)
        val petGender: TextView = itemView.findViewById(R.id.pet_gender)
        val petMessage: TextView = itemView.findViewById(R.id.pet_message)
    }

    class CreatePetButtonViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val createPetButton: ImageView = view.findViewById(R.id.button_create_pet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            R.layout.pet_list_item -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.pet_list_item, parent, false)

                val holder = HistoryListViewHolder(view)
                setListenerOnView(holder)

                holder
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.create_pet_button, parent, false)

                val holder = CreatePetButtonViewHolder(view)
                setListenerOnView(holder)

                holder
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            R.layout.pet_list_item -> {
                holder as HistoryListViewHolder

                val currentItem = resultList[position]

                // Set representative pet icon
                val isRepresentativePet = currentItem.id == SessionManager.fetchLoggedInAccount(context)?.representativePetId?: 0
                if (isRepresentativePet) {
                    holder.representativePetIcon.setImageResource(R.drawable.crown)
                    holder.representativePetIcon.scaleType = ImageView.ScaleType.FIT_XY
                    holder.representativePetIcon.visibility = View.VISIBLE
                } else{
                    holder.representativePetIcon.visibility = View.INVISIBLE
                }

                // Set pet photo
                if(currentItem.photoUrl != null) {
                    fetchPetPhoto(currentItem.id, holder.petPhoto)
                }
                else {
                    holder.petPhoto.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
                }

                setPetInfoLayout(holder, currentItem)
            }
        }
    }

    override fun getItemCount() = resultList.size + 1

    override fun getItemViewType(position: Int): Int {
        return if(position == resultList.size) {
            R.layout.create_pet_button
        } else {
            R.layout.pet_list_item
        }
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
            petListIdOrder.add(it.id)
        }
        PetManagerFragment().savePetListOrder(context.getString(R.string.data_name_pet_list_id_order),
            petListIdOrder, context)
    }
    override fun onRowSelected(itemViewHolder: HistoryListViewHolder) {
        val anim = ValueAnimator.ofFloat(1f, 0.5f)
        anim.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float

            itemViewHolder.itemView.scaleX = value
            itemViewHolder.itemView.scaleY = value
        }
        anim.duration = 100
        anim.start()

        // LongClick 직후에 곧바로 발생하는 Click을 방지하기 위함
        clickable = false
    }
    override fun onRowClear(itemViewHolder: HistoryListViewHolder) {
        val anim = ValueAnimator.ofFloat(0.5f, 1f)
        anim.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float

            itemViewHolder.itemView.scaleX = value
            itemViewHolder.itemView.scaleY = value
        }
        anim.duration = 100
        anim.start()
        
        clickable = true

        // 드래그가 끝난 뒤, 스크롤을 PagerSnapHelper에 맞춰서 복구
        restoreScroll.invoke()
    }

    private fun setListenerOnView(holder: HistoryListViewHolder) {
        // Long clicking the item to draging
        holder.itemView.setOnLongClickListener(View.OnLongClickListener {
            this.startDragListener.onStartDrag(holder)

            return@OnLongClickListener false
        })

        // click -> open pet profile
        holder.itemView.setOnClickListener {
            if(!clickable) return@setOnClickListener

            val currentItem = resultList[holder.absoluteAdapterPosition]

            // if pet photo not yet fetched
            if (currentItem.photoUrl != null && holder.petPhoto.drawable == null) {
                return@setOnClickListener
            }

            // set pet values to Intent
            val petProfileIntent = Intent(holder.itemView.context, MyPetActivity::class.java)
            if(currentItem.photoUrl != null) {
                val bitmap = (holder.petPhoto.drawable as BitmapDrawable).bitmap
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val photoByteArray = stream.toByteArray()
                Util.saveByteArrayToSharedPreferences(context, context.getString(R.string.pref_name_byte_arrays),
                    context.getString(R.string.data_name_my_pet_selected_pet_photo), photoByteArray)
            }
            else {
                Util.saveByteArrayToSharedPreferences(context, context.getString(R.string.pref_name_byte_arrays),
                    context.getString(R.string.data_name_my_pet_selected_pet_photo), null)
            }
            petProfileIntent.putExtra("petId", currentItem.id)
            petProfileIntent.putExtra("petName", currentItem.name)
            petProfileIntent.putExtra("petBirth",
                if(currentItem.yearOnly!!) currentItem.birth!!.substring(0, 4)
                else currentItem.birth
            )
            petProfileIntent.putExtra("petSpecies", currentItem.species)
            petProfileIntent.putExtra("petBreed", currentItem.breed)
            petProfileIntent.putExtra("petGender", Util.getGenderSymbol(currentItem.gender, context))
            petProfileIntent.putExtra("petAge", Util.getAgeFromBirth(currentItem.birth))
            petProfileIntent.putExtra("petMessage", currentItem.message)
            val isRepresentativePet = currentItem.id == SessionManager.fetchLoggedInAccount(context)?.representativePetId?: 0
            petProfileIntent.putExtra("isRepresentativePet", isRepresentativePet)

            // open activity
            petProfileIntent.putExtra("fragmentType", "pet_profile_pet_manager")
            holder.itemView.context.startActivity(petProfileIntent)
            (context as Activity).overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    private fun setListenerOnView(holder: CreatePetButtonViewHolder) {
        holder.createPetButton.setOnClickListener {
            onClickCreateButton.invoke()
        }
    }

    private fun fetchPetPhoto(id: Long, view: View) {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(context)!!)
            .fetchPetPhotoReq(FetchPetPhotoReqDto(id))
        ServerUtil.enqueueApiCall(call, {false}, context, { response ->
            // set fetched photo to view
            (view as ImageView).setImageBitmap(BitmapFactory.decodeStream(response.body()!!.byteStream()))
        }, {}, {})
    }

    private fun setPetInfoLayout(holder: HistoryListViewHolder, item: Pet){
        holder.petName.text = item.name
        holder.petBreed.text = item.breed
        holder.petAge.text = Util.getAgeFromBirth(item.birth) + "살"

        // Set visibility of pet_birth_layout
        if(item.yearOnly!!){
            holder.petBirthLayout.visibility = View.GONE
        } else {
            holder.petBirth.text = getBirthString(item.birth!!)
        }

        holder.petGender.text = Util.getGenderSymbol(item.gender, context)
        // Set color of pet_gender
        if(item.gender) holder.petGender.setTextColor(context.getColor(R.color.pink))
        else holder.petGender.setTextColor(context.getColor(R.color.peter_river))

        holder.petMessage.text =
            if(item.message.isNullOrEmpty()) context.getString(R.string.filled_heart) else item.message
    }

    private fun getBirthString(birth: String): String {
        val localDate = LocalDate.parse(birth)
        return "${localDate.monthValue}월 ${localDate.dayOfMonth}일"
    }

    public fun setResult(result: List<Pet>){
        this.resultList = result
        notifyDataSetChanged()
    }
}