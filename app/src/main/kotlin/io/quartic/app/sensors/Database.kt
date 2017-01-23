package io.quartic.app.sensors

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.quartic.tracker.api.SensorValue
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class Database(context: Context) {
    val lock = ReentrantReadWriteLock()

    companion object {
        var INSTANCE: Database? = null

        fun getInstance(context: Context): Database {
            if (INSTANCE == null) {
                INSTANCE = Database(context)
            }
            return INSTANCE!!
        }
    }

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

    fun processSensorData(batchSize: Int, f: (List<SensorValue>) -> Unit) {
        lock.write {
            helper.writableDatabase.use { db ->
                while (true) {
                    db.beginTransaction()
                    val sensorValues = arrayListOf<SensorValue>()
                    db.query("sensors", arrayOf("id", "name", "value", "timestamp"), null, null, null, null, null, "$batchSize")
                            .use { cursor ->
                                cursor.moveToFirst()
                                while (!cursor.isAfterLast) {
                                    sensorValues.add(SensorValue(
                                            cursor.getInt(0),
                                            cursor.getString(1),
                                            cursor.getString(2),
                                            cursor.getLong(3)
                                    ))
                                    cursor.moveToNext()
                                }
                            }

                    if (sensorValues.isEmpty()) {
                        break
                    }

                    try {
                        f.invoke(sensorValues)
                        sensorValues.forEach { db.delete("sensors", "id = ?", arrayOf("${it.id}")) }
                    } catch (e: Exception) {
                        if (db.inTransaction()) {
                            db.endTransaction()
                            return
                        }
                    }
                    db.setTransactionSuccessful()
                    db.endTransaction()
                }
            }
        }
    }

    fun writeSensor(name: String, value: String, timestamp: Long) {
        lock.write {
            helper.writableDatabase.use { db ->
                db.beginTransaction()
                val contentValues = ContentValues()

                with (contentValues) {
                    put("name", name)
                    put("value", value)
                    put("timestamp", timestamp)
                }
                db.insertOrThrow("sensors", null, contentValues)
                db.setTransactionSuccessful()
                db.endTransaction()
            }
        }
    }
}
