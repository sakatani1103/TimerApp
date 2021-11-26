package com.example.timerapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ListType {
    SIMPLE_LAYOUT,
    DETAIL_LAYOUT,
    INITIAL_LAYOUT
}

enum class NotificationType {
    VIBRATION,
    ALARM
}

@Entity
data class Timer(
    @PrimaryKey(autoGenerate = false)
    val name: String,
    @ColumnInfo
    val total: Int = 0,
    @ColumnInfo
    val listType: ListType = ListType.SIMPLE_LAYOUT,
    @ColumnInfo
    val notificationType: NotificationType = NotificationType.VIBRATION,
    @ColumnInfo
    val isDisplay: Boolean = true,
    @ColumnInfo
    val detail: String = "no presetTimer",
    @ColumnInfo
    val isSelected: Boolean = false
)
