package com.example.motorhealth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bt_def.db.myDbManager
import com.example.motorhealth.databinding.FragmentDbBinding
import com.example.motorhealth.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar


class DbFragment : Fragment() {

    private lateinit var binding: FragmentDbBinding
    private lateinit var myDbManager: myDbManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDbBinding.inflate(inflater, container, false)
        myDbManager = myDbManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myDbManager.openDb()
        val result = myDbManager.readDbData()
        var text = ""
        for (item in result) {
            var item_text = " id: " + item.id + " | info: " + item.info +  " | time: " + item.time + "____________"
            text += item_text
        }
        binding.textDb.text = text

        if (result.size == 0){
            binding.imageError.visibility = View.GONE
            binding.textError.visibility = View.GONE
            binding.imageOK.visibility = View.VISIBLE
            binding.textOK.visibility = View.VISIBLE

        }else{
            binding.imageError.visibility = View.VISIBLE
            binding.textError.visibility = View.VISIBLE
            binding.imageOK.visibility = View.GONE
            binding.textOK.visibility = View.GONE
        }
        myDbManager.closeDb()


    }

    }