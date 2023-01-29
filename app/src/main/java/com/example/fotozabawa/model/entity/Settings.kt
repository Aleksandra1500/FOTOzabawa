package com.example.fotozabawa.model.entity

import android.text.Editable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey(autoGenerate=true)
    val id:Int,
    val seconds: Int,
    val photoNumber:Int,
    val theme:Int,
)
