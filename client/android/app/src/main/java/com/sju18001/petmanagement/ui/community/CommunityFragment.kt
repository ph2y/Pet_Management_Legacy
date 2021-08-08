package com.sju18001.petmanagement.ui.community

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        binding.createPostFab.setOnClickListener{
            // TODO: 글쓰기 기능
        }
        
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeAdapter(){
        adapter = CommunityPostListAdapter(arrayListOf())
        binding.communityHomeRecyclerView?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)
            
            // 스크롤하여, 최하단에 위치할 시 post 추가 로드
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if(!recyclerView.canScrollVertically(1)){
                        updatePosts()
                    }
                }
            })
        }

        // 초기 post 추가
        updatePosts()
    }
    
    private fun updatePosts(){
        val items = getFetchedPost()
        adapter.addItems(items)
        adapter.notifyDataSetChanged()
    }
    
    private fun getFetchedPost(): List<CommunityPost>{
        // TODO: 서버의 fetch post 기능이 구현되면 적용할 것
        val item = CommunityPost("rachmaninoff", "url", "몽자", listOf(), "테스트용!", 25)
        return listOf<CommunityPost>(item, item, item, item, item)
    }
}