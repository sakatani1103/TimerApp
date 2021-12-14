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
        val output = "通知: $time 前"
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











