package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

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
import com.sju18001.petmanagement.databinding.FragmentEditPetFeedScheduleBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalTime

class EditPetFeedScheduleFragment : Fragment() {
    private var _binding: FragmentEditPetFeedScheduleBinding? = null
    private val binding get() = _binding!!

    private val myPetViewModel: MyPetViewModel by activityViewModels()

    // variable for storing API call(for cancel)
    private var petProfileFetchApiCall: Call<List<PetProfileFetchResponseDto>>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    // 리싸이클러뷰
    private lateinit var adapter: PetNameListAdapter

    // API Calls
    private var petFeedScheduleCreateApiCall: Call<PetFeedScheduleCreateResponseDto>? = null
    private var petFeedScheduleUpdateApiCall: Call<PetFeedScheduleUpdateResponseDto>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPetFeedScheduleBinding.inflate(inflater, container, false)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        // for update_pet_feed_schedule
        setViewForUpdate()

        // 상태 로드
        loadState(myPetViewModel)

        // Timepicker
        binding.feedTimePicker.setOnTimeChangedListener { _, hour, minute ->
            myPetViewModel.feedTimeHour = hour
            myPetViewModel.feedTimeMinute = minute
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
            // TODO: pet_id 리스트화하여 전달
            val intent = requireActivity().intent

            if(intent.getStringExtra("fragmentType") == "create_pet_feed_schedule"){
                createPetFeedSchedule()
            }else{
                updatePetFeedSchedule(intent.getLongExtra("id", 0), intent.getBooleanExtra("isTurnedOn", false))
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

        petFeedScheduleCreateApiCall?.cancel()
        petFeedScheduleUpdateApiCall?.cancel()
        petProfileFetchApiCall?.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setViewForUpdate(){
        // 스케줄 데이터 불러오기
        val intent = requireActivity().intent

        if(intent.getStringExtra("fragmentType") == "update_pet_feed_schedule"){
            binding.feedTimePicker.hour = intent.getIntExtra("hour", 0)
            binding.feedTimePicker.minute = intent.getIntExtra("minute", 0)
            binding.memoEditText.setText(intent.getStringExtra("memo"))
        }

        // 액션 타이틀
        binding.actionTitle.text = "일정 편집"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun loadState(myPetViewModel: MyPetViewModel){
        // Timepicker
        binding.feedTimePicker.hour = myPetViewModel.feedTimeHour?.let{ it }
        binding.feedTimePicker.minute = myPetViewModel.feedTimeMinute?.let{ it }

        // Memo
        binding.memoEditText.setText(myPetViewModel.memo?.let{ it })
    }

    private fun addPetNameList(){
        petProfileFetchApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).petProfileFetchRequest()
        petProfileFetchApiCall!!.enqueue(object: Callback<List<PetProfileFetchResponseDto>> {
            override fun onResponse(
                call: Call<List<PetProfileFetchResponseDto>>,
                response: Response<List<PetProfileFetchResponseDto>>
            ) {
                response.body()?.map {
                    adapter.addItem(PetNameListItem(it.name, it.id))
                }
                adapter.notifyDataSetChanged()

                if(!isSameLengthWithAdapter(myPetViewModel.isPetChecked)){
                    myPetViewModel.isPetChecked = Array(adapter.itemCount){ false }
                }
            }

            override fun onFailure(call: Call<List<PetProfileFetchResponseDto>>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPetFeedSchedule(){
        val petFeedScheduleCreateRequestDto = PetFeedScheduleCreateRequestDto(
            getCheckedPetIdList(), LocalTime.of(binding.feedTimePicker.hour, binding.feedTimePicker.minute).toString(), binding.memoEditText.text.toString()
        )

        petFeedScheduleCreateApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .petFeedScheduleCreateRequest(petFeedScheduleCreateRequestDto)
        petFeedScheduleCreateApiCall!!.enqueue(object: Callback<PetFeedScheduleCreateResponseDto> {
            override fun onResponse(
                call: Call<PetFeedScheduleCreateResponseDto>,
                response: Response<PetFeedScheduleCreateResponseDto>
            ) {
                if(response.isSuccessful){
                    activity?.finish()
                }else{
                    Toast.makeText(context, "데이터 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PetFeedScheduleCreateResponseDto>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePetFeedSchedule(id: Long, isTurnedOn: Boolean){
        val petFeedScheduleUpdateRequestDto = PetFeedScheduleUpdateRequestDto(
            id, getCheckedPetIdList(), LocalTime.of(binding.feedTimePicker.hour, binding.feedTimePicker.minute).toString(), binding.memoEditText.text.toString(), isTurnedOn
        )

        petFeedScheduleUpdateApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .petFeedScheduleUpdateRequest(petFeedScheduleUpdateRequestDto)
        petFeedScheduleUpdateApiCall!!.enqueue(object: Callback<PetFeedScheduleUpdateResponseDto> {
            override fun onResponse(
                call: Call<PetFeedScheduleUpdateResponseDto>,
                response: Response<PetFeedScheduleUpdateResponseDto>
            ) {
                if(response.isSuccessful){
                    activity?.finish()
                }else{
                    Toast.makeText(context, "데이터 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PetFeedScheduleUpdateResponseDto>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isSameLengthWithAdapter(target: Array<Boolean>?): Boolean{
        if(target == null) return false

        return adapter.itemCount == target.size
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