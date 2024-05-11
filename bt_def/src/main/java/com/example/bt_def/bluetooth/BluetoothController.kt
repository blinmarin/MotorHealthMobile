package com.example.bt_def.bluetooth

import android.bluetooth.BluetoothAdapter
import com.example.bt_def.databinding.ListItemBinding

class BluetoothController(private val adapter: BluetoothAdapter) {
    private var connectThread: ConnectThread? = null

    fun connect(mac: String, binding: ListItemBinding){
        if (adapter.isEnabled && mac.isNotEmpty()){
            val device = adapter.getRemoteDevice(mac)
            connectThread = ConnectThread(device, binding)
            connectThread?.start()
        }
    }
//    fun sendMessage(message: String){
//        connectThread?.sendMessage(message)
//    }
//    fun closeConnection(){
//        connectThread?.closeConnection()
//    }

}