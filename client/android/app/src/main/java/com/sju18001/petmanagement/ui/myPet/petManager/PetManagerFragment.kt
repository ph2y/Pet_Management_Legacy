package com.sju18001.petmanagement.ui.myPet.petManager

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentPetManagerBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.PetProfileFetchResponseDto
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

    // variable for storing API call(for cancel)
    private var petProfileFetchApiCall: Call<List<PetProfileFetchResponseDto>>? = null

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
        adapter = PetListAdapter(this)
        binding.myPetListRecyclerView.adapter = adapter
        binding.myPetListRecyclerView.layoutManager = LinearLayoutManager(activity)

        touchHelper = ItemTouchHelper(PetListDragAdapter(adapter))
        touchHelper.attachToRecyclerView(binding.myPetListRecyclerView)

        return root
    }

    override fun onStart() {
        super.onStart()

        // add pet fab -> start activity and set fragment to add pet
        binding.addPetFab.setOnClickListener {
            val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
            myPetActivityIntent.putExtra("fragmentType", "add_pet")
            startActivity(myPetActivityIntent)
        }
    }

    override fun onResume() {
        super.onResume()

        // call API using Retrofit
        petProfileFetchApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).petProfileFetchRequest()
        petProfileFetchApiCall!!.enqueue(object: Callback<List<PetProfileFetchResponseDto>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<PetProfileFetchResponseDto>>,
                response: Response<List<PetProfileFetchResponseDto>>
            ) {
                if(response.isSuccessful) {
                    val petListApi: ArrayList<PetListItem> = ArrayList()
                    response.body()?.map {
                        val item = PetListItem()
                        item.setValues(
                            it.id,
                            it.name,
                            LocalDate.parse(it.birth),
                            it.year_only,
                            it.species,
                            it.breed,
                            it.gender,
                            R.drawable.sample1
                        )
                        petListApi.add(item)
                    }

                    // if RecyclerView items not yet added -> set result + restore RecycleView scroll state
                    if(adapter.itemCount == 0) {
                        petList.addAll(petListApi)
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
                    val errorMessage = JSONObject(response.errorBody()!!.string().trim()).getString("message")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<List<PetProfileFetchResponseDto>>, t: Throwable) {
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
            binding.myPetListRecyclerView.scrollToPosition(petList.size - 1)
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
            return
        }

        // if there are multiple differences -> update list + no animation
        petList = apiResponse.toMutableList()
        adapter.setResult(petList)
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
        petProfileFetchApiCall?.cancel()
    }
}