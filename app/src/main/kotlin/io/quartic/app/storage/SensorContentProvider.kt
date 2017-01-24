package io.quartic.app.storage

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.content.ContentUris
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write


class SensorContentProvider : ContentProvider() {
    val lock = ReentrantReadWriteLock()

    companion object {
        private const val TAG = "SensorContentProvider"
        private const val PROVIDER_NAME = "io.quartic.app.provider"
        private const val SENSORS = 1
        private const val SENSORS_ID = 2
        private const val SENSORS_COUNT = 3
        val CONTENT_URI: Uri = Uri.parse("content://${PROVIDER_NAME}/sensors")

        private const val BATCH_SIZE = 100
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        uriMatcher.addURI(PROVIDER_NAME, "sensors", SENSORS)
        uriMatcher.addURI(PROVIDER_NAME, "sensors/#", SENSORS_ID)
        uriMatcher.addURI(PROVIDER_NAME, "sensors/count", SENSORS_COUNT)
    }

    private class Helper(context: Context) :
            SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
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

    private var helper: Helper? = null

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        when(uriMatcher.match(uri)) {
            SENSORS -> lock.write {
                val db = helper!!.writableDatabase
                db.beginTransaction()
                val id = db.insertOrThrow("sensors", null, values)
                db.setTransactionSuccessful()
                db.endTransaction()
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }
            else -> throw IllegalArgumentException("not recognised: $uri")
        }
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        when(uriMatcher.match(uri)) {
            SENSORS -> lock.read {
                return helper!!.readableDatabase.query("sensors", arrayOf("id", "name", "value", "timestamp"),
                        null, null, null, null, null, "${BATCH_SIZE}")
            }
            SENSORS_COUNT -> lock.read {
                return helper!!.readableDatabase.rawQuery("select count(*) from sensors", null)
            }

            else -> throw IllegalArgumentException("not recognised: $uri")
        }
    }

    override fun onCreate(): Boolean {
        helper = Helper(context!!)
        return true
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        when(uriMatcher.match(uri)) {
            SENSORS_ID -> lock.write {
                val db = helper!!.writableDatabase
                val id = uri!!.pathSegments[1]
                return db.delete("sensors", "id=?", arrayOf(id))
            }
            else -> throw IllegalArgumentException("not recognised: $uri")
        }
    }

    override fun getType(uri: Uri?): String {
        return ""
    }

}
