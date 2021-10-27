package com.example.timerapp.database

data class PresetTimer (
    val id: Long,
    val name: String,
    val presetName: String,
    val presetTime: String,
    val notificationTime: String
)