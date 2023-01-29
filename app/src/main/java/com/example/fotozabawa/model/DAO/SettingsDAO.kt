package com.example.fotozabawa.model.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fotozabawa.model.entity.Settings

@Dao
interface SettingsDAO {
    @Query("SELECT seconds FROM settings")
    fun getSeconds(): Int

    @Query("SELECT photoNumber FROM settings")
    fun getPhotoNumber(): Int

    @Query("SELECT theme FROM settings")
    fun getTheme(): Int

    @Query("DELETE FROM settings")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(settings: Settings)

    @Update
    suspend fun update(settings: Settings)
}