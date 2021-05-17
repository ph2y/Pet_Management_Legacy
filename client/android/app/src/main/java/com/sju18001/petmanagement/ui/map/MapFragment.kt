package com.sju18001.petmanagement.ui.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R

import com.sju18001.petmanagement.databinding.FragmentMapBinding
import net.daum.mf.map.api.MapPoint

import net.daum.mf.map.api.MapView

class MapFragment : Fragment() {

    private lateinit var mapViewModel: CommunityViewModel
    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel =
            ViewModelProvider(this).get(CommunityViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //MavView 초기화
        val mapView = MapView(this.activity)

        val mapViewContainer = root?.findViewById<ViewGroup>(R.id.map_view)!!
        mapViewContainer.addView(mapView)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}