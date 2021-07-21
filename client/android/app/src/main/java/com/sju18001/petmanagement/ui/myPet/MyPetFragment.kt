package com.sju18001.petmanagement.ui.myPet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.R

import com.sju18001.petmanagement.databinding.FragmentMyPetBinding

class MyPetFragment : Fragment() {

    private lateinit var myPetViewModel: MyPetViewModel

    // variables for view binding
    private var _binding: FragmentMyPetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myPetViewModel =
            ViewModelProvider(this).get(MyPetViewModel::class.java)

        // view binding
        _binding = FragmentMyPetBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // add pet fab -> start activity and set fragment to add pet
        binding.addPetFab.setOnClickListener {
            val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
            myPetActivityIntent.putExtra("fragmentType", "add_pet")
            startActivity(myPetActivityIntent)
        }


        //
        //for testing #######################################################################
        val adapter = MyPetListAdapter()
        binding.myPetListRecyclerView.adapter = adapter
        binding.myPetListRecyclerView.layoutManager = LinearLayoutManager(activity)

        val petList: ArrayList<MyPetListItem> = ArrayList()
        val item1 = MyPetListItem()
        val item2 = MyPetListItem()
        val item3 = MyPetListItem()

        item1.setValues(0, "둥이", "2013년", "강아지", "불도그", false, R.drawable.sample1)
        petList.add(item1)
        item2.setValues(1, "초코", "2021년 1월 1일", "강아지", "시추", true, R.drawable.sample2)
        petList.add(item2)
        item3.setValues(2, "똘이", "2020년 12월 31일", "강아지", "푸들", true, R.drawable.sample3)
        petList.add(item3)

        adapter.setResult(petList)
        //for testing #######################################################################
        //


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}