package com.example.timerapp.others

import com.example.timerapp.database.PresetTimer
import java.lang.StringBuilder

fun convertLongToTimeString(time: Long): String {
    val allSec = time / 1000L
    val min = allSec / 60L
    val sec = allSec % 60L

    return when {
        (time == 0L) -> ""
        (min == 0L) -> {
            sec.toString() + "秒"
        }
        (sec == 0L) -> {
            min.toString() + "分"
        }
        else -> {
            min.toString() + "分" + sec.toString() + "秒"
        }
    }
}

fun setIntToNumberPicker(time: Long): Map<Int, Long> {
    val allSec = time / 1000L
    val min = allSec / 60L
    val num1 = min / 100L
    val num2 = min % 100L / 10L
    val num3 = min % 100L % 10L

    val sec = allSec % 60L
    val num4 = sec / 10L
    val num5 = sec % 10L
    return mapOf(
        1 to num1, 2 to num2, 3 to num3, 4 to num4, 5 to num5
    )
}

fun setPreNotification(time: Long): Map<String, Long> {
    val allSec = time / 1000L
    val min = allSec / 60L
    val sec = allSec % 60L
    return mapOf("min" to min, "sec" to sec)
}

fun convertDetail(presetList: List<PresetTimer>): String {
    return when (presetList.count()) {
        0 -> "no presetTimer"
        1 -> {
            if (presetList.first().notificationTime == 0L) {
                "通知: なし"
            } else {
                val notificationTime = convertLongToTimeString(presetList.first().notificationTime)
                "通知: " + notificationTime + "前"
            }
        }
        else -> {
            val sb = StringBuilder()
            sb.apply {
                presetList.forEach { presetTimer ->
                    append("${presetTimer.presetName}\t")
                    append("${convertLongToTimeString(presetTimer.presetTime)}\t")
                    if (presetTimer.notificationTime == 0L) {
                        append("通知: なし\n")
                    } else {
                        append("通知: " + convertLongToTimeString(presetTimer.notificationTime) + "前\n")
                    }
                }
            }
            sb.toString()
        }
    }
}
