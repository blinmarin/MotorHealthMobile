package com.example.bt_def.db

import android.bluetooth.BluetoothDevice
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.lang.reflect.Constructor

class DataItem {
    var id: String = ""
    var info: String = ""
    var time: String = ""

    constructor(id: String, info: String, time: String){
        this.id = id
        this.info = info
        this.time = time
    }
}
class myDbManager(context: Context) {
    val myDbHelper = MyDbHelper(context)
    var db: SQLiteDatabase? = null

    fun openDb() {
        db = myDbHelper.writableDatabase
    }

    fun insertToDb(id: String, info: String, time: String) {
        val values = ContentValues().apply {
            put(myDbNameClass.COLUMN_NAME_MOTOR_ID, id)
            put(myDbNameClass.COLUMN_NAME_INFO, info)
            put(myDbNameClass.COLUMN_NAME_TIME, time)
        }

        db?.insert(myDbNameClass.TABLE_NAME, null, values)

    }



    fun readDbData(): MutableList<DataItem> {
//        db?.execSQL(myDbNameClass.DROP_TABLE)
        var resultList : MutableList<DataItem> = ArrayList()
        val cursor = db?.query(myDbNameClass.TABLE_NAME, null, null, null, null, null, null)

        while (cursor?.moveToNext()!!) {
            val dataMotorId =
                cursor.getString(cursor.getColumnIndexOrThrow(myDbNameClass.COLUMN_NAME_MOTOR_ID))
            val dataMotorInfo =
                cursor.getString(cursor.getColumnIndexOrThrow(myDbNameClass.COLUMN_NAME_INFO))
            val dataMotorTime =
                cursor.getString(cursor.getColumnIndexOrThrow(myDbNameClass.COLUMN_NAME_TIME))
            val item = DataItem(dataMotorId, dataMotorInfo, dataMotorTime)

            resultList.add(item)
        }
        cursor.close()
        return resultList
    }

    fun closeDb() {
        myDbHelper.close()
    }

}