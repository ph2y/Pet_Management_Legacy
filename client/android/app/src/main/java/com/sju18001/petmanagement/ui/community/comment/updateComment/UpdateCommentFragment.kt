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

        Util.showKeyboard(requireActivity(), binding.editTextUpdateComment)

        return binding.root
    }
}