package com.sju18001.petmanagement.ui.myPage.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.databinding.FragmentAccountEditBinding

class EditAccountFragment : Fragment() {
    private lateinit var editAccountViewModel: EditAccountViewModel
    private var _binding: FragmentAccountEditBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        editAccountViewModel =
            ViewModelProvider(this).get(EditAccountViewModel::class.java)

        _binding = FragmentAccountEditBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onResume() {
        super.onResume()

        val intent = requireActivity().intent

        binding.nicknameEdit.setText(intent.getStringExtra("nickname"))
        binding.emailEdit.setText(intent.getStringExtra("email"))
        binding.phoneEdit.setText(intent.getStringExtra("phone"))
        binding.marketingSwitch.isChecked = intent.getBooleanExtra("marketing", false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}