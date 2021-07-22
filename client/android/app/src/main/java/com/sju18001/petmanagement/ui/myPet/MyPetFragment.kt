package com.sju18001.petmanagement.ui.myPet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.R

import com.sju18001.petmanagement.databinding.FragmentMyPetBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.PetProfileFetchResponseDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPetFragment : Fragment() {

    private lateinit var myPetViewModel: MyPetViewModel

    // variables for view binding
    private var _binding: FragmentMyPetBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MyPetListAdapter

    // variable for storing API call(for cancel)
    private var petProfileFetchApiCall: Call<List<PetProfileFetchResponseDto>>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!
    }

    override fun onResume() {
        super.onResume()

        // call API using Retrofit
        val petList: ArrayList<MyPetListItem> = ArrayList()
        petProfileFetchApiCall = RetrofitBuilder.serverApi.petProfileFetchRequest(token = "Bearer ${sessionManager.fetchUserToken()!!}")
        petProfileFetchApiCall!!.enqueue(object: Callback<List<PetProfileFetchResponseDto>> {
            override fun onResponse(
                call: Call<List<PetProfileFetchResponseDto>>,
                response: Response<List<PetProfileFetchResponseDto>>
            ) {
                response.body()?.map {
                    val item = MyPetListItem()
                    item.setValues(
                        it.id,
                        it.name,
                        it.birth,
                        it.species,
                        it.breed,
                        it.gender,
                        R.drawable.sample1
                    )
                    petList.add(item)
                }

                adapter.setResult(petList)
            }

            override fun onFailure(call: Call<List<PetProfileFetchResponseDto>>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myPetViewModel =
            ViewModelProvider(this).get(MyPetViewModel::class.java)

        // view binding
        _binding = FragmentMyPetBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // add pet fab -> start activity and set fragment to add pet
        binding.addPetFab.setOnClickListener {
            val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
            myPetActivityIntent.putExtra("fragmentType", "add_pet")
            startActivity(myPetActivityIntent)
        }

        adapter = MyPetListAdapter()
        binding.myPetListRecyclerView.adapter = adapter
        binding.myPetListRecyclerView.layoutManager = LinearLayoutManager(activity)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}