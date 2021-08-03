package com.sju18001.petmanagement.ui.myPet.petScheduleManager

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentPetScheduleManagerBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.PetSchedule
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalTime

class PetScheduleManagerFragment : Fragment() {
    private var _binding: FragmentPetScheduleManagerBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val myPetViewModel: MyPetViewModel by activityViewModels()

    // 리싸이클러뷰
    private lateinit var adapter: PetScheduleListAdapter

    // API Calls
    private var fetchPetScheduleApiCall: Call<FetchPetScheduleResDto>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetScheduleManagerBinding.inflate(inflater, container, false)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        // 어뎁터 초기화
        initializeAdapter()

        // 추가 버튼
        binding.addPetScheduleFab.setOnClickListener{
            val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
            myPetActivityIntent.putExtra("fragmentType", "create_pet_schedule")
            startActivity(myPetActivityIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        return binding.root

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!
    }

    override fun onStart() {
        super.onStart()

        updateAdapterDataSetByFetchPetSchedule()
    }

    override fun onDestroy() {
        super.onDestroy()

        fetchPetScheduleApiCall?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeAdapter(){
        adapter = PetScheduleListAdapter(arrayListOf(), myPetViewModel.petNameForId)
        adapter.petScheduleListAdapterInterface = object: PetScheduleListAdapterInterface {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun startPetScheduleEditFragmentForUpdate(data: PetSchedule) {
                val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
                myPetActivityIntent
                    .putExtra("fragmentType", "update_pet_schedule")
                    .putExtra("id", data.id)
                    .putExtra("petIdList", data.petIdList)
                    .putExtra("time", data.time)
                    .putExtra("memo", data.memo)
                    .putExtra("enable", data.enable)
                startActivity(myPetActivityIntent)
                requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }

            override fun askForDeleteItem(position: Int, id: Long) {
                val builder = AlertDialog.Builder(activity)
                builder.setMessage("일정을 삭제하시겠습니까?")
                    .setPositiveButton(
                        R.string.confirm, DialogInterface.OnClickListener { _, _ ->
                            deletePetSchedule(id)
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

            override fun deletePetSchedule(id: Long) {
                val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
                    .deletePetScheduleReq(DeletePetScheduleReqDto(id))
                call!!.enqueue(object: Callback<DeletePetScheduleResDto> {
                    override fun onResponse(
                        call: Call<DeletePetScheduleResDto>,
                        response: Response<DeletePetScheduleResDto>
                    ) {
                        // Do nothing
                    }

                    override fun onFailure(call: Call<DeletePetScheduleResDto>, t: Throwable) {
                        Log.e("ScheduleAdapter", t.message.toString())
                    }
                })
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun updatePetSchedule(data: PetSchedule){
                val updatePetScheduleReqDto = UpdatePetScheduleReqDto(
                    data.id, data.petIdList, data.time, data.memo, data.enable
                )

                val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
                    .updatePetScheduleReq(updatePetScheduleReqDto)
                call!!.enqueue(object: Callback<UpdatePetScheduleResDto> {
                    override fun onResponse(
                        call: Call<UpdatePetScheduleResDto>,
                        response: Response<UpdatePetScheduleResDto>
                    ) {
                        if(response.isSuccessful){
                            // Do nothing
                        }else{
                            Toast.makeText(context, "데이터 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<UpdatePetScheduleResDto>, t: Throwable) {
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        binding.petScheduleListRecyclerView.adapter = adapter
        binding.petScheduleListRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

    private fun updateAdapterDataSetByFetchPetSchedule(){
        val dataSet = arrayListOf<PetSchedule>()
        val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

        fetchPetScheduleApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchPetScheduleReq(body)
        fetchPetScheduleApiCall!!.enqueue(object: Callback<FetchPetScheduleResDto> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<FetchPetScheduleResDto>,
                response: Response<FetchPetScheduleResDto>
            ) {
                // dataSet에 값 저장
                response.body()?.petScheduleList?.map{
                    dataSet.add(it)
                }
                dataSet.sortBy{ it.time }

                adapter.setDataSet(dataSet)
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<FetchPetScheduleResDto>, t: Throwable) {
                Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}