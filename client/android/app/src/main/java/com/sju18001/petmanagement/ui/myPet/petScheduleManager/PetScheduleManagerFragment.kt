package com.sju18001.petmanagement.ui.myPet.petScheduleManager

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.CustomProgressBar
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentPetScheduleManagerBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.PetSchedule
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import com.sju18001.petmanagement.ui.myPet.MyPetViewModel
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PetScheduleManagerFragment : Fragment() {
    private var _binding: FragmentPetScheduleManagerBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val myPetViewModel: MyPetViewModel by activityViewModels()

    // 리싸이클러뷰
    private lateinit var adapter: PetScheduleListAdapter

    private var isViewDestroyed = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetScheduleManagerBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        initializeAdapter()

        // 추가 버튼
        binding.addPetScheduleFab.setOnClickListener{
            val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
            myPetActivityIntent.putExtra("fragmentType", "create_pet_schedule")
            startActivity(myPetActivityIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        // 첫 Fetch가 끝나기 전까지 ProgressBar 표시
        CustomProgressBar.addProgressBar(requireContext(), binding.fragmentPetScheduleManagerParentLayout, 80, R.color.white)

        updateAdapterDataSetByFetchPetSchedule()
    }

    override fun onDestroy() {
        super.onDestroy()

        isViewDestroyed = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeAdapter(){
        adapter = PetScheduleListAdapter(arrayListOf(), myPetViewModel.petNameForId)
        adapter.petScheduleListAdapterInterface = object: PetScheduleListAdapterInterface {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun startCreateUpdatePetScheduleFragmentForUpdate(data: PetSchedule) {
                val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
                myPetActivityIntent
                    .putExtra("fragmentType", "update_pet_schedule")
                    .putExtra("id", data.id)
                    .putExtra("petIdList", data.petIdList)
                    .putExtra("time", data.time)
                    .putExtra("originalTime", data.time)
                    .putExtra("memo", data.memo)
                    .putExtra("enabled", data.enabled)
                startActivity(myPetActivityIntent)
                requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }

            override fun askForDeleteItem(position: Int, item: PetSchedule) {
                val builder = AlertDialog.Builder(activity)
                builder.setMessage("일정을 삭제하시겠습니까?")
                    .setPositiveButton(
                        R.string.confirm, DialogInterface.OnClickListener { _, _ ->
                            PetScheduleNotification.cancelNotificationWorkManager(requireContext(), item.time)
                            deletePetSchedule(item.id)

                            adapter.removeItem(position)
                            adapter.notifyItemRemoved(position)
                            adapter.notifyItemRangeChanged(position, adapter.itemCount)
                        }
                    )
                    .setNegativeButton(
                        R.string.cancel, DialogInterface.OnClickListener { dialog, _ ->
                            dialog.cancel()
                        }
                    )
                    .create()
                    .show()
            }

            override fun deletePetSchedule(id: Long) {
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                    .deletePetScheduleReq(DeletePetScheduleReqDto(id))
                ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {}, {}, {})
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun updatePetSchedule(data: PetSchedule){
                val updatePetScheduleReqDto = UpdatePetScheduleReqDto(
                    data.id, data.petIdList, data.time, data.memo, data.enabled
                )
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                    .updatePetScheduleReq(updatePetScheduleReqDto)
                ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {}, {}, {})
            }

            override fun getContext(): Context {
                return requireContext()
            }
        }

        binding.petScheduleListRecyclerView.adapter = adapter
        binding.petScheduleListRecyclerView.layoutManager = LinearLayoutManager(activity)

        // set adapter item change observer
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                setEmptyNotificationView(adapter.itemCount)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)

                setEmptyNotificationView(adapter.itemCount)
            }
        })
    }

    private fun updateAdapterDataSetByFetchPetSchedule(){
        val dataSet = arrayListOf<PetSchedule>()

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPetScheduleReq(ServerUtil.getEmptyBody())
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            // dataSet에 값 저장
            response.body()?.petScheduleList?.map{
                dataSet.add(PetSchedule(
                    it.id, it.username, it.petList, it.time, it.memo, it.enabled,
                    it.petIdList.replace(" ", "")
                ))
            }
            dataSet.sortBy{ it.time }

            adapter.setDataSet(dataSet)
            adapter.notifyDataSetChanged()

            CustomProgressBar.removeProgressBar(binding.fragmentPetScheduleManagerParentLayout)
        },{ CustomProgressBar.removeProgressBar(binding.fragmentPetScheduleManagerParentLayout) },
            { CustomProgressBar.removeProgressBar(binding.fragmentPetScheduleManagerParentLayout) })
    }

    private fun setEmptyNotificationView(itemCount: Int) {
        // set notification view
        val visibility = if(itemCount != 0) View.GONE else View.VISIBLE
        binding.emptyPetScheduleListNotification.visibility = visibility
    }
}