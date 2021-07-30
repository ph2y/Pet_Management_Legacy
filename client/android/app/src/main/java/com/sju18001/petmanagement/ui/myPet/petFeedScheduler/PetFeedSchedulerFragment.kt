package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.databinding.FragmentPetFeedSchedulerBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.PetFeedScheduleFetchResponseDto
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalTime

class PetFeedSchedulerFragment : Fragment() {
    private var _binding: FragmentPetFeedSchedulerBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val myPetViewModel: MyPetViewModel by activityViewModels()

    // 리싸이클러뷰
    private lateinit var adapter: PetFeedScheduleListAdapter

    // API Calls
    private var petFeedScheduleFetchApiCall: Call<List<PetFeedScheduleFetchResponseDto>>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetFeedSchedulerBinding.inflate(inflater, container, false)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

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

    override fun onResume() {
        super.onResume()

        // 리싸이클러뷰
        setAdapterByPetFeedScheduleFetch()
    }

    override fun onDestroy() {
        super.onDestroy()

        petFeedScheduleFetchApiCall?.cancel()
    }


    private fun setAdapterByPetFeedScheduleFetch(){
        val dataSet = arrayListOf<PetFeedScheduleListItem>()
        val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

        petFeedScheduleFetchApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .petFeedScheduleFetchRequest(body)
        petFeedScheduleFetchApiCall!!.enqueue(object: Callback<List<PetFeedScheduleFetchResponseDto>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<PetFeedScheduleFetchResponseDto>>,
                response: Response<List<PetFeedScheduleFetchResponseDto>>
            ) {
                response.body()?.map{
                    dataSet.add(PetFeedScheduleListItem(
                            LocalTime.parse(it.feed_time),
                            arrayListOf(it.pet_id),
                            it.memo,
                            it.is_turned_on
                        )
                    )
                }

                adapter = PetFeedScheduleListAdapter(dataSet, myPetViewModel.petNameForId)
                binding.petFeedScheduleListRecyclerView.adapter = adapter
                binding.petFeedScheduleListRecyclerView.layoutManager = LinearLayoutManager(activity)
            }

            override fun onFailure(call: Call<List<PetFeedScheduleFetchResponseDto>>, t: Throwable) {
                Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}