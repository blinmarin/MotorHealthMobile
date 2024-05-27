package com.example.bt_def.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.bt_def.databinding.ListItemBinding
import com.example.bt_def.db.myDbManager

class BluetoothController(private val adapter: BluetoothAdapter) {
    private var connectThread: ConnectThread? = null

    fun connect(mac: String, binding: ListItemBinding, context: Context){
        if (adapter.isEnabled && mac.isNotEmpty()){
            val device = adapter.getRemoteDevice(mac)
            connectThread = ConnectThread(device, binding, context)
            connectThread?.start()
        }
    }
    fun sendMessage(message: String){
        connectThread?.sendMessage(message)
    }
    fun closeConnection(){
        connectThread?.closeConnection()
    }

}