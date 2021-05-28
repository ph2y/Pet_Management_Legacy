package com.sju18001.petmanagement.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentMapBinding
import com.sju18001.petmanagement.restapi.KakaoApi
import com.sju18001.petmanagement.restapi.Documents

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapFragment : Fragment(), MapView.CurrentLocationEventListener {
    val API_KEY = "KakaoAK fcb50b998a702691c31e6e2b3a4555be"
    val BASE_URL = "https://dapi.kakao.com/"

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
    private var searchRadiusMeter: Int = 3000

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

        // 현재 위치 버튼
        root.findViewById<FloatingActionButton>(R.id.currentLocationButton).setOnClickListener(View.OnClickListener {
            setMapCenterPointToCurrentLocation(mapView)
        })

        // 검색 버튼
        val searchTextInput = root.findViewById<EditText>(R.id.search_text_input)
        searchTextInput.setOnEditorActionListener{ textView, action, event ->
            searchKeyword(textView.text.toString())
            hideKeyboard(textView)

            true
        }

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

    private fun searchKeyword(keyword: String){
        // Retrofit 구성
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoApi::class.java)
        
        // GET 요청
        try{
            val call = api.getSearchKeyword(
                API_KEY,
                keyword,
                currentMapPoint!!.mapPointGeoCoord.longitude.toString(),
                currentMapPoint!!.mapPointGeoCoord.latitude.toString(),
                searchRadiusMeter
            )

            call.enqueue(object: Callback<Documents> {
                override fun onResponse(
                    call: Call<Documents>,
                    response: Response<Documents>
                ) {
                    Log.i("성공", response.body().toString())
                }

                override fun onFailure(call: Call<Documents>, t: Throwable) {
                    Log.i("MapFragment", "Failed To Get Request: ${t.message}")
                }

            })
        }catch(e: Exception){
            // currentMapPoint가 아직 초기화되지 않았을 경우
            Log.e("MapFragment", e.stackTrace.toString())
        }
    }

    private fun hideKeyboard(view: View){
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }
}