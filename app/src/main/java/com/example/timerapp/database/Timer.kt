package com.example.timerapp.database

data class Timer(
    val id: Int,
    val name: String,
    val total: String,
    val detail: String,
    val is_notification: Boolean,
    val notification_time: String,
    var isExpanded: Boolean = false
)
