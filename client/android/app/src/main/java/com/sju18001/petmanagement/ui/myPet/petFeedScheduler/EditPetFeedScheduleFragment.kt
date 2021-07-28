package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentAddEditPetBinding
import com.sju18001.petmanagement.databinding.FragmentEditPetFeedScheduleBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.PetProfileFetchResponseDto
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import com.sju18001.petmanagement.ui.myPet.petManager.PetListItem
import com.sju18001.petmanagement.ui.signIn.SignInViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class EditPetFeedScheduleFragment : Fragment() {
    private var _binding: FragmentEditPetFeedScheduleBinding? = null
    private val binding get() = _binding!!

    private val myPetViewModel: MyPetViewModel by activityViewModels()

    // variable for storing API call(for cancel)
    private var petProfileFetchApiCall: Call<List<PetProfileFetchResponseDto>>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    // 리싸이클러뷰
    private lateinit var adapter: PetNameListAdapter

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPetFeedScheduleBinding.inflate(inflater, container, false)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        // 상태 로드
        loadState(myPetViewModel)

        // Timepicker
        binding.feedTimePicker.setOnTimeChangedListener { _, hour, minute ->
            myPetViewModel.feedTimeHour = hour
            myPetViewModel.feedTimeMinute = minute
        }

        // 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            activity?.finish()
        }

        // 취소 버튼
        binding.cancelButton.setOnClickListener {
            activity?.finish()
        }

        // 확인 버튼
        binding.confirmButton.setOnClickListener {
            // TODO: 현재 값들로 Create API call
            activity?.finish()
        }

        // 리싸이클러뷰
        adapter = PetNameListAdapter(arrayListOf())
        binding.petNameListRecyclerView.adapter = adapter
        binding.petNameListRecyclerView.layoutManager = LinearLayoutManager(activity)

        addPetNameList()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun loadState(myPetViewModel: MyPetViewModel){
        // Timepicker
        binding.feedTimePicker.hour = myPetViewModel.feedTimeHour
        binding.feedTimePicker.minute = myPetViewModel.feedTimeMinute
    }

    private fun addPetNameList(){
        petProfileFetchApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).petProfileFetchRequest()
        petProfileFetchApiCall!!.enqueue(object: Callback<List<PetProfileFetchResponseDto>> {
            override fun onResponse(
                call: Call<List<PetProfileFetchResponseDto>>,
                response: Response<List<PetProfileFetchResponseDto>>
            ) {
                response.body()?.map {
                    adapter.addItem(PetNameListItem(it.name))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<List<PetProfileFetchResponseDto>>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }
}