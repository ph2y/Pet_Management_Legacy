package com.sju18001.petmanagement.ui.community.comment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeAdapter(){
        adapter = CommunityCommentListAdapter(arrayListOf())
        binding.recyclerViewComment?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)
        }

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
        val item1 = Comment("default", "liszt", "불닭볶음면 맛있네요..^^", "6시간")
        val item2 = Comment("default", "franz", "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.", "5시간")
        return listOf(item1, item2, item1, item2, item1, item1, item2, item1, item2, item1, item1, item2, item1, item2, item1)
    }
}