package com.example.timerapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class PresetTimer(
    @ColumnInfo
    val name: String,
    @ColumnInfo
    val presetName: String,
    @ColumnInfo
    val timerOrder: Int,
    @ColumnInfo
    val presetTime: Long = 0L,
    @ColumnInfo
    val notificationTime: Long = 0L,
    @ColumnInfo
    val isSelected: Boolean = false,
    @PrimaryKey
    val presetTimerId: String = UUID.randomUUID().toString()
)