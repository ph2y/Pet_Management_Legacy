package com.sju18001.petmanagement.ui.community.comment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCommunityCommentBinding
import com.sju18001.petmanagement.restapi.dao.Comment
import com.sju18001.petmanagement.restapi.dao.Post

class CommunityCommentFragment : Fragment() {

    private var _binding: FragmentCommunityCommentBinding? = null
    private val binding get() = _binding!!

    // 리싸이클러뷰
    private lateinit var adapter: CommunityCommentListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityCommentBinding.inflate(inflater, container, false)

        // 어뎁터 초기화
        initializeAdapter()

        // 리스너 추가
        setListenerOnViews()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeAdapter(){
        adapter = CommunityCommentListAdapter(arrayListOf())
        adapter.communityCommentListAdapterInterface = object: CommunityCommentListAdapterInterface{
            override fun getActivity(): Activity {
                return requireActivity()
            }
        }

        binding.recyclerViewComment?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)

            // 스크롤하여, 최하단에 위치할 시 comment 추가 로드
            it.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if(!recyclerView.canScrollVertically(1)){
                        // TODO: 서버 API와 연동하여 구현
                        updateComments()
                    }
                }
            })
        }

        // 초기 댓글 추가
        updateComments()
    }

    private fun updateComments(){
        val items = getFetchedComment()
        adapter.addItems(items)
        binding.recyclerViewComment.post{
            adapter.notifyDataSetChanged()
        }
    }

    private fun getFetchedComment(): List<Comment>{
        // TODO: 서버의 fetch comment 기능이 구현되면 적용할 것
        return listOf()
    }

    private fun setListenerOnViews(){
        // 뒤로가기 버튼
        binding.buttonBack.setOnClickListener {
            activity?.finish()
        }

        // 편의 기능: 키보드 내리기
        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentCommunityCommentParentLayout)
        
        // 댓글 / 답글 생성
        binding.buttonCreateComment.setOnClickListener {
            // TODO: 댓글/답글 CREATE
        }
    }
}