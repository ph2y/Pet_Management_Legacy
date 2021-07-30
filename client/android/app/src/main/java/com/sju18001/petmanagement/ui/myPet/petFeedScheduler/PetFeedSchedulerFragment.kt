package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import android.app.AlertDialog
import android.content.DialogInterface
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
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentPetFeedSchedulerBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.*
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

        // 어뎁터 초기화
        initializeAdapter()

        // 추가 버튼
        binding.addPetFeedScheduleFab.setOnClickListener{
            val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
            myPetActivityIntent.putExtra("fragmentType", "create_pet_feed_schedule")
            startActivity(myPetActivityIntent)
        }

        return binding.root

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!
    }

    override fun onStart() {
        super.onStart()

        updateAdapterDataSetByPetFeedScheduleFetch()
    }

    override fun onDestroy() {
        super.onDestroy()

        petFeedScheduleFetchApiCall?.cancel()
    }

    private fun initializeAdapter(){
        adapter = PetFeedScheduleListAdapter(arrayListOf(), myPetViewModel.petNameForId)
        adapter.petFeedScheduleListAdapterInterface = object: PetFeedScheduleListAdapterInterface {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun startEditPetFeedScheduleFragmentForUpdate(data: PetFeedScheduleListItem) {
                val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
                myPetActivityIntent
                    .putExtra("fragmentType", "update_pet_feed_schedule")
                    .putExtra("id", data.id)
                    .putExtra("petIdList", data.petIdList)
                    .putExtra("feedTimeHour", data.feedTime.hour)
                    .putExtra("feedTimeMinute", data.feedTime.minute)
                    .putExtra("memo", data.memo)
                    .putExtra("isTurnedOn", data.isTurnedOn)
                startActivity(myPetActivityIntent)
            }

            override fun askForDeleteItem(position: Int, id: Long) {
                val builder = AlertDialog.Builder(activity)
                builder.setMessage("일정을 삭제하시겠습니까?")
                    .setPositiveButton(
                        R.string.confirm, DialogInterface.OnClickListener { _, _ ->
                            deletePetFeedSchedule(id)
                            adapter.removeItem(position)
                            adapter.notifyDataSetChanged()
                        }
                    )
                    .setNegativeButton(
                        R.string.cancel, DialogInterface.OnClickListener { dialog, _ ->
                            dialog.cancel()
                        }
                    )
                    .create()
                    .show()
            }

            override fun deletePetFeedSchedule(id: Long) {
                val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
                    .petFeedScheduleDeleteRequest(PetFeedScheduleDeleteRequestDto(id))
                call!!.enqueue(object: Callback<PetFeedScheduleDeleteResponseDto> {
                    override fun onResponse(
                        call: Call<PetFeedScheduleDeleteResponseDto>,
                        response: Response<PetFeedScheduleDeleteResponseDto>
                    ) {
                        // Do nothing
                    }

                    override fun onFailure(call: Call<PetFeedScheduleDeleteResponseDto>, t: Throwable) {
                        Log.e("ScheduleAdapter", t.message.toString())
                    }
                })
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun updatePetFeedSchedule(data: PetFeedScheduleListItem){
                val petFeedScheduleUpdateRequestDto = PetFeedScheduleUpdateRequestDto(
                    data.id, data.petIdList, data.feedTime.toString(), data.memo, !data.isTurnedOn
                )

                val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
                    .petFeedScheduleUpdateRequest(petFeedScheduleUpdateRequestDto)
                call!!.enqueue(object: Callback<PetFeedScheduleUpdateResponseDto> {
                    override fun onResponse(
                        call: Call<PetFeedScheduleUpdateResponseDto>,
                        response: Response<PetFeedScheduleUpdateResponseDto>
                    ) {
                        if(response.isSuccessful){
                            // Do nothing
                        }else{
                            Toast.makeText(context, "데이터 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<PetFeedScheduleUpdateResponseDto>, t: Throwable) {
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        binding.petFeedScheduleListRecyclerView.adapter = adapter
        binding.petFeedScheduleListRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

    private fun updateAdapterDataSetByPetFeedScheduleFetch(){
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
                // dataSet에 값 저장
                response.body()?.map{
                    dataSet.add(PetFeedScheduleListItem(
                            it.id,
                            LocalTime.parse(it.feed_time),
                            it.pet_id_list,
                            it.memo,
                            it.is_turned_on
                        )
                    )
                }
                dataSet.sortBy{ it.feedTime }

                adapter.setDataSet(dataSet)
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<List<PetFeedScheduleFetchResponseDto>>, t: Throwable) {
                Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}