package com.example.motorhealth

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.bt_def.BluetoothConstants
import com.example.bt_def.db.myDbManager
import com.example.motorhealth.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.Date

class MainFragment : Fragment() {
    private var preferences: SharedPreferences? = null
    private lateinit var binding: FragmentMainBinding
    private lateinit var myDbManager: myDbManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMainBinding.inflate(inflater, container, false)
        myDbManager = myDbManager(requireContext())
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = activity?.getSharedPreferences(BluetoothConstants.MOTOR_ID, Context.MODE_PRIVATE)

        val networkConnection = NetworkConnection(requireContext())
        networkConnection.observe(viewLifecycleOwner){
            if(it){
                myDbManager.openDb()
                val result = myDbManager.readDbData()
//                myDbManager.closeDb()
                for (item in result) {
                    requestData(item.id, item.info, item.time)
                }
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
            editor?.apply()
            findNavController().navigate(R.id.action_mainFragment_to_deviceListFragment)

        }

        binding.buttonDb.setOnClickListener{
//            myDbManager.openDb()
//            val id = preferences?.getString(BluetoothConstants.MOTOR_ID, "")
//            val editor = preferences?.edit()
//            editor?.putString(BluetoothConstants.MOTOR_ID, null)
//            editor?.apply()
//            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
//            val currentDate = sdf.format(Date())
//            if (id != null) {
//                myDbManager.insertToDb(id, "Здесь была Марина", currentDate)
//            }
//            myDbManager.closeDb()
            findNavController().navigate(R.id.action_mainFragment_to_dbFragment)

        }
    }

    private fun requestData(id: String, info: String, time: String){
        val url = "http://10.0.2.2:3000/motor"
        val requestBody: String
        try{
            val jsonBody = JSONObject()
            jsonBody.put("id", id)
            jsonBody.put("info", info)
            jsonBody.put("time", time)
            requestBody = jsonBody.toString()
            val request = object : StringRequest(
                Method.POST,
                url,
                { response ->
                    Log.d("MyLog", "Успех: ${response} deleted ${time}")
                    myDbManager.openDb()
                    myDbManager.deleteDbData(time)
                    myDbManager.closeDb()

                },
                { error ->
                    Log.d("MyLog", "Ошибка: ${error}")
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray? {
                    return try{
                        requestBody.toByteArray(Charsets.UTF_8)
                    }catch(e : UnsupportedEncodingException){
                        null
                    }
                }
            }
            val queue = Volley.newRequestQueue(context)
            queue.add(request)

        } catch (e: JSONException){

        }

    }

}