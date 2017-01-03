package io.quartic.app.sensors

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) {
    class Helper(context: Context) :
            SQLiteOpenHelper(context, Helper.DATABASE_NAME, null, Helper.DATABASE_VERSION) {
        companion object {
            private val DATABASE_NAME = "tracker"
            private val DATABASE_VERSION = 1
            private val CREATE = """
                CREATE TABLE
                    sensors (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name VARCHAR NOT NULL,
                        value VARCHAR NOT NULL,
                        timestamp INT NOT NULL,
                        uploaded INT
                    )
            """
        }

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // Nah
        }
    }

    val helper = Helper(context)

    public fun writeSensor(name: String, value: String, timestamp: Long) {
        helper.writableDatabase.use { db ->
            db.beginTransaction()
            val contentValues = ContentValues()

            with (contentValues) {
                put("name", name)
                put("value", value)
                put("timestamp", timestamp)
            }
            db.insertOrThrow("sensors", null, contentValues)
            db.endTransaction()
        }
    }


}
