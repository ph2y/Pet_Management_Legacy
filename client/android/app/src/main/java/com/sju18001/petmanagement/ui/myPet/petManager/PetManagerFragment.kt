package com.sju18001.petmanagement.ui.myPet.petManager

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentPetManagerBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.FetchPetReqDto
import com.sju18001.petmanagement.restapi.dto.FetchPetResDto
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.time.LocalDate

class PetManagerFragment : Fragment(), OnStartDragListener {

    // variable for ViewModel
    val myPetViewModel: MyPetViewModel by activityViewModels()

    // variables for view binding
    private var _binding: FragmentPetManagerBinding? = null
    private val binding get() = _binding!!

    // variables for RecyclerView
    private lateinit var adapter: PetListAdapter
    private var petList: MutableList<PetListItem> = mutableListOf()
    lateinit var touchHelper: ItemTouchHelper
    public var PET_LIST_ORDER: String = "pet_list_id_order"

    // variable for storing API call(for cancel)
    private var fetchPetApiCall: Call<FetchPetResDto>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!
    }

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
        val root: View = binding.root

        // initialize RecyclerView
        adapter = PetListAdapter(this, requireActivity())
        binding.myPetListRecyclerView.adapter = adapter
        binding.myPetListRecyclerView.layoutManager = LinearLayoutManager(activity)

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

        // create DTO
        val fetchPetReqDto = FetchPetReqDto( null )

        fetchPetApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchPetReq(fetchPetReqDto)
        fetchPetApiCall!!.enqueue(object: Callback<FetchPetResDto> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<FetchPetResDto>,
                response: Response<FetchPetResDto>
            ) {
                if(response.isSuccessful) {
                    val petListApi: ArrayList<PetListItem> = ArrayList()
                    response.body()?.petList?.map {
                        val item = PetListItem()
                        item.setValues(
                            it.id,
                            it.name,
                            LocalDate.parse(it.birth),
                            it.yearOnly,
                            it.species,
                            it.breed,
                            it.gender,
                            R.drawable.sample1,
                            it.message
                        )
                        petListApi.add(item)

                        myPetViewModel.addPetNameForId(it.id, it.name)
                    }

                    // if RecyclerView items not yet added
                    if(adapter.itemCount == 0) {
                        // update list order + reorder
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
                }
                else {
                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchPetResDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> return
                if(_binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    // update pet list order
    private fun updatePetListOrder(apiResponse: ArrayList<PetListItem>) {
        // get current saved pet list order
        val petListOrder = getPetListOrder(PET_LIST_ORDER)

        // check for not deleted
        val notDeleted: MutableList<Long> = mutableListOf()
        for(id in petListOrder) {
            val deleted = apiResponse.find { it.getPetId() == id }
            if(deleted == null) {
                notDeleted.add(id)
            }
        }

        // check for not added
        val notAdded: MutableList<Long> = mutableListOf()
        for(pet in apiResponse) {
            val added = petListOrder.find { it == pet.getPetId() }
            if(added == null) {
                notAdded.add(pet.getPetId()!!)
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
        savePetListOrder(PET_LIST_ORDER, petListOrder, requireActivity())
    }

    // reorder pet list
    private fun reorderPetList(apiResponse: ArrayList<PetListItem>) {
        // get saved pet list order
        val petListOrder = getPetListOrder(PET_LIST_ORDER)

        // sort by order
        petList = mutableListOf()
        for(id in petListOrder) {
            val pet = apiResponse.find { it.getPetId() == id }
            petList.add(pet!!)
        }
    }

    // check for difference in lists
    private fun checkListDifference(apiResponse: ArrayList<PetListItem>) {
        // variables for id lists
        val apiResponseIdList: MutableList<Long> = mutableListOf()
        val recyclerViewIdList: MutableList<Long> = mutableListOf()

        // get id lists for API/RecyclerView
        apiResponse.map {
            apiResponseIdList.add(it.getPetId()!!)
        }
        petList.map {
            recyclerViewIdList.add(it.getPetId()!!)
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
                if(petList[i].getPetId() == deletedId[0]) {
                    petList.removeAt(i)
                    adapter.notifyItemRemoved(i)
                    break
                }
            }
            updatePetListOrder(apiResponse)
            return
        }

        // TODO: if item updated

        // if there are multiple/no differences -> update list with all changes + no animation
        updatePetListOrder(apiResponse)
        reorderPetList(apiResponse)
        adapter.setResult(petList)
    }

    // save pet list order
    public fun savePetListOrder(key: String, list: MutableList<Long>, context: Context) {
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor: SharedPreferences.Editor = preferences.edit()
        val json: String = Gson().toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    // get pet list order
    private fun getPetListOrder(key: String?): MutableList<Long> {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val gson = Gson()
        val json: String? = prefs.getString(key, null)
        val type: Type = object : TypeToken<MutableList<Long>>() {}.type

        if(json == null) { return mutableListOf() }
        return gson.fromJson(json, type)
    }

    override fun onStop() {
        super.onStop()

        // save RecyclerView scroll position
        myPetViewModel.lastScrolledIndex = (binding.myPetListRecyclerView.layoutManager as LinearLayoutManager)
            .findFirstVisibleItemPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // stop api call when fragment is destroyed
        fetchPetApiCall?.cancel()
    }
}