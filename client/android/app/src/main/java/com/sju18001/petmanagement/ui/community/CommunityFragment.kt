package com.sju18001.petmanagement.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.databinding.FragmentCommunityBinding
import com.sju18001.petmanagement.restapi.dao.CommunityPost
import com.sju18001.petmanagement.ui.map.CommunityViewModel
import com.sju18001.petmanagement.ui.myPet.petScheduleManager.PetScheduleListAdapter

class CommunityFragment : Fragment() {

    private lateinit var communityViewModel: CommunityViewModel
    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    // 리싸이클러뷰
    private lateinit var adapter: CommunityPostListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        communityViewModel =
            ViewModelProvider(this).get(CommunityViewModel::class.java)

        _binding = FragmentCommunityBinding.inflate(inflater, container, false)

        // 어뎁터 초기화
        initializeAdapter()

        // 글쓰기 버튼
        // TODO: 현재는 테스트용를 위해 add item 기능을 수행합니다. 따라서 나중에 이것을 수정해야합니다.
        binding.createPostFab.setOnClickListener{
            val item = CommunityPost("rachmaninoff", "url", "몽자", listOf(), "테스트용!", 25)
            val items = listOf<CommunityPost>(item, item, item, item, item)
            adapter.addItems(items)
            adapter.notifyDataSetChanged()
        }
        
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeAdapter(){
        adapter = CommunityPostListAdapter(arrayListOf())
        binding.communityHomeRecyclerView.adapter = adapter
        binding.communityHomeRecyclerView.layoutManager = LinearLayoutManager(activity)
    }
}