package com.example.bt_def.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//класс для создания базы данных при первом запуске приложения
class MyDbHelper(context: Context) : SQLiteOpenHelper(context, myDbNameClass.DATABASE_NAME,
    null, myDbNameClass.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(myDbNameClass.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(myDbNameClass.DROP_TABLE)
        onCreate(db)
    }
}