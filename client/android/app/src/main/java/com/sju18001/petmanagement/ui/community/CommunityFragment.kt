package com.sju18001.petmanagement.ui.community

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentCommunityBinding
import com.sju18001.petmanagement.restapi.dao.Post
import com.sju18001.petmanagement.ui.map.CommunityViewModel

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
        adapter.communityPostListAdapterInterface = object: CommunityPostListAdapterInterface {
            override fun startCommunityActivityForCommentFragment() {
                val communityActivityIntent = Intent(context, CommunityActivity::class.java)
                communityActivityIntent
                    .putExtra("fragmentType", "comment_fragment")
                // TODO: 댓글 정보 전달
                startActivity(communityActivityIntent)
                requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }
        }
        binding.communityRecyclerView?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)
            
            // 스크롤하여, 최하단에 위치할 시 post 추가 로드
            it.addOnScrollListener(object: RecyclerView.OnScrollListener() {
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
        binding.communityRecyclerView.post{
            adapter.notifyDataSetChanged()
        }
    }
    
    private fun getFetchedPost(): List<Post>{
        // TODO: 서버의 fetch post 기능이 구현되면 적용할 것
        val item1 = Post("rachmaninoff", "url", "몽자", listOf(), "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.", 25)
        val item2 = Post("liszt", "url", "탱이", listOf(), "item2", 128)
        return listOf(item1, item2, item1, item2, item1)
    }
}