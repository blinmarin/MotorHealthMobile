package com.example.bt_def

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.bt_def.bluetooth.BluetoothController
import com.example.bt_def.databinding.ListItemBinding
import com.example.bt_def.db.myDbManager
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

//класс управления отдельным пунктом из списка двигателей
class ItemAdapter( val adapterType: Boolean, var bluetoothController: BluetoothController,
                   var context: Context, var lifecycleOwner: LifecycleOwner) : ListAdapter<ListItem,
        ItemAdapter.MyHolder>(Comparator()) {
    class MyHolder(
        view: View,
        val adapterType: Boolean,
        val bluetoothController: BluetoothController,
        val context: Context,
        val lifecycleOwner: LifecycleOwner
    ) : RecyclerView.ViewHolder(view){
        private val b = ListItemBinding.bind(view)
        private var item1: ListItem? = null
        private var dbManager = myDbManager(context)

        val timer = object: CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {b.itemSearch.visibility = View.VISIBLE}
            override fun onFinish() {b.itemSearch.visibility = View.GONE}
        }

        init {
            itemView.setOnClickListener{
                timer.start()
                // реализация сопряжения Bluetooth устройства
                if (adapterType) {
                    try {
                        item1?.device?.createBond()
                    } catch (e:SecurityException){
                        Snackbar.make(b.root, "Не удалось произвести сопряжение", Snackbar.LENGTH_LONG).show()
                    }

                } else {
                    //подключение к устройству и получение данных
                    bluetoothController.connect(item1?.device?.address ?: "", b, context)

                    //проверка на наличие подключения к сети Интернет
                    val networkConnection = NetworkConnectionBT(context)
                    networkConnection.observe(lifecycleOwner){
                        if(it){
                            //попытка отправки данных из базы на сервер
                            dbManager.openDb()
                            val result = dbManager.readDbData()
                            dbManager.closeDb()
                            for (item in result) {
                                requestData(item.info, item.time)
                            }
                        }
                    }
                }
            }
        }
        fun bind(item: ListItem) = with(b){
            item1 = item
            try{
                name.text = item.device.name
                mac.text = item.device.address
            } catch (e: SecurityException){

            }
        }

        private fun requestData(info: String, time: String){
            val url = "http://10.0.2.2:3000/motor"
            val requestBody: String
            try{
                val jsonBody = JSONObject()
                jsonBody.put("data", "idengine:1234::temp:${info}")
                requestBody = jsonBody.toString()
                val request = object : StringRequest(
                    Method.POST,
                    url,
                    { response ->
                        Log.d("MyLog", "Успех: ${response} deleted ${time} из ItemAdapter")
                        dbManager.openDb()
                        dbManager.deleteDbData(time)
                        dbManager.closeDb()

                    },
                    { error ->
                        Log.d("MyLog", "Ошибка: ${error} из ItemAdapter")
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

    class Comparator : DiffUtil.ItemCallback<ListItem>(){
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)

        return MyHolder(view, adapterType, bluetoothController, context, lifecycleOwner)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.bind(getItem(position))
    }

}