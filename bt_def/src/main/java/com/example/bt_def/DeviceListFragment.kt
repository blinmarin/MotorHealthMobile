package com.example.bt_def

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bt_def.bluetooth.BluetoothController
import com.example.bt_def.databinding.FragmentListBinding
import com.example.bt_def.db.myDbManager
import com.google.android.material.snackbar.Snackbar

class DeviceListFragment : Fragment() {
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var discoveryAdapter: ItemAdapter
    private lateinit var bAdapter: BluetoothAdapter
    private lateinit var binding: FragmentListBinding
    private lateinit var btLauncher: ActivityResultLauncher<Intent>
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var bluetoothController: BluetoothController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun isLocationEnabled(): Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun checkLocation(){
        if(isLocationEnabled()){
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener{
                override fun onClickButton() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imBluetoothOn.setOnClickListener{
            btLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
        binding.imBluetoothSearch.setOnClickListener{
            if (!isLocationEnabled()){
                checkLocation()
            }else{
                try{
                    if(bAdapter.isEnabled == true){
                        bAdapter.startDiscovery()
                        it.visibility = View.GONE
                        binding.pbSearch.visibility = View.VISIBLE
                    }
                } catch (e: SecurityException){}
            }

        }
        intentFilters()
        checkPermissions()
        initRcViews()
        registerBtLauncher()
        initBtAdapter()
        bluetoothState()

    }

    private fun initRcViews() = with(binding){
        rcViewPaired.layoutManager = LinearLayoutManager(requireContext())
        rcViewSearch.layoutManager = LinearLayoutManager(requireContext())
        initBtAdapter()
        bluetoothController = BluetoothController(bAdapter)
        val context = requireContext()
        itemAdapter = ItemAdapter( false, bluetoothController, context, viewLifecycleOwner)
        discoveryAdapter = ItemAdapter( true, bluetoothController, context, viewLifecycleOwner)
        rcViewPaired.adapter = itemAdapter
        rcViewSearch.adapter = discoveryAdapter
    }

    private fun getPairedDevices(){
        try{
            val list = ArrayList<ListItem>()
            val deviceList = bAdapter.bondedDevices as Set<BluetoothDevice>
            deviceList.forEach{
                list.add(
                    ListItem(
                        it
                    )
                )
            }
            itemAdapter.submitList(list)
        } catch (e: SecurityException){
        }

    }

    private fun initBtAdapter(){
        val bManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bAdapter = bManager.adapter
    }

    private fun bluetoothState(){
        if (bAdapter.isEnabled == true){
            changeButtonColor(binding.imBluetoothOn, Color.GREEN)
            getPairedDevices()
        }

    }

    private fun registerBtLauncher(){
        btLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            if (it.resultCode == Activity.RESULT_OK){
                changeButtonColor(binding.imBluetoothOn, Color.GREEN)
                getPairedDevices()
                Snackbar.make(binding.root, "Блютуз включен", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(binding.root, "Блютуз выключен", Snackbar.LENGTH_LONG).show()
                changeButtonColor(binding.imBluetoothOn, Color.GRAY)
            }
        }
    }

    private fun  checkPermissions(){
        if(!checkBtPermissions()){
            registerPermissionListener()
            launchBtPermissions()
        }
    }

    private fun launchBtPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            pLauncher.launch( arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN)
            )
        } else{
            pLauncher.launch( arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }

    private fun registerPermissionListener(){
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){

        }
    }


    private val bReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            if(intent?.action == BluetoothDevice.ACTION_FOUND){
                val device = if(Build.VERSION.SDK_INT >= 33){
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE,BluetoothDevice::class.java)
                }else{
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }

                val list = mutableSetOf<ListItem>()
                list.addAll(discoveryAdapter.currentList)
                if (device != null) list.addAll(
                    listOf(ListItem(device)))
                discoveryAdapter.submitList(list.toList())
                try {
                    Log.d("MyLog", "Device: ${device?.name}")
                } catch (e: SecurityException){

                }

            } else if(intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED){
                getPairedDevices()
            } else if(intent?.action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED){
                binding.imBluetoothSearch.visibility = View.VISIBLE
                binding.pbSearch.visibility = View.GONE
            }
        }

    }

    private fun intentFilters(){
        val f1 = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val f2 = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        val f3 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        activity?.registerReceiver(bReceiver, f1)
        activity?.registerReceiver(bReceiver, f2)
        activity?.registerReceiver(bReceiver, f3)
    }

}