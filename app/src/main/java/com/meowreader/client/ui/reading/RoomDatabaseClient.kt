package com.meowreader.client.ui.reading

import android.content.Context
import androidx.room.Room
import com.meowreader.client.data.database.AppDatabase

object RoomDatabaseClient {
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "meowreader-db"
            ).build()
            instance = db
            db
        }
    }
}
