package com.sju18001.petmanagement.ui.myPet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}