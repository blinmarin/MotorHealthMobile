package com.example.bt_def.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.util.Log
import android.view.View
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

//класс ConnectThread для подключения к Bluetooth устройствам и получения данных
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

    //функция для открытия сокета, подключения и получения данных
    override fun run(){
        try{
            Snackbar.make(binding.root, "Подключение...", Snackbar.LENGTH_LONG).show()
            mSocket?.connect()
            Snackbar.make(binding.root, "Подключено", Snackbar.LENGTH_LONG).show()
            readMessage()
            Log.d("MyLog", "Остановка подключения....")

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

    //функция получения данных с bluetooth-устройства
    @SuppressLint("SimpleDateFormat")
    private fun readMessage(){
        val buffer = ByteArray(256)
        while(true){
            try{
                sendMessage("start")
                Log.d("MyLog", "Получение....")
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

            } catch(e: IOException){
                Snackbar.make(binding.root, "При получении данных произошла ошибка", Snackbar.LENGTH_LONG).show()
                val editor = preferences?.edit()
                editor?.putString(BluetoothConstants.MOTOR_ID, null)
                editor?.apply()
                break
            }

        }
        Snackbar.make(binding.root, "Данные сохранены и будут отправлены", Snackbar.LENGTH_LONG).show()
    }

    //функция отправки сообщения для начала обмена данными
    fun sendMessage(message: String){
        try{
            mSocket?.outputStream?.write(message.toByteArray())
        }catch (e: IOException){
            Snackbar.make(binding.root, "При получении данных произошла ошибка", Snackbar.LENGTH_LONG).show()
        }
    }

    //функция закрытия канала передачи данных
    fun closeConnection(){
        try{
            mSocket?.close()
        } catch (e: IOException){

        }
    }
}