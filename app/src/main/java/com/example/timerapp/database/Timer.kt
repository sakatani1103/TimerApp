package com.example.timerapp.database

enum class ListType {
    SIMPLE_LAYOUT,
    DETAIL_LAYOUT
}

data class Timer(
    val id: Int,
    val name: String,
    val total: String,
    val detail: String,
    val listType: ListType,
    val is_notification: Boolean,
    val notification_time: String,
    var isExpanded: Boolean = false
)
