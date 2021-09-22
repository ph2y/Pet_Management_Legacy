package com.sju18001.petmanagement.ui.community

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentCommunityBinding
import com.sju18001.petmanagement.ui.community.post.PostFragment


class CommunityFragment : Fragment() {
    val communityViewModel: CommunityViewModel by activityViewModels()

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private var postFragment: PostFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)

        // For create post FAB
        binding.createPostFab.setOnClickListener {
            postFragment?.let{
                it.startCreatePostFragment()
            }
        }

        // Set PostFragment
        if(childFragmentManager.findFragmentById(R.id.post_fragment_container) == null){
            postFragment = PostFragment()
            childFragmentManager
                .beginTransaction()
                .add(R.id.post_fragment_container, postFragment!!)
                .commit()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun startAllVideos(){
        postFragment?.let{
            it.startAllVideos()
        }
    }

    fun pauseAllVideos(){
        postFragment?.let{
            it.pauseAllVideos()
        }
    }
}