package com.sju18001.petmanagement.ui.myPet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sju18001.petmanagement.R

import com.sju18001.petmanagement.databinding.FragmentMyPetBinding

class MyPetFragment : Fragment() {

    private lateinit var myPetViewModel: MyPetViewModel
    private var _binding: FragmentMyPetBinding? = null

    // create view variables
    private lateinit var myPetListRecyclerView: RecyclerView
    private lateinit var addPetFab: FloatingActionButton

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myPetViewModel =
            ViewModelProvider(this).get(MyPetViewModel::class.java)

        _binding = FragmentMyPetBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // initialize view variables
        myPetListRecyclerView = root.findViewById(R.id.my_pet_list_recyclerView)
        addPetFab = root.findViewById(R.id.add_pet_fab)

        // add pet fab -> start activity and set fragment to add pet
        addPetFab.setOnClickListener {
            val myPetActivityIntent = Intent(context, MyPetActivity::class.java)
            myPetActivityIntent.putExtra("fragmentType", "add_pet")
            startActivity(myPetActivityIntent)
        }


        //
        //for testing #######################################################################
        val adapter = MyPetListAdapterTemporary()
        myPetListRecyclerView.adapter = adapter
        myPetListRecyclerView.layoutManager = LinearLayoutManager(activity)

        val petListTemporary: ArrayList<MyPetListItemTemporary> = ArrayList()
        val item1 = MyPetListItemTemporary()
        val item2 = MyPetListItemTemporary()
        val item3 = MyPetListItemTemporary()

        item1.MyPetListItemTemporary(0, "한울", "2013년", "강아지", "시추", false, R.drawable.test)
        petListTemporary.add(item1)
        item2.MyPetListItemTemporary(1, "초코", "2021년 1월 1일", "강아지", "시추", true, R.drawable.test2)
        petListTemporary.add(item2)
        item3.MyPetListItemTemporary(2, "똘이", "2020년 12월 31일", "강아지", "푸들", true, R.drawable.test3)
        petListTemporary.add(item3)

        adapter.setResult(petListTemporary)
        //for testing #######################################################################
        //



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}