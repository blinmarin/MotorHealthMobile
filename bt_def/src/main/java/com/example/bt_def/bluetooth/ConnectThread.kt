package com.example.bt_def.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.bt_def.databinding.ListItemBinding
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.util.UUID

class ConnectThread(device: BluetoothDevice, b: ListItemBinding) : Thread() {
    private val uuid = "00001101-0000-1000-8000-00805F9B34FB"
    private var mSocket: BluetoothSocket? = null
    private var binding = b
    init {
        try{
            mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
        } catch (e: IOException){

        } catch (se: SecurityException) {

        }
    }
    override fun run(){
        try{
            Snackbar.make(binding.root, "Подключение...", Snackbar.LENGTH_LONG).show()
            mSocket?.connect()
            Snackbar.make(binding.root, "Подключено", Snackbar.LENGTH_LONG).show()
            readMessage()
        } catch (e: IOException){
            Snackbar.make(binding.root, "Ошибка подключения", Snackbar.LENGTH_LONG).show()
        } catch (se: SecurityException) {
            Snackbar.make(binding.root, "Нет доступа", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun readMessage(){
        val buffer = ByteArray(256)
        while(true){
            try{
                val length = mSocket?.inputStream?.read(buffer)
                val message = String(buffer, 0, length ?: 0)
                Log.d("MyLog", "Получено: ${message}")
                Snackbar.make(binding.root, "Получено ${message}", Snackbar.LENGTH_LONG).show()
                Snackbar.make(binding.root, "Данные сохранены и будут отправлены", Snackbar.LENGTH_LONG).show()
            } catch(e: IOException){
                Snackbar.make(binding.root, "Ошибка подключения", Snackbar.LENGTH_LONG).show()
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