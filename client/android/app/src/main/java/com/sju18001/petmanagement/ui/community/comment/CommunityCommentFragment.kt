package com.sju18001.petmanagement.ui.community.comment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.databinding.FragmentCommunityCommentBinding

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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeAdapter(){
        adapter = CommunityCommentListAdapter(arrayListOf())
        binding.commentRecyclerView?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)
        }
    }
}