package com.example.timerapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

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
    @ColumnInfo
    val name: String,
    @ColumnInfo
    val total: Long = 0L,
    @ColumnInfo
    val listType: ListType = ListType.SIMPLE_LAYOUT,
    @ColumnInfo
    val notificationType: NotificationType = NotificationType.VIBRATION,
    @ColumnInfo
    val isDisplay: Boolean = false,
    @ColumnInfo
    val detail: String = "no presetTimer",
    @ColumnInfo
    val isSelected: Boolean = false,
    @PrimaryKey
    val timerId: String = UUID.randomUUID().toString()
)
