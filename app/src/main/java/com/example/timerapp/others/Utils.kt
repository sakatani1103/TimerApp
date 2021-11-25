package com.example.timerapp.others

import com.example.timerapp.database.PresetTimer
import java.lang.StringBuilder

fun convertLongToTimeString(time: Int): String {
    val allSec = time / 1000
    val min = allSec / 60
    val sec = allSec % 60

    return when {
        (time == 0) -> ""
        (min == 0) -> { sec.toString() + "秒"}
        (sec == 0)  -> { min.toString() + "分"}
        else -> { min.toString() + "分" + sec.toString() + "秒" }
    }
}

fun setIntToNumberPicker(time: Int): Map<Int, Int> {
    val allSec = time / 1000
    val min = allSec / 60
    val num1 = min / 100
    val num2 = min % 100 / 10
    val num3 = min % 100 % 10

    val sec = allSec % 60
    val num4 = sec / 10
    val num5 = sec % 10
    return mapOf(
        1 to num1, 2 to num2, 3 to num3, 4 to num4, 5 to num5
    )
}

fun setPreNotification(time: Int): Map<String, Int> {
    val allSec = time / 1000
    val min = allSec / 60
    val sec = allSec % 60
    return mapOf("min" to min, "sec" to sec)
}

fun convertDetail(presetList: List<PresetTimer>): String {
    return when (presetList.count()) {
        0 -> "no presetTimer"
        1 -> {
            if (presetList.first().notificationTime == 0) {
                "通知: なし"
            } else {
                val notificationTime = convertLongToTimeString(presetList.first().notificationTime)
                "通知: "+ notificationTime + "前"
            }
        }
        else -> {
            val sb = StringBuilder()
            sb.apply {
                presetList.forEach { presetTimer ->
                    append("${presetTimer.presetName}\t")
                    append("${convertLongToTimeString(presetTimer.presetTime)}\t")
                    if (presetTimer.notificationTime == 0){
                        append("通知: なし\n")
                    } else {
                        append("通知: " + convertLongToTimeString(presetTimer.notificationTime) +"前\n")
                    }
                }
            }
            sb.toString()
        }
    }
}
