package com.example.bt_def.db

import android.os.DropBoxManager
import android.provider.BaseColumns

object myDbNameClass {
    const val TABLE_NAME = "motors"
    const val COLUMN_NAME_MOTOR_ID = "motor_id"
    const val COLUMN_NAME_INFO = "info"
    const val COLUMN_NAME_TIME = "time"

    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "MotorDb.db"

    const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, $COLUMN_NAME_MOTOR_ID TEXT, $COLUMN_NAME_INFO TEXT, $COLUMN_NAME_TIME TEXT)"

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"

}