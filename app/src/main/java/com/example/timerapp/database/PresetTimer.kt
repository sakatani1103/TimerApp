package com.example.timerapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(primaryKeys = ["name", "presetName", "timerOrder"])
data class PresetTimer (
    val name: String,
    val presetName: String,
    val timerOrder: Int,
    @ColumnInfo
    val presetTime: Int = 0,
    @ColumnInfo
    val notificationTime: Int = 0,
    @ColumnInfo
    val isSelected: Boolean = false
)