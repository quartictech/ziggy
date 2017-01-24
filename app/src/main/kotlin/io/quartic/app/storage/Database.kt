package io.quartic.app.storage

import android.content.ContentValues
import android.content.Context
import io.quartic.app.storage.SensorContentProvider.Companion.CONTENT_URI
import io.quartic.tracker.api.SensorValue

class Database(val context: Context) {
    fun writeSensor(name: String, value: String, timestamp: Long) {
        val contentValues = ContentValues()
        with(contentValues) {
            put("name", name)
            put("value", value)
            put("timestamp", timestamp)
        }
        context.contentResolver.insert(CONTENT_URI, contentValues)
    }

    fun getSensorValues(): List<SensorValue> {
        val sensorValues = arrayListOf<SensorValue>()
        context.contentResolver.query(CONTENT_URI, null, null, null, null)
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
        return sensorValues
    }

    fun delete(ids: List<Int>) {
        ids.forEach { id -> context.contentResolver.delete(
                CONTENT_URI.buildUpon().appendPath(id.toString()).build(), null, null)
        }
    }

    fun  getBacklogSize(): Int {
        val cursor = context.contentResolver.query(CONTENT_URI.buildUpon().appendPath("count").build(), null, null, null, null)
        cursor.moveToFirst()
        return cursor.getInt(0)
    }
}
