package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentAddEditPetBinding
import com.sju18001.petmanagement.databinding.FragmentEditPetFeedScheduleBinding

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
            // TODO: 현재 값들을 PetFeedSchedulerFragment.adapter 전달 & addItem(~)
            activity?.finish()
        }

        // 리싸이클러뷰
        // TODO: 펫 리스트(name, id) 로드
        val dataSet = arrayListOf(PetNameListItem("냠냠미"), PetNameListItem("몀몀미"))

        adapter = PetNameListAdapter(dataSet)
        binding.petNameListRecyclerView.adapter = adapter
        binding.petNameListRecyclerView.layoutManager = LinearLayoutManager(activity)

        return binding.root
    }
}