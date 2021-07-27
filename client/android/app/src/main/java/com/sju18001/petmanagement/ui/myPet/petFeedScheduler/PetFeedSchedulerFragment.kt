package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.databinding.FragmentPetFeedSchedulerBinding
import java.time.LocalTime

class PetFeedSchedulerFragment : Fragment() {
    private var _binding: FragmentPetFeedSchedulerBinding? = null
    private val binding get() = _binding!!

    // 리싸이클러뷰
    private lateinit var adapter: PetFeedScheduleListAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetFeedSchedulerBinding.inflate(inflater, container, false)

        // 리싸이클러뷰
        val dataSet = arrayListOf<PetFeedScheduleListItem>()

        adapter = PetFeedScheduleListAdapter(dataSet)
        binding.petFeedScheduleListRecyclerView.adapter = adapter
        binding.petFeedScheduleListRecyclerView.layoutManager = LinearLayoutManager(activity)

        // 추가 버튼
        binding.addPetFeedScheduleFab.setOnClickListener{
            val newItem = PetFeedScheduleListItem(LocalTime.of(9, 0), arrayListOf(1, 2), "밥 주기", true)
            adapter.addItem(newItem)
            adapter.notifyDataSetChanged()
        }

        return binding.root
    }
}