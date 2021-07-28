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

class PetFeedSchedulerFragment : Fragment() {
    private var _binding: FragmentPetFeedSchedulerBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    lateinit var myPetViewModel: MyPetViewModel

    // 리싸이클러뷰
    private lateinit var adapter: PetFeedScheduleListAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetFeedSchedulerBinding.inflate(inflater, container, false)

        myPetViewModel = ViewModelProvider(this).get(MyPetViewModel::class.java)

        // 리싸이클러뷰
        // TODO: 스케줄 로드 & 시간에 따른 결합
        val dataSet = arrayListOf<PetFeedScheduleListItem>()

        adapter = PetFeedScheduleListAdapter(dataSet)
        binding.petFeedScheduleListRecyclerView.adapter = adapter
        binding.petFeedScheduleListRecyclerView.layoutManager = LinearLayoutManager(activity)

        // 추가 버튼
        binding.addPetFeedScheduleFab.setOnClickListener{
            val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
            myPetActivityIntent.putExtra("fragmentType", "edit_pet_feed_schedule")
            startActivity(myPetActivityIntent)
        }

        return binding.root
    }
}