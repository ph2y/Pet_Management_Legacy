package com.sju18001.petmanagement.ui.myPage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentMyPageBinding
import com.sju18001.petmanagement.ui.myPage.account.EditAccountFragment
import com.sju18001.petmanagement.ui.myPage.preferences.PreferencesFragment

class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onStart() {
        super.onStart()

        binding.accountLookup.setOnClickListener {
            val accountLookupIntent = Intent(context, MyPageActivity::class.java)
            accountLookupIntent.putExtra("fragmentType", "account_edit")
            startActivity(accountLookupIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        binding.preferencesLookup.setOnClickListener {
            val preferencesLookupIntent = Intent(context, MyPageActivity::class.java)
            preferencesLookupIntent.putExtra("fragmentType", "preferences")
            startActivity(preferencesLookupIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        binding.notificationLookup.setOnClickListener {
            val notificationPreferencesIntent = Intent(context, MyPageActivity::class.java)
            notificationPreferencesIntent.putExtra("fragmentType", "notification_preferences")
            startActivity(notificationPreferencesIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        binding.themeLookup.setOnClickListener {
            val themePreferencesIntent = Intent(context, MyPageActivity::class.java)
            themePreferencesIntent.putExtra("fragmentType", "theme_preferences")
            startActivity(themePreferencesIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}