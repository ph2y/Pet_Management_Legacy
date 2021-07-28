package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.databinding.FragmentPetFeedSchedulerBinding
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import java.time.LocalTime
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.PetFeedScheduleFetchResponseDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PetFeedSchedulerFragment : Fragment() {
    private var _binding: FragmentPetFeedSchedulerBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val myPetViewModel: MyPetViewModel by activityViewModels()

    // 리싸이클러뷰
    private lateinit var adapter: PetFeedScheduleListAdapter

    // variable for storing API call(for cancel)
    private lateinit var petFeedScheduleFetchApiCall: Call<List<PetFeedScheduleFetchResponseDto>>

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetFeedSchedulerBinding.inflate(inflater, container, false)

        // 리싸이클러뷰
        // TODO: 스케줄 로드 & 시간에 따른 결합
        val dataSet = arrayListOf<PetFeedScheduleListItem>(PetFeedScheduleListItem(LocalTime.of(9, 30), arrayListOf(1, 2, 3), "노브랜드 사료 주기", false))

        adapter = PetFeedScheduleListAdapter(dataSet, myPetViewModel.petNameForId)
        binding.petFeedScheduleListRecyclerView.adapter = adapter
        binding.petFeedScheduleListRecyclerView.layoutManager = LinearLayoutManager(activity)

        // 추가 버튼
        binding.addPetFeedScheduleFab.setOnClickListener{
            val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
            myPetActivityIntent.putExtra("fragmentType", "edit_pet_feed_schedule")
            startActivity(myPetActivityIntent)
        }

        return binding.root

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        // call API using Retrofit
        petFeedScheduleFetchApiCall = RetrofitBuilder.getServerApi().petFeedScheduleFetchRequest(token = "Bearer ${sessionManager.fetchUserToken()!!}")
        petFeedScheduleFetchApiCall!!.enqueue(object: Callback<List<PetFeedScheduleFetchResponseDto>> {
            override fun onResponse(
                call: Call<List<PetFeedScheduleFetchResponseDto>>,
                response: Response<List<PetFeedScheduleFetchResponseDto>>
            ) {
                TODO("Not yet implemented")
            }

            override fun onFailure(
                call: Call<List<PetFeedScheduleFetchResponseDto>>,
                t: Throwable
            ) {
                TODO("Not yet implemented")
            }

        })
    }
}