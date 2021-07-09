package com.sju18001.petmanagement.ui.map

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Permission
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentMapBinding
import com.sju18001.petmanagement.restapi.Documents
import com.sju18001.petmanagement.restapi.KakaoApi
import com.sju18001.petmanagement.restapi.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    // 지도 관련
    private var currentMapPoint: MapPoint? = null
    private var isLoadingCurrentMapPoint: Boolean = false
    private var searchRadiusMeter: Int = 3000
    private var searchTextInput: EditText? = null

    // 상수
    private val CURRENT_LOCATION_BUTTON_MARGIN: Int = 16
    private val NAVVIEW_HEIGHT: Int = 56
    private val LOCATION_INFORMATION_HEIGHT: Int = 128
    private val ANIMATION_DURATION: Long = 200

    // View
    private var currentLocationButton: View? = null
    private var navView: View? = null
    private var locationInformation: View? = null

    // 검색 기능 관련
    private var currentDocuments: List<Place>? = null

    // 애니메이션
    private var showingNavViewAnim: ValueAnimator? = null
    private var hidingNavViewAnim: ValueAnimator? = null
    private var increasingCurrentLocationButtonMarginAnim: ValueAnimator? = null
    private var decreasingCurrentLocationButtonMarginAnim: ValueAnimator? = null
    private var showingLocationInformationAnim: ValueAnimator? = null
    private var hidingLocationInformationAnim: ValueAnimator? = null

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


        // 맵 권한
        val permission = Permission()
        permission.requestDeniedPermissions(requireActivity(), permission.requiredPermissionsForMap)


        // MavView 초기화
        val mapView = MapView(this.activity)
        val mapViewContainer = root?.findViewById<ViewGroup>(R.id.map_view)!!
        mapViewContainer.addView(mapView)

        mapView.setCurrentLocationEventListener(this)
        mapView.setMapViewEventListener(this)
        mapView.setPOIItemEventListener(this)

        mapView.setCustomCurrentLocationMarkerTrackingImage(R.drawable.marker_current_location, MapPOIItem.ImageOffset(16, 16))
        mapView.setCalloutBalloonAdapter(CustomCalloutBalloonAdapter(inflater))
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving


        setMapCenterPointToCurrentLocation(mapView)


        // 현재 위치 버튼
        currentLocationButton = root.findViewById<FloatingActionButton>(R.id.currentLocationButton)
        currentLocationButton!!.setOnClickListener(View.OnClickListener {
            setMapCenterPointToCurrentLocation(mapView)
        })
        val currentLocationButtonParams = currentLocationButton!!.layoutParams as ViewGroup.MarginLayoutParams
        currentLocationButtonParams.bottomMargin += Util().convertDpToPixel(NAVVIEW_HEIGHT)
        currentLocationButton!!.layoutParams = currentLocationButtonParams


        // 검색바
        searchTextInput = root.findViewById<EditText>(R.id.search_text_input)
        var searchTextCancel = root.findViewById<ImageButton>(R.id.search_text_cancel)

        searchTextInput!!.setOnEditorActionListener{ textView, action, event ->
            searchKeyword(textView.text.toString(), mapView)
            Util().hideKeyboard(requireActivity(), textView)

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
            Util().hideKeyboard(requireActivity(), searchTextInput!!)
        }


        // 애니메이션 초기화
        initializeAnimations()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    // * MapView
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


    // * 검색
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
                    val body = response.body()
                    if(body != null){
                        currentDocuments = body.documents
                        addPOIItemsForDocuments(currentDocuments!!, mapView)
                    }
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

    private fun addPOIItemsForDocuments(documents: List<Place>, mapView: MapView){
        mapView.removeAllPOIItems()

        for(i: Int in 0 until documents.count()){
            var iconId: Int = when(documents[i].category_group_code){
                "MT1", "CS2" -> R.drawable.marker_store
                "PS3", "SC4", "AC5" -> R.drawable.marker_school
                "PK6", "OL7" -> R.drawable.marker_car
                "SW8" -> R.drawable.marker_train
                "CE7" -> R.drawable.marker_cafe
                "HP8", "PM9" -> R.drawable.marker_hospital
                else -> R.drawable.marker_default
            }

            val newMarker: MapPOIItem = MapPOIItem().apply{
                itemName = documents[i].place_name
                tag = i
                mapPoint = MapPoint.mapPointWithGeoCoord(documents[i].y.toDouble(), documents[i].x.toDouble())

                markerType = MapPOIItem.MarkerType.CustomImage
                isCustomImageAutoscale = false
                setCustomImageAnchor(0.5f, 0.5f)
                customImageResourceId = iconId
            }

            mapView.addPOIItem(newMarker)
        }
    }


    // * 특정 위치의 정보 표시
    private fun showLocationInformation(item: MapPOIItem){
        try{
            if(isAnimationRunning()){
                return
            }

            // 네비게이션바가 열려있는 상태일 때
            if(navView!!.height > 0){
                hidingNavViewAnim!!.doOnEnd { anim ->
                    showingLocationInformationAnim!!.start()

                    anim.removeAllListeners()
                }

                updateLocationInformation(item)

                hidingNavViewAnim!!.start()
                decreasingCurrentLocationButtonMarginAnim!!.start()
            }else{ // 장소 정보가 열려있는 상태일 때
                hidingLocationInformationAnim!!.doOnEnd { anim ->
                    updateLocationInformation(item)

                    showingLocationInformationAnim!!.start()

                    anim.removeAllListeners()
                }

                hidingLocationInformationAnim!!.start()
            }
        }catch(e: Exception){
            Log.e("MapFragment", "Failed to show location information: " + e.message)
        }
    }

    private fun updateLocationInformation(item: MapPOIItem){
        if(currentDocuments != null){
            val document = currentDocuments!![item.tag]

            setLocationInformationTexts(document)
            setLocationInformationButtons(document)
        }else{
            Log.e("MapFragment", "currentDocuments is null")
        }
    }

    private fun setLocationInformationTexts(document: Place){
        val locationPlaceName = requireActivity().findViewById<TextView>(R.id.location_place_name)
        val locationCategoryName = requireActivity().findViewById<TextView>(R.id.location_category_name)
        val locationDistance = requireActivity().findViewById<TextView>(R.id.location_distance)

        locationPlaceName.text = document.place_name
        locationCategoryName.text = document.category_group_name
        setLocationDistance(locationDistance, document.distance)
    }

    private fun setLocationInformationButtons(document: Place){
        setLocationInformationPathfindingButton(document)
        setLocationInformationCallButton(document)
        setLocationInformationShareButton(document)
    }

    private fun setLocationInformationPathfindingButton(document: Place){
        val builder = AlertDialog.Builder(context)
            .setTitle("길찾기")
            .setMessage("길찾기를 위해 카카오맵 웹페이지로 이동합니다.")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { _, _ ->
                    Util().openWebPage(requireActivity(), "https://map.kakao.com/link/to/" + document.id)
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
                })
            .create()

        // 버튼 이벤트
        val pathfindingButton = requireActivity().findViewById<ImageButton>(R.id.pathfinding_button)
        pathfindingButton.setOnClickListener{ _ ->
            builder.show()
        }
    }

    private fun setLocationInformationCallButton(document: Place){
        // 전화번호 정보가 없을 때는 전화 버튼을 숨김
        if(document.phone.isEmpty()){
            setCallButtonHorizontalWeight(0f)
        }else{
            setCallButtonHorizontalWeight(1f)

            // 전화 및 연락처 권한 획득
            val permission = Permission()
            permission.requestDeniedPermissions(requireActivity(), permission.requiredPermissionsForCall)
            permission.requestDeniedPermissions(requireActivity(), permission.requiredPermissionsForContacts)

            // AlertDialog 구성
            val buttonStrings: Array<CharSequence> = arrayOf("전화하기", "연락처 저장하기", "클립보드에 복사하기")
            val builder = AlertDialog.Builder(context)
                .setTitle(document.phone)
                .setItems(buttonStrings,
                    DialogInterface.OnClickListener{ _, which ->
                        when(which){
                            0 -> Util().doCall(requireActivity(), document.phone)
                            1 -> Util().insertContactsContract(requireActivity(), document)
                            2 -> Util().doCopy(requireActivity(), document.phone)
                        }
                    })
                .setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.cancel()
                    })
                .create()
            
            // 버튼 이벤트
            val callButton = requireActivity().findViewById<ImageButton>(R.id.call_button)
            callButton.setOnClickListener{ _ ->
                builder.show()
            }
        }
    }

    private fun setCallButtonHorizontalWeight(weight: Float){
        val locationInformationButtons = requireActivity().findViewById<ConstraintLayout>(R.id.location_information_buttons)
        val constraintSet = ConstraintSet()

        constraintSet.clone(locationInformationButtons)
        constraintSet.setHorizontalWeight(R.id.call_button, weight)
        constraintSet.applyTo(locationInformationButtons)
    }

    private fun setLocationInformationShareButton(document: Place){
        // 버튼 이벤트
        val shareButton = requireActivity().findViewById<ImageButton>(R.id.share_button)
        shareButton.setOnClickListener{ _ ->
            Util().shareText(requireActivity(), document.place_url)
        }
    }

    private fun setLocationDistance(locationDistance: TextView, distance: String){
        locationDistance.text = distance + "m"

        // 거리에 따라 색상을 지정함
        val color:Int = when (distance.toInt()){
            in 0..750 -> ContextCompat.getColor(requireContext(), R.color.emerald)
            in 751..1500 -> ContextCompat.getColor(requireContext(), R.color.sunflower)
            in 1501..2250 -> ContextCompat.getColor(requireContext(), R.color.carrot)
            else -> ContextCompat.getColor(requireContext(), R.color.alizarin)
        }

        locationDistance.setTextColor(color)
    }

    private fun hideLocationInformation(){
        try{
            if(isAnimationRunning()){
                return
            }

            hidingLocationInformationAnim!!.doOnEnd { anim ->
                showingNavViewAnim!!.start()
                increasingCurrentLocationButtonMarginAnim!!.start()

                anim.removeAllListeners()
            }

            hidingLocationInformationAnim!!.start()
        }catch(e: Exception){
            Log.e("MapFragment", "Failed to show location information: " + e.message)
        }
    }


    // * 애니메이션
    private fun initializeAnimations(){
        showingNavViewAnim = ValueAnimator.ofInt(0, Util().convertDpToPixel(NAVVIEW_HEIGHT))
        showingNavViewAnim!!.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int

            navView!!.layoutParams.height = value
            navView!!.requestLayout()
        }
        showingNavViewAnim!!.duration = ANIMATION_DURATION

        hidingNavViewAnim = ValueAnimator.ofInt(Util().convertDpToPixel(NAVVIEW_HEIGHT), 0)
        hidingNavViewAnim!!.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int

            navView!!.layoutParams.height = value
            navView!!.requestLayout()
        }
        hidingNavViewAnim!!.duration = ANIMATION_DURATION

        increasingCurrentLocationButtonMarginAnim = ValueAnimator.ofInt(Util().convertDpToPixel(CURRENT_LOCATION_BUTTON_MARGIN), Util().convertDpToPixel(NAVVIEW_HEIGHT + CURRENT_LOCATION_BUTTON_MARGIN))
        increasingCurrentLocationButtonMarginAnim!!.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            val params = currentLocationButton!!.layoutParams as (ViewGroup.MarginLayoutParams)

            params.bottomMargin = value
            currentLocationButton!!.requestLayout()
        }
        increasingCurrentLocationButtonMarginAnim!!.duration = ANIMATION_DURATION

        decreasingCurrentLocationButtonMarginAnim = ValueAnimator.ofInt(Util().convertDpToPixel(NAVVIEW_HEIGHT + CURRENT_LOCATION_BUTTON_MARGIN), Util().convertDpToPixel(CURRENT_LOCATION_BUTTON_MARGIN))
        decreasingCurrentLocationButtonMarginAnim!!.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            val params = currentLocationButton!!.layoutParams as (ViewGroup.MarginLayoutParams)

            params.bottomMargin = value
            currentLocationButton!!.requestLayout()
        }
        decreasingCurrentLocationButtonMarginAnim!!.duration = ANIMATION_DURATION

        showingLocationInformationAnim = ValueAnimator.ofInt(1, Util().convertDpToPixel(LOCATION_INFORMATION_HEIGHT))
        showingLocationInformationAnim!!.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            if(locationInformation == null) locationInformation = requireActivity().findViewById<ConstraintLayout>(R.id.location_information)

            locationInformation!!.layoutParams.height = value
            locationInformation!!.requestLayout()
        }
        showingLocationInformationAnim!!.duration = ANIMATION_DURATION

        hidingLocationInformationAnim = ValueAnimator.ofInt(Util().convertDpToPixel(LOCATION_INFORMATION_HEIGHT), 1)
        hidingLocationInformationAnim!!.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            if(locationInformation == null) locationInformation = requireActivity().findViewById<ConstraintLayout>(R.id.location_information)

            locationInformation!!.layoutParams.height = value
            locationInformation!!.requestLayout()
        }
        hidingLocationInformationAnim!!.duration = ANIMATION_DURATION
    }

    private fun isAnimationRunning(): Boolean{
        return showingNavViewAnim!!.isRunning ||
                hidingNavViewAnim!!.isRunning ||
                increasingCurrentLocationButtonMarginAnim!!.isRunning ||
                decreasingCurrentLocationButtonMarginAnim!!.isRunning ||
                showingLocationInformationAnim!!.isRunning ||
                hidingLocationInformationAnim!!.isRunning
    }


    // * CurrentLocationEventListener 인터페이스
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


    // * MapViewEventListener 인터페이스
    override fun onMapViewInitialized(p0: MapView?) {
    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
        if(searchTextInput!!.isFocused == true){
            Util().hideKeyboard(requireActivity(), searchTextInput!!)
        }

        // 장소 정보가 열려있을 때
        if(navView != null && locationInformation != null){
            if (navView!!.height == 0 && locationInformation!!.height > 0){
                hideLocationInformation()
            }
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


    // * MapView.POIItemEventListener 인터페이스
    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        if(p1!=null){
            showLocationInformation(p1!!)
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

    // * 커스텀 말풍선 클래스
    class CustomCalloutBalloonAdapter(inflater: LayoutInflater) : CalloutBalloonAdapter {
        private val mCalloutBalloon: View = inflater.inflate(R.layout.custom_callout_balloon, null)

        override fun getCalloutBalloon(poiItem: MapPOIItem): View {
            val customCalloutBalloonImage = (mCalloutBalloon.findViewById<View>(R.id.custom_callout_balloon_image) as ImageView)
            customCalloutBalloonImage.setImageResource(R.drawable.ic_baseline_place_48)
            customCalloutBalloonImage.y = 8f

            return mCalloutBalloon
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View? {
            return null
        }
    }
}