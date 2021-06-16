package com.sju18001.petmanagement.ui.map

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentMapBinding
import com.sju18001.petmanagement.restapi.KakaoApi
import com.sju18001.petmanagement.restapi.Documents
import com.sju18001.petmanagement.restapi.Place

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.daum.android.map.MapViewEventListener
import net.daum.mf.map.api.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapFragment : Fragment(), MapView.CurrentLocationEventListener, MapView.MapViewEventListener, MapView.POIItemEventListener {
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
    private var searchTextInput: EditText? = null

    private var navView: View? = null
    private var navViewFirstHeight: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel =
            ViewModelProvider(this).get(CommunityViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

        // 권한 확인 및 획득
        val deniedPermissions = getDeniedPermissions()
        requestPermissions(deniedPermissions)

        // MavView 초기화
        val mapView = MapView(this.activity)
        val mapViewContainer = root?.findViewById<ViewGroup>(R.id.map_view)!!
        mapViewContainer.addView(mapView)
        mapView.setCurrentLocationEventListener(this)
        mapView.setMapViewEventListener(this)
        mapView.setPOIItemEventListener(this)
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
        setMapCenterPointToCurrentLocation(mapView)

        // 현재 위치 버튼
        root.findViewById<FloatingActionButton>(R.id.currentLocationButton).setOnClickListener(View.OnClickListener {
            setMapCenterPointToCurrentLocation(mapView)
        })

        // 검색바
        searchTextInput = root.findViewById<EditText>(R.id.search_text_input)
        var searchTextCancel = root.findViewById<ImageButton>(R.id.search_text_cancel)

        searchTextInput!!.setOnEditorActionListener{ textView, action, event ->
            searchKeyword(textView.text.toString(), mapView)
            hideKeyboard(textView)

            /* WARNING: 에뮬레이터에서 Circle이 정상 작동하지 않을 시 밑의 3줄 주석 처리를 해야한다.
            setMapCenterPointToCurrentLocation(mapView)
            val searchAreaCircle = addCircleCenteredAtCurrentLocation(mapView, searchRadiusMeter)
            moveCameraOnCircle(mapView, searchAreaCircle!!, 50) */

            true
        }

        searchTextInput!!.addTextChangedListener {
            if(!searchTextInput!!.text.isEmpty()){
                searchTextCancel.visibility = View.VISIBLE
            }else{
                searchTextCancel.visibility = View.INVISIBLE
            }
        }

        searchTextCancel!!.setOnClickListener{
            searchTextInput!!.setText("")
            hideKeyboard(searchTextInput!!)
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

    private fun addCircleCenteredAtCurrentLocation(mapView: MapView, radius: Int): MapCircle?{
        var searchAreaCircle:MapCircle? = null
        mapView.removeAllCircles()

        try{
            searchAreaCircle = MapCircle(
                currentMapPoint,
                radius,
                Color.argb(128, 255, 0, 0),
                Color.argb(0, 0, 0, 0)
            )
            searchAreaCircle.tag = 1000
            mapView.addCircle(searchAreaCircle)
        }catch(e: Exception){
            // currentMapPoint가 아직 초기화되지 않았을 경우
            Log.e("MapFragment", e.stackTrace.toString())
        }

        return searchAreaCircle
    }
    private fun moveCameraOnCircle(mapView: MapView, circle: MapCircle, padding: Int){
        try{
            val mapPointBoundsArray = arrayOf(circle.bound, circle.bound)
            val mapPointBounds = MapPointBounds(mapPointBoundsArray)
            mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding))
        }catch(e:Exception){
            // circle을 반환받지 못했을 경우
            Log.i("MapFragment", e.stackTrace.toString())
        }
    }

    // 검색 및 JSON 데이터 로드
    private fun searchKeyword(keyword: String, mapView:MapView){
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
                    addPOIItemsForDocuments(response.body()!!.documents, mapView)
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

    // Response 데이터 처리
    private fun addPOIItemsForDocuments(documents: List<Place>, mapView: MapView){
        mapView.removeAllPOIItems()

        for(i: Int in 0 until documents.count()){
            val newMarker: MapPOIItem = MapPOIItem()
            newMarker.itemName = documents[i].place_name
            newMarker.tag = i
            newMarker.mapPoint = MapPoint.mapPointWithGeoCoord(documents[i].y.toDouble(), documents[i].x.toDouble())
            newMarker.markerType = MapPOIItem.MarkerType.BluePin
            newMarker.isShowCalloutBalloonOnTouch = false

            mapView.addPOIItem(newMarker)
        }
    }

    private fun hideKeyboard(view: View){
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    private fun showLocationInformation(item: MapPOIItem?){
        if(navView!=null){
            navViewFirstHeight = navView!!.height
            val anim = ValueAnimator.ofInt(navViewFirstHeight!!, 0)
            anim.addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int
                navView!!.layoutParams.height = value
                navView!!.requestLayout()
            }
            anim.duration = 50
            anim.start()
        }else{
            Log.e("MapFragment", "navView is null.")
        }
    }

    private fun hideLocationInformation(){
        if(navView != null && navViewFirstHeight != null){
            val anim = ValueAnimator.ofInt(0, navViewFirstHeight!!)
            anim.addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int
                navView!!.layoutParams.height = value
                navView!!.requestLayout()
            }
            anim.duration = 50
            anim.start()
        }else{
            Log.e("MapFragment", "navView or navViewFirstHeight is null.")
        }
    }
    
    // CurrentLocationEventListener 인터페이스 구현
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
    
    // MapViewEventListener 인터페이스 구현
    override fun onMapViewInitialized(p0: MapView?) {
    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
        if(searchTextInput!!.isFocused == true){
            hideKeyboard(searchTextInput!!)
        }

        if(navView!!.height == 0){
            hideLocationInformation()
        }
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
    }
    
    // MapView.POIItemEventListener 인터페이스 구현
    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        if(p1!=null){
            showLocationInformation(p1)
        }
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
    }

    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
    }
}