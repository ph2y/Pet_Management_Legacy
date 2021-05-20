package com.sju18001.petmanagement.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    // 권한 관련
    private val REQUEST_CODE = 100
    private val requiredPermissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel =
            ViewModelProvider(this).get(CommunityViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        // 권한 확인 및 획득
        val deniedPermissions = getDeniedPermissions()
        requestPermissions(deniedPermissions)

        // MavView 초기화
        val mapView = MapView(this.activity)

        val mapViewContainer = root?.findViewById<ViewGroup>(R.id.map_view)!!
        mapViewContainer.addView(mapView)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDeniedPermissions():ArrayList<String>{
        var deniedPermissions = ArrayList<String>()

        for(p in requiredPermissions){
            if(ContextCompat.checkSelfPermission(requireContext(), p) == PackageManager.PERMISSION_DENIED){
                deniedPermissions.add(p)
            }
        }

        return deniedPermissions
    }

    private fun requestPermissions(permissions: ArrayList<String>){
        if(permissions.isNotEmpty()){
            val array = arrayOfNulls<String>(permissions.size)
            ActivityCompat.requestPermissions(requireActivity(), permissions.toArray(array), REQUEST_CODE)
        }
    }
}