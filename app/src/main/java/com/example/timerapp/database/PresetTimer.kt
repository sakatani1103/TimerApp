package com.example.timerapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["name", "presetName", "timerOrder"])
data class PresetTimer(
    val name: String,
    val presetName: String,
    val timerOrder: Int,
    @ColumnInfo
    val presetTime: Long = 0L,
    @ColumnInfo
    val notificationTime: Long = 0L,
    @ColumnInfo
    val isSelected: Boolean = false
)