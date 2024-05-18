package com.example.bt_def

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bt_def.bluetooth.BluetoothController
import com.example.bt_def.databinding.ListItemBinding
import com.example.bt_def.db.myDbManager
import com.google.android.material.snackbar.Snackbar

class ItemAdapter( val adapterType: Boolean, var bluetoothController: BluetoothController, var context: Context, var lifecycleOwner: LifecycleOwner) : ListAdapter<ListItem, ItemAdapter.MyHolder>(Comparator()) {
    class MyHolder(
        view: View,
        val adapterType: Boolean,
        val bluetoothController: BluetoothController,
        val context: Context,
        val lifecycleOwner: LifecycleOwner
    ) : RecyclerView.ViewHolder(view){
        private val b = ListItemBinding.bind(view)
        private var item1: ListItem? = null

        val timer = object: CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {b.itemSearch.visibility = View.VISIBLE}
            override fun onFinish() {b.itemSearch.visibility = View.GONE}
        }

        init {
            itemView.setOnClickListener{
                timer.start()
                if (adapterType) {
                    try {
                        item1?.device?.createBond()
                    } catch (e:SecurityException){
                        Snackbar.make(b.root, "Не удалось произвести сопряжение", Snackbar.LENGTH_LONG).show()
                    }

                } else {
                    bluetoothController.connect(item1?.device?.address ?: "", b, context)

                    val networkConnection = NetworkConnectionBT(context)
                    networkConnection.observe(lifecycleOwner){
                        if(it){
                            //////////////////попробовать отправить, если получилось очистить
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