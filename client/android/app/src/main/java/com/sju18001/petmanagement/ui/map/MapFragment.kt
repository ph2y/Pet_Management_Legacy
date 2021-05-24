package com.sju18001.petmanagement.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPoint

import net.daum.mf.map.api.MapView

class MapFragment : Fragment(), MapView.CurrentLocationEventListener {

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

    // 지도 관련
    private var currentMapPoint: MapPoint? = null
    private var isLoadingCurrentMapPoint: Boolean = false

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

        // 트랙킹 모드 및 현재 위치로 맵 이동
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
        mapView.setCurrentLocationEventListener(this)
        setMapCenterPointToCurrentLocation(mapView)

        // 현재 위치 fab 버튼
        root.findViewById<FloatingActionButton>(R.id.currentLocationButton).setOnClickListener(View.OnClickListener {
            setMapCenterPointToCurrentLocation(mapView)
        })

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

    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
        if (p1 != null) {
            currentMapPoint = p1
        }
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
    }

    private fun setMapCenterPointToCurrentLocation(mapView: MapView){
        if(currentMapPoint!=null){
            mapView.setMapCenterPoint(currentMapPoint, true)
        }else{ // 현재 위치 정보를 얻기 전
            if(!isLoadingCurrentMapPoint){
                isLoadingCurrentMapPoint = true

                GlobalScope.launch(Dispatchers.IO){
                    while(currentMapPoint==null){
                        // Wait for Loading
                        delay(100L)
                    }

                    mapView.setMapCenterPoint(currentMapPoint, true)
                }
            }
        }
    }
}