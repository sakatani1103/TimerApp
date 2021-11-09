package com.example.timerapp.database

import androidx.room.Embedded
import androidx.room.Relation

data class TimerWithPresetTimer (
    @Embedded val timer: Timer,
    @Relation(
        parentColumn = "name",
        entityColumn = "name",
    )
    val presetTimer: List<PresetTimer>
)