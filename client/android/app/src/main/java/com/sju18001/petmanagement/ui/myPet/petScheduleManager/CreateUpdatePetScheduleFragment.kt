package com.sju18001.petmanagement.ui.myPet.petScheduleManager

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCreateUpdatePetScheduleBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalTime

class CreateUpdatePetScheduleFragment : Fragment() {
    private var _binding: FragmentCreateUpdatePetScheduleBinding? = null
    private val binding get() = _binding!!

    private val myPetViewModel: MyPetViewModel by activityViewModels()

    // 리싸이클러뷰
    private lateinit var adapter: PetNameListAdapter

    private var isViewDestroyed = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateUpdatePetScheduleBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        // for update_pet_schedule
        setViewForUpdate()

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

        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentCreateUpdatePetScheduleParentLayout)

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        isViewDestroyed = true
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
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPetReq(FetchPetReqDto( null , null))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
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
        }, {}, {})
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPetSchedule(){
        lockViews()

        val createPetScheduleReqDto = CreatePetScheduleReqDto(
            getCheckedPetIdList(), LocalTime.of(binding.timePicker.hour, binding.timePicker.minute).toString(), binding.memoEditText.text.toString()
        )
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .createPetScheduleReq(createPetScheduleReqDto)
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            activity?.finish()
        }, {
            unlockViews()
        }, {
            unlockViews()
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePetSchedule(id: Long, enabled: Boolean){
        lockViews()

        val updatePetScheduleReqDto = UpdatePetScheduleReqDto(
            id, getCheckedPetIdList(), LocalTime.of(binding.timePicker.hour, binding.timePicker.minute).toString(), binding.memoEditText.text.toString(), enabled
        )
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .updatePetScheduleReq(updatePetScheduleReqDto)
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            activity?.finish()
        }, {
            unlockViews()
        }, {
            unlockViews()
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

    private fun lockViews() {
        binding.cancelButton.isEnabled = false
        binding.confirmButton.isEnabled = false
        binding.timePicker.isEnabled = false
        binding.petNameListRecyclerView.isEnabled = false
        binding.petNameListRecyclerView.let{
            for(i in 0..adapter.itemCount) {
                it.findViewHolderForLayoutPosition(i)?.itemView?.findViewById<CheckBox>(R.id.pet_name_check_box)?.isEnabled = false
                it.findViewHolderForLayoutPosition(i)?.itemView?.findViewById<CheckBox>(R.id.pet_name_check_box)?.buttonTintList = context?.resources?.getColorStateList(R.color.gray)
            }
        }
        binding.memoEditText.isEnabled = false
        binding.backButton.isEnabled = false
    }

    private fun unlockViews() {
        binding.cancelButton.isEnabled = true
        binding.confirmButton.isEnabled = true
        binding.timePicker.isEnabled = true
        binding.petNameListRecyclerView.isEnabled = true
        binding.petNameListRecyclerView.let{
            for(i in 0..adapter.itemCount) {
                it.findViewHolderForLayoutPosition(i)?.itemView?.findViewById<CheckBox>(R.id.pet_name_check_box)?.isEnabled = true
                it.findViewHolderForLayoutPosition(i)?.itemView?.findViewById<CheckBox>(R.id.pet_name_check_box)?.buttonTintList = context?.resources?.getColorStateList(R.color.carrot)
            }
        }
        binding.memoEditText.isEnabled = true
        binding.backButton.isEnabled = true
    }

}