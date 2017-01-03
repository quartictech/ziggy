package io.quartic.app.sensors

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) :
        SQLiteOpenHelper(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION) {
    companion object {
        private val DATABASE_NAME = "sensors"
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
