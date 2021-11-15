package com.example.timerapp.database

import androidx.collection.arraySetOf
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// idはnullの時に自動生成
@Entity(primaryKeys = ["name", "presetName"])
data class PresetTimer (
    val name: String,
    val presetName: String,
    @ColumnInfo
    val presetTime: Int,
    @ColumnInfo
    val notificationTime: Int,
    //@PrimaryKey(autoGenerate = true)
    //val presetTimerId: Long? = null,
)