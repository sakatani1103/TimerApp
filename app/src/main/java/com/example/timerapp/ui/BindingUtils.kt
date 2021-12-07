package com.example.timerapp.ui

import android.widget.*
import androidx.databinding.BindingAdapter
import com.example.timerapp.R
import com.example.timerapp.R.*
import com.example.timerapp.database.NotificationType
import com.example.timerapp.others.convertLongToTimeString
import com.example.timerapp.others.setPreNotification
import com.google.android.material.card.MaterialCardView

// simpleListItem & listItem
@BindingAdapter("totalTime")
fun TextView.setTotalTime(totalTime: Long) {
    if (totalTime > 0L) {
        val totalString = convertLongToTimeString(totalTime)
        val output = "計 $totalString"
        text = output
    }
}

@BindingAdapter("notificationType", "timerDetail")
fun ImageView.setNotificationImage(notificationType: NotificationType, timerDetail: String) {
    if (timerDetail != "no presetTimer") {
        setImageResource(
            when (notificationType) {
                NotificationType.VIBRATION -> R.drawable.ic_vibration
                NotificationType.ALARM -> R.drawable.ic_notifications
            }
        )
    }
}

@BindingAdapter("notificationType")
fun TextView.setNotificationTv(notificationType: NotificationType) {
    text = when (notificationType) {
        NotificationType.VIBRATION -> {
            context.getString(string.vibration)
        }
        NotificationType.ALARM -> {
            context.getString(string.alarm)
        }
    }
}

@BindingAdapter("timerDetail")
fun TextView.setTimerDetail(timerDetail: String) {
    if (timerDetail != "no presetTimer") {
        text = timerDetail
    }
}

// presetTimerListItem
@BindingAdapter("customTime")
fun TextView.setPresetTime(customTime: Long) {
    text = convertLongToTimeString(customTime)
}

@BindingAdapter("notificationTime")
fun TextView.setNotificationTime(notificationTime: Long) {
    text = if (notificationTime > 0L) {
        val time = convertLongToTimeString(notificationTime)
        val output = "通知：　$time 前"
        output
    } else {
        ""
    }
}

// delete function
@BindingAdapter("selectedColor")
fun MaterialCardView.setSelectedColor(selectedColor: Boolean) {
    if (selectedColor) {
        setBackgroundColor(context.getColor(color.light_purple))
    } else {
        setBackgroundColor(context.getColor(color.white))
    }
}

@BindingAdapter("selectedImage")
fun ImageView.setSelectedImage(selectedImage: Boolean) {
    if (selectedImage) {
        setImageResource(R.drawable.ic_check)
    } else {
        setImageResource(R.drawable.ic_check_outline)
    }
}

// notificationTimerDialog
//@BindingAdapter("temporalNotificationTime", "timePlace", "temporalPresetTime")
//fun NumberPicker.setPresetTimerNumber(
//    temporalNotificationTime: Long,
//    timePlace: Int,
//    temporalPresetTime: Long
//) {
//    // presetTimeに応じて設定できる範囲を決める
//    val numMap = setPreNotification(temporalPresetTime)
//    val setNumMap = if (temporalPresetTime > 0) {
//        setPreNotification(temporalNotificationTime)
//    } else mapOf("min" to 0, "sec" to 0)
//    // min単位で時間が設定されている場合は分設定のみ変更
//    if (numMap["min"]!! > 0L) {
//        when (timePlace) {
//            1 -> {
//                maxValue = numMap["min"]!!.toInt()
//                minValue = 0
//                value = setNumMap["min"]!!.toInt()
//            }
//            2 -> {
//                maxValue = 60
//                minValue = 0
//                value = setNumMap["sec"]!!.toInt()
//            }
//        }
//    } else {
//        when (timePlace) {
//            1 -> {
//                maxValue = 999
//                minValue = 0
//                value = setNumMap["min"]!!.toInt()
//            }
//            2 -> {
//                maxValue = 60
//                minValue = 0
//                value = setNumMap["sec"]!!.toInt()
//            }
//        }
//    }
//}










