package com.sju18001.petmanagement.ui.myPet.petManager

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentPetManagerBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Pet
import com.sju18001.petmanagement.restapi.dto.FetchPetReqDto
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import java.lang.reflect.Type

class PetManagerFragment : Fragment(), OnStartDragListener {

    // variable for ViewModel
    val myPetViewModel: MyPetViewModel by activityViewModels()

    // variables for view binding
    private var _binding: FragmentPetManagerBinding? = null
    private val binding get() = _binding!!

    // variables for RecyclerView
    private lateinit var adapter: PetListAdapter
    private var petList: MutableList<Pet> = mutableListOf()
    lateinit var touchHelper: ItemTouchHelper

    private var isViewDestroyed = false

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentPetManagerBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        val root: View = binding.root

        // initialize RecyclerView
        adapter = PetListAdapter(this, requireActivity())
        binding.myPetListRecyclerView.adapter = adapter
        binding.myPetListRecyclerView.layoutManager = LinearLayoutManager(activity)

        // set adapter item change observer
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                setEmptyNotificationView(adapter.itemCount)
            }
        })

        touchHelper = ItemTouchHelper(PetListDragAdapter(adapter))
        touchHelper.attachToRecyclerView(binding.myPetListRecyclerView)

        return root
    }

    override fun onStart() {
        super.onStart()

        // create pet fab -> start activity and set fragment to create pet
        binding.createPetFab.setOnClickListener {
            val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
            myPetActivityIntent.putExtra("fragmentType", "create_pet")
            startActivity(myPetActivityIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    override fun onResume() {
        super.onResume()

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPetReq(FetchPetReqDto( null , null))
        ServerUtil.enqueueApiCall(call, isViewDestroyed, requireContext(), { response ->
            val petListApi: ArrayList<Pet> = ArrayList()
            response.body()?.petList?.map {
                petListApi.add(it)
                myPetViewModel.addPetNameForId(it.id, it.name)
            }

            // if RecyclerView items not yet added
            if(adapter.itemCount == 0) {
                updatePetListOrder(petListApi)
                reorderPetList(petListApi)

                // set result + restore last scrolled index
                adapter.setResult(petList)
                binding.myPetListRecyclerView.scrollToPosition(myPetViewModel.lastScrolledIndex)
            }
            // check for difference in lists(current RecyclerView vs API response)
            else {
                checkListDifference(petListApi)
            }

            adapter.notifyDataSetChanged()
        }, {}, {})
    }

    // update pet list order
    private fun updatePetListOrder(apiResponse: ArrayList<Pet>) {
        // get current saved pet list order
        val petListOrder = getPetListOrder(requireContext()
            .getString(R.string.data_name_pet_list_id_order), requireContext())

        // check for not deleted
        val notDeleted: MutableList<Long> = mutableListOf()
        for(id in petListOrder) {
            val deleted = apiResponse.find { it.id == id }
            if(deleted == null) {
                notDeleted.add(id)
            }
        }

        // check for not added
        val notAdded: MutableList<Long> = mutableListOf()
        for(pet in apiResponse) {
            val added = petListOrder.find { it == pet.id }
            if(added == null) {
                notAdded.add(pet.id)
            }
        }

        // update pet list order
            // delete not deleted
        for(id in notDeleted) {
            petListOrder.remove(id)
        }
            // add not added
        for(id in notAdded) {
            petListOrder.add(id)
        }

        // save to device(SharedPreferences)
        savePetListOrder(requireContext().getString(R.string
            .data_name_pet_list_id_order), petListOrder, requireContext())
    }

    // reorder pet list
    private fun reorderPetList(apiResponse: ArrayList<Pet>) {
        // get saved pet list order
        val petListOrder = getPetListOrder(requireContext()
            .getString(R.string.data_name_pet_list_id_order), requireContext())

        // sort by order
        petList = mutableListOf()
        for(id in petListOrder) {
            val pet = apiResponse.find { it.id == id }
            petList.add(pet!!)
        }
    }

    // check for difference in lists
    private fun checkListDifference(apiResponse: ArrayList<Pet>) {
        // variables for id lists
        val apiResponseIdList: MutableList<Long> = mutableListOf()
        val recyclerViewIdList: MutableList<Long> = mutableListOf()

        // get id lists for API/RecyclerView
        apiResponse.map {
            apiResponseIdList.add(it.id)
        }
        petList.map {
            recyclerViewIdList.add(it.id)
        }

        // get added/deleted id
        val addedId = apiResponseIdList.minus(recyclerViewIdList)
        val deletedId = recyclerViewIdList.minus(apiResponseIdList)

        // show add/delete animation(if difference size is 1)
            // if added
        if(addedId.size == 1 && deletedId.isEmpty()) {
            petList.add(petList.size, apiResponse[apiResponse.size - 1])
            adapter.notifyItemInserted(petList.size)
            binding.myPetListRecyclerView.smoothScrollToPosition(petList.size - 1)
            updatePetListOrder(apiResponse)
            return
        }
            // if deleted
        if(deletedId.size == 1 && addedId.isEmpty()) {
            for(i in 0 until petList.size) {
                if(petList[i].id == deletedId[0]) {
                    petList.removeAt(i)
                    adapter.notifyItemRemoved(i)
                    adapter.notifyItemRangeChanged(i, adapter.itemCount)
                    break
                }
            }
            val position = if(petList.size >= 1) petList.size else 0
            binding.myPetListRecyclerView.smoothScrollToPosition(position)
            updatePetListOrder(apiResponse)
            return
        }

        // if there are multiple/no differences -> update list with all changes + no animation
        updatePetListOrder(apiResponse)
        reorderPetList(apiResponse)
        adapter.setResult(petList)
    }

    // save pet list order
    public fun savePetListOrder(dataName: String, list: MutableList<Long>, context: Context) {
        val preferences: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.pref_name_pet_list_id_order), Context.MODE_PRIVATE)
        val json: String = Gson().toJson(list)

        preferences.edit().putString(dataName, json).apply()
    }

    // get pet list order
    public fun getPetListOrder(dataName: String?, context: Context): MutableList<Long> {
        val preferences: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.pref_name_pet_list_id_order), Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = preferences.getString(dataName, null)
        val type: Type = object : TypeToken<MutableList<Long>>() {}.type

        if(json == null) { return mutableListOf() }
        return gson.fromJson(json, type)
    }

    private fun setEmptyNotificationView(itemCount: Int) {
        // set notification view
        val visibility = if(itemCount != 0) View.GONE else View.VISIBLE
        binding.emptyPetListNotification.visibility = visibility
    }
}