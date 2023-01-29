package com.example.fotozabawa.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fotozabawa.model.DAO.SettingsDAO
import com.example.fotozabawa.model.entity.Settings

@Database(
    entities = [Settings::class],
    version = 1,
    exportSchema = false
)

abstract class SettingsDatabase : RoomDatabase() {

    abstract fun settingsDAO(): SettingsDAO

    companion object {
        @Volatile
        private var INSTANCE: SettingsDatabase? = null

        fun getDatabase(context: Context): SettingsDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SettingsDatabase::class.java,
                    "settings_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}