package com.sju18001.petmanagement.ui.community.followerFollowing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.databinding.FragmentFollowingBinding
import com.sju18001.petmanagement.restapi.SessionManager

class FollowingFragment : Fragment() {

    // variable for ViewModel
    val followerFollowingViewModel: FollowerFollowingViewModel by activityViewModels()

    // variables for view binding
    private var _binding: FragmentFollowingBinding? = null
    private val binding get() = _binding!!

    // variables for RecyclerView
    // TODO

    // variable for storing API call(for cancel)
    // TODO

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // stop api call when fragment is destroyed
        // TODO
    }
}