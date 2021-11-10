package com.sju18001.petmanagement.ui.community.comment.updateComment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentUpdateCommentBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.UpdateCommentReqDto
import com.sju18001.petmanagement.restapi.dto.UpdateCommentResDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateCommentFragment : Fragment() {
    private var _binding: FragmentUpdateCommentBinding? = null
    private val binding get() = _binding!!

    private var isViewDestroyed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateCommentBinding.inflate(inflater, container, false)
        isViewDestroyed = false

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
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

        // 확인 버튼
        binding.buttonConfirm.setOnClickListener {
            updateComment()
        }
    }

    private fun updateComment(){
        lockInputs()

        val body = UpdateCommentReqDto(
            requireActivity().intent.getLongExtra("id", -1), binding.editTextUpdateComment.text.toString()
        )
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!).updateCommentReq(body)
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            // Pass datum for comment
            val intent = Intent()
            val newContents = binding.editTextUpdateComment.text.toString().replace("\n", "")
            intent.putExtra("newContents", newContents)
            intent.putExtra("position", requireActivity().intent.getIntExtra("position", -1))
            requireActivity().setResult(RESULT_OK, intent)

            Toast.makeText(context, context?.getText(R.string.update_comment_success), Toast.LENGTH_SHORT).show()
            activity?.finish()
        }, { unlockInputs() }, { unlockInputs() })
    }


    private fun lockInputs(){
        binding.buttonConfirm.visibility = View.GONE
        binding.updateCommentProgressBar.visibility = View.VISIBLE
        binding.editTextUpdateComment.isEnabled = false
    }

    private fun unlockInputs(){
        binding.buttonConfirm.visibility = View.VISIBLE
        binding.updateCommentProgressBar.visibility = View.GONE
        binding.editTextUpdateComment.isEnabled = true
    }
}