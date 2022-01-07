package com.sju18001.petmanagement.ui.welcomePage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.sju18001.petmanagement.MainActivity
import com.sju18001.petmanagement.databinding.FragmentWelcomePageBinding

class WelcomePageFragment : Fragment() {
    private var _binding: FragmentWelcomePageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWelcomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager
        viewPager.adapter = WelcomePageCollectionAdapter(this)

        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            tab.view.isClickable = false
        }.attach()
    }

    override fun onStart() {
        super.onStart()
        
        // 스킵버튼
        binding.skipButton.setOnClickListener{
            val intent = Intent(context, MainActivity::class.java)

            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    class WelcomePageCollectionAdapter(fragment: Fragment): FragmentStateAdapter(fragment){
        override fun getItemCount(): Int = 1

        override fun createFragment(position: Int): Fragment {
            return WelcomePageProfileFragment()
        }
    }
}