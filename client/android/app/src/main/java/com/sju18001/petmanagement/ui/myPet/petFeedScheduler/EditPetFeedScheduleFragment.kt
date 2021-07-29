package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentAddEditPetBinding
import com.sju18001.petmanagement.databinding.FragmentEditPetFeedScheduleBinding
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import com.sju18001.petmanagement.ui.signIn.SignInViewModel
import java.time.LocalTime
import java.util.*

class EditPetFeedScheduleFragment : Fragment() {
    private var _binding: FragmentEditPetFeedScheduleBinding? = null
    private val binding get() = _binding!!

    // 리싸이클러뷰
    private lateinit var adapter: PetNameListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPetFeedScheduleBinding.inflate(inflater, container, false)

        val myPetViewModel: MyPetViewModel by activityViewModels()

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
        // TODO: 펫 리스트(name, id) Fetch API call
        val dataSet = arrayListOf(PetNameListItem("냠냠미"), PetNameListItem("몀몀미"))

        adapter = PetNameListAdapter(dataSet)
        binding.petNameListRecyclerView.adapter = adapter
        binding.petNameListRecyclerView.layoutManager = LinearLayoutManager(activity)

        return binding.root
    }

    private fun loadState(myPetViewModel: MyPetViewModel){
        // Timepicker
        binding.feedTimePicker.hour = myPetViewModel.feedTimeHour
        binding.feedTimePicker.minute = myPetViewModel.feedTimeMinute
    }
}