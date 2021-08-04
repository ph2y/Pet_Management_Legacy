<<<<<<< HEAD:client/android/app/src/main/java/com/sju18001/petmanagement/ui/login/recovery/RecoveryFragment.kt
package com.sju18001.petmanagement.ui.signIn.recovery
=======
package com.sju18001.petmanagement.ui.login.findIdPw
>>>>>>> hotfix/client-refactoring-for-signIn-and-signUp:client/android/app/src/main/java/com/sju18001/petmanagement/ui/login/findIdPw/FindIdPwFragment.kt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
<<<<<<< HEAD:client/android/app/src/main/java/com/sju18001/petmanagement/ui/login/recovery/RecoveryFragment.kt
import com.sju18001.petmanagement.databinding.FragmentRecoveryBinding
=======
import com.sju18001.petmanagement.databinding.FragmentFindIdPwBinding
>>>>>>> hotfix/client-refactoring-for-signIn-and-signUp:client/android/app/src/main/java/com/sju18001/petmanagement/ui/login/findIdPw/FindIdPwFragment.kt

private val TAB_ELEMENTS = listOf("아이디 찾기", "비밀번호 찾기")

class RecoveryFragment: Fragment() {
    private var _binding: FragmentRecoveryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecoveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager
        viewPager.adapter = RecoveryCollectionAdapter(this)

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

    class RecoveryCollectionAdapter(fragment: Fragment): FragmentStateAdapter(fragment){
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> RecoverUsernameFragment()
                else -> RecoverPasswordFragment()
            }
        }
    }
}