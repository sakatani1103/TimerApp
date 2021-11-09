package com.example.timerapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// idはnullの時に自動生成
@Entity
data class PresetTimer (
    @ColumnInfo
    val name: String,
    @ColumnInfo
    val presetName: String,
    @ColumnInfo
    val presetTime: Int,
    @ColumnInfo
    val notificationTime: Int,
    @PrimaryKey(autoGenerate = true)
    val presetTimerId: Long? = null,
)