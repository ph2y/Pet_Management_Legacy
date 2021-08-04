package com.sju18001.petmanagement.ui.login.findIdPw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.sju18001.petmanagement.databinding.FragmentFindIdPwBinding

private val TAB_ELEMENTS = listOf("아이디 찾기", "비밀번호 찾기")

class FindIdPwFragment: Fragment() {
    private var _binding: FragmentFindIdPwBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFindIdPwBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager
        viewPager.adapter = FindIdPwCollectionAdapter(this)

        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            tab.text = TAB_ELEMENTS[position]
        }.attach()
    }

    override fun onStart() {
        super.onStart()

        // for back button(top-left)
        binding.backButton.setOnClickListener{
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class FindIdPwCollectionAdapter(fragment: Fragment): FragmentStateAdapter(fragment){
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> FindIdFragment()
                else -> FindPwFragment()
            }
        }
    }
}