package com.example.bt_def.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.bt_def.BluetoothConstants
import com.example.bt_def.databinding.ListItemBinding
import com.example.bt_def.db.myDbManager
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class ConnectThread(device: BluetoothDevice, b: ListItemBinding, context: Context) : Thread(){
    private var preferences: SharedPreferences? = null
    private val uuid = "00001101-0000-1000-8000-00805F9B34FB"
    private var mSocket: BluetoothSocket? = null
    private var binding = b
    private val dbManager = myDbManager(context)
    private val myContext = context


    init {
        try{
            mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
        } catch (e: IOException){

        } catch (se: SecurityException) {

        }
    }
    override fun run(){
        try{

//            preferences = myContext.getSharedPreferences(BluetoothConstants.MOTOR_ID, Context.MODE_PRIVATE)
//            dbManager.openDb()
//            val id = preferences?.getString(BluetoothConstants.MOTOR_ID, "")
//            val editor = preferences?.edit()
//            editor?.putString(BluetoothConstants.MOTOR_ID, null)
//            editor?.apply()
//            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
//            val currentDate = sdf.format(Date())
//            if (id != null) {
//                dbManager.insertToDb(id, "Здесь была Марина", currentDate)
//            }
//            dbManager.closeDb()


            Snackbar.make(binding.root, "Подключение...", Snackbar.LENGTH_LONG).show()
            mSocket?.connect()
            Snackbar.make(binding.root, "Подключено", Snackbar.LENGTH_LONG).show()
            readMessage()
        } catch (e: IOException){
            Snackbar.make(binding.root, "Ошибка подключения", Snackbar.LENGTH_LONG).show()

            val editor = preferences?.edit()
            editor?.putString(BluetoothConstants.MOTOR_ID, null)
            editor?.apply()
        } catch (se: SecurityException) {
            Snackbar.make(binding.root, "Нет доступа", Snackbar.LENGTH_LONG).show()

            val editor = preferences?.edit()
            editor?.putString(BluetoothConstants.MOTOR_ID, null)
            editor?.apply()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun readMessage(){
        val buffer = ByteArray(256)
        while(true){
            try{
                val length = mSocket?.inputStream?.read(buffer)
                val message = String(buffer, 0, length ?: 0)
                Log.d("MyLog", "Получено: ${message}")

                preferences = myContext.getSharedPreferences(BluetoothConstants.MOTOR_ID, Context.MODE_PRIVATE)
                dbManager.openDb()
                val id = preferences?.getString(BluetoothConstants.MOTOR_ID, "")
                val editor = preferences?.edit()
                editor?.putString(BluetoothConstants.MOTOR_ID, null)
                editor?.apply()
                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                val currentDate = sdf.format(Date())
                if (id != null) {
                    dbManager.insertToDb(id, message, currentDate)
                }
                dbManager.closeDb()

                Snackbar.make(binding.root, "Данные сохранены и будут отправлены", Snackbar.LENGTH_LONG).show()
            } catch(e: IOException){
                Snackbar.make(binding.root, "Ошибка подключения", Snackbar.LENGTH_LONG).show()
                val editor = preferences?.edit()
                editor?.putString(BluetoothConstants.MOTOR_ID, null)
                editor?.apply()
                break
            }

        }
    }

//    fun sendMessage(message: String){
//        try{
//            mSocket?.outputStream?.write(message.toByteArray())
//        }catch (e: IOException){
//            listener.onReceive(BluetoothController.BLUETOOTH_NO_CONNECTED)
//        }
//    }

//    fun closeConnection(){
//        try{
//            mSocket?.close()
//        } catch (e: IOException){
//
//        }
//    }
}