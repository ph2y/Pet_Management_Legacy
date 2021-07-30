package com.sju18001.petmanagement.ui.myPet.petManager

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentPetManagerBinding

import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.PetProfileFetchResponseDto
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class PetManagerFragment : Fragment() {

    private lateinit var myPetViewModel: MyPetViewModel

    // variables for view binding
    private var _binding: FragmentPetManagerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PetListAdapter

    // variable for storing API call(for cancel)
    private var petProfileFetchApiCall: Call<List<PetProfileFetchResponseDto>>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myPetViewModel =
            ViewModelProvider(this).get(MyPetViewModel::class.java)

        // view binding
        _binding = FragmentPetManagerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // add pet fab -> start activity and set fragment to add pet
        binding.addPetFab.setOnClickListener {
            val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
            myPetActivityIntent.putExtra("fragmentType", "add_pet")
            startActivity(myPetActivityIntent)
        }

        adapter = PetListAdapter()
        binding.myPetListRecyclerView.adapter = adapter
        binding.myPetListRecyclerView.layoutManager = LinearLayoutManager(activity)

        return root
    }

    override fun onResume() {
        super.onResume()

        // call API using Retrofit
        val petList: ArrayList<PetListItem> = ArrayList()
        petProfileFetchApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).petProfileFetchRequest()
        petProfileFetchApiCall!!.enqueue(object: Callback<List<PetProfileFetchResponseDto>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<PetProfileFetchResponseDto>>,
                response: Response<List<PetProfileFetchResponseDto>>
            ) {
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
                    petList.add(item)
                }

                adapter.setResult(petList)
            }

            override fun onFailure(call: Call<List<PetProfileFetchResponseDto>>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}