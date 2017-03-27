package io.quartic.app.storage

import android.content.ContentValues
import android.content.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.quartic.app.storage.SensorContentProvider.Companion.CONTENT_URI
import io.quartic.tracker.api.SensorReading
import io.quartic.tracker.api.SensorValue

class Database(val context: Context) {
    companion object {
        val OBJECT_MAPPER = jacksonObjectMapper()
    }

    fun writeSensor(name: String, value: SensorValue, timestamp: Long) {
        val contentValues = ContentValues()
        with(contentValues) {
            put("name", name)
            put("value", OBJECT_MAPPER.writeValueAsString(value))
            put("timestamp", timestamp)
        }
        context.contentResolver.insert(CONTENT_URI, contentValues)
    }

    val sensorValues: List<SensorReading>
        get() {
            val sensorValues = arrayListOf<SensorReading>()
            context.contentResolver.query(CONTENT_URI, null, null, null, null)
                    .use { cursor ->
                        cursor.moveToFirst()
                        while (!cursor.isAfterLast) {
                            sensorValues.add(SensorReading(
                                    cursor.getInt(0),
                                    cursor.getString(1),
                                    OBJECT_MAPPER.readValue(cursor.getString(2), SensorValue::class.java),
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

    val backlogSize: Int
        get() = context.contentResolver.query(CONTENT_URI.buildUpon().appendPath("count").build(), null, null, null, null)
                    .use { cursor ->
                        cursor.moveToFirst()
                        return cursor.getInt(0)
                    }
}
