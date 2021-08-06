package com.sju18001.petmanagement.ui.myPet.petScheduleManager

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentPetScheduleEditBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.time.LocalTime

class PetScheduleEditFragment : Fragment() {
    private var _binding: FragmentPetScheduleEditBinding? = null
    private val binding get() = _binding!!

    private val myPetViewModel: MyPetViewModel by activityViewModels()

    // variable for storing API call(for cancel)
    private var fetchPetApiCall: Call<FetchPetResDto>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    // 리싸이클러뷰
    private lateinit var adapter: PetNameListAdapter

    // API Calls
    private var createPetScheduleApiCall: Call<CreatePetScheduleResDto>? = null
    private var updatePetScheduleApiCall: Call<UpdatePetScheduleResDto>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetScheduleEditBinding.inflate(inflater, container, false)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        // for update_pet_schedule
        setViewForUpdate()

        // 상태 로드
        loadState(myPetViewModel)

        // Timepicker
        binding.timePicker.setOnTimeChangedListener { _, hour, minute ->
            myPetViewModel.time = LocalTime.of(hour, minute).toString()
        }

        // 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            activity?.finish()
        }

        // 취소 버튼
        binding.cancelButton.setOnClickListener {
            activity?.finish()
        }

        // 확인 버튼
        binding.confirmButton.setOnClickListener {
            val intent = requireActivity().intent

            if(intent.getStringExtra("fragmentType") == "create_pet_schedule"){
                createPetSchedule()
            }else{
                val enabled = intent.getBooleanExtra("enabled", false)
                if(enabled){
                    PetScheduleNotification.cancelNotificationWorkManager(requireContext(), intent.getStringExtra("originalTime"))
                    PetScheduleNotification.enqueueNotificationWorkManager(requireContext(),
                        LocalTime.of(binding.timePicker.hour, binding.timePicker.minute).toString()+":00",
                        binding.memoEditText.text.toString()
                    )
                }
                updatePetSchedule(intent.getLongExtra("id", 0), enabled)
            }
        }

        // 리싸이클러뷰
        adapter = PetNameListAdapter(arrayListOf())
        adapter.petNameListAdapterInterface = object: PetNameListAdapterInterface{
            override fun setCheckBoxForViewModel(checkBox: CheckBox, position: Int){
                myPetViewModel.isPetChecked?.let{
                    if(it.size >= position + 1){
                        checkBox.isChecked = it[position]
                    }
                }
            }

            override fun setViewModelForCheckBox(position: Int) {
                myPetViewModel.isPetChecked?.let{
                    if(it.size >= position + 1) {
                        it[position] = !it[position]
                    }
                }
            }
        }

        binding.petNameListRecyclerView.adapter = adapter
        binding.petNameListRecyclerView.layoutManager = LinearLayoutManager(activity)

        addPetNameList()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        createPetScheduleApiCall?.cancel()
        updatePetScheduleApiCall?.cancel()
        fetchPetApiCall?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setViewForUpdate(){
        // 일정 데이터 불러오기
        val intent = requireActivity().intent

        if(intent.getStringExtra("fragmentType") == "update_pet_schedule"){
            binding.timePicker.hour = intent.getIntExtra("hour", 0)
            binding.timePicker.minute = intent.getIntExtra("minute", 0)
            binding.memoEditText.setText(intent.getStringExtra("memo"))

            // 액션 타이틀
            binding.actionTitle.text = context?.getText(R.string.pet_schedule_update_title)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadState(myPetViewModel: MyPetViewModel){
        // Timepicker
        if(myPetViewModel.time != 0){
            LocalTime.parse(myPetViewModel.time.toString())?.let{
                binding.timePicker.hour = it.hour
                binding.timePicker.minute = it.minute
            }
        }

        // Memo
        binding.memoEditText.setText(myPetViewModel.memo?.let{ it })
    }

    private fun addPetNameList(){
        // create DTO
        val fetchPetReqDto = FetchPetReqDto( null )

        fetchPetApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchPetReq(fetchPetReqDto)
        fetchPetApiCall!!.enqueue(object: Callback<FetchPetResDto> {
            override fun onResponse(
                call: Call<FetchPetResDto>,
                response: Response<FetchPetResDto>
            ) {
                response.body()?.petList?.map {
                    adapter.addItem(PetNameListItem(it.name, it.id))
                }
                adapter.notifyDataSetChanged()

                // isPetChecked 초기화 로직
                if(!isSameLengthWithAdapter(myPetViewModel.isPetChecked)) {

                    // false 배열로 초기화
                    myPetViewModel.isPetChecked = Array(adapter.itemCount) { false }

                    // intent - extra
                    setViewModelForUpdate()
                }
            }

            override fun onFailure(call: Call<FetchPetResDto>, t: Throwable) {
                // Do nothing
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPetSchedule(){
        val createPetScheduleReqDto = CreatePetScheduleReqDto(
            getCheckedPetIdList(), LocalTime.of(binding.timePicker.hour, binding.timePicker.minute).toString(), binding.memoEditText.text.toString()
        )

        createPetScheduleApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .createPetScheduleReq(createPetScheduleReqDto)
        createPetScheduleApiCall!!.enqueue(object: Callback<CreatePetScheduleResDto> {
            override fun onResponse(
                call: Call<CreatePetScheduleResDto>,
                response: Response<CreatePetScheduleResDto>
            ) {
                if(response.isSuccessful){
                    activity?.finish()
                }else{
                    Toast.makeText(context, Util.getMessageFromErrorBody(response.errorBody()!!), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CreatePetScheduleResDto>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePetSchedule(id: Long, enabled: Boolean){
        val updatePetScheduleReqDto = UpdatePetScheduleReqDto(
            id, getCheckedPetIdList(), LocalTime.of(binding.timePicker.hour, binding.timePicker.minute).toString(), binding.memoEditText.text.toString(), enabled
        )

        updatePetScheduleApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .updatePetScheduleReq(updatePetScheduleReqDto)
        updatePetScheduleApiCall!!.enqueue(object: Callback<UpdatePetScheduleResDto> {
            override fun onResponse(
                call: Call<UpdatePetScheduleResDto>,
                response: Response<UpdatePetScheduleResDto>
            ) {
                if(response.isSuccessful){
                    activity?.finish()
                }else{
                    Toast.makeText(context, Util.getMessageFromErrorBody(response.errorBody()!!), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdatePetScheduleResDto>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isSameLengthWithAdapter(target: Array<Boolean>?): Boolean{
        if(target == null) return false

        return adapter.itemCount == target.size
    }

    private fun setViewModelForUpdate(){
        val intent = requireActivity().intent
        if(intent.getStringExtra("fragmentType") == "update_pet_schedule"){
            // Initialize ViewModel for PetIdList
            intent.getStringExtra("petIdList")?.let{
                val petIdListOfString = it.replace(" ", "").split(",")

                // Set item(id) in petIdList true
                for (i in 0 until adapter.itemCount) {
                    if(adapter.getItem(i).id.toString() in petIdListOfString){
                        myPetViewModel.isPetChecked!![i] = true
                    }
                }
                intent.removeExtra("petIdList")
            }
        }
    }


    private fun getCheckedPetIdList(): String{
        var checkedPetIdList = ""

        myPetViewModel.isPetChecked?.let{
            for(i in it.indices){
                if(it[i]){
                    checkedPetIdList += "${adapter.getItem(i).id},"
                }
            }
            checkedPetIdList = checkedPetIdList.dropLast(1)
        }

        return checkedPetIdList
    }
}