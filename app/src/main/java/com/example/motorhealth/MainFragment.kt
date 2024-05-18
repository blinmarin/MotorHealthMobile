package com.example.motorhealth

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.bt_def.BluetoothConstants
import com.example.bt_def.db.myDbManager
import com.example.motorhealth.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment() {
    private var preferences: SharedPreferences? = null
    private lateinit var binding: FragmentMainBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = activity?.getSharedPreferences(BluetoothConstants.MOTOR_ID, Context.MODE_PRIVATE)
        val networkConnection = NetworkConnection(requireContext())
        networkConnection.observe(viewLifecycleOwner){
            if(it){
                //////////////////попробовать отправить, если получилось очистить
            }
        }




        binding.editText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s:CharSequence,start:Int,before:Int, count:Int){
                if (binding.editText.text.toString().length > 0){
                    binding.bList.visibility = View.VISIBLE
                } else {
                    binding.bList.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })


        binding.bList.setOnClickListener {
            val editor = preferences?.edit()
            editor?.putString(BluetoothConstants.MOTOR_ID, binding.editText.text.toString())

//            val id = preferences?.getString(BluetoothConstants.MOTOR_ID, "")
//            Snackbar.make(binding.root, id.toString(), Snackbar.LENGTH_LONG).show()

            editor?.apply()
            findNavController().navigate(R.id.action_mainFragment_to_deviceListFragment)



        }

        binding.buttonDb.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_dbFragment)

        }
    }

}