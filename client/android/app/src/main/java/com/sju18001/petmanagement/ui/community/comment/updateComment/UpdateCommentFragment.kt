package com.sju18001.petmanagement.ui.community.comment.updateComment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentUpdateCommentBinding

class UpdateCommentFragment : Fragment() {
    private var _binding: FragmentUpdateCommentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateCommentBinding.inflate(inflater, container, false)
        
        // 초기화
        loadContents()
        setListenerOnViews()

        // 키보드 내리기
        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentUpdateCommentParentLayout)

        // 포커스
        val editTextUpdateComment = binding.editTextUpdateComment
        editTextUpdateComment.postDelayed({
            Util.showKeyboard(requireActivity(), editTextUpdateComment)
        }, 100)

        return binding.root
    }

    private fun loadContents(){
        val intent = requireActivity().intent
        val contents = intent.getStringExtra("contents")

        if(contents != null && contents.isNotEmpty()){
            binding.editTextUpdateComment.setText(contents)
            intent.putExtra("contents", "")
        }
    }

    private fun setListenerOnViews(){
        // 뒤로가기 버튼
        binding.buttonBack.setOnClickListener {
            activity?.finish()
        }
    }
}