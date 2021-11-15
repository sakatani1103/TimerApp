package com.example.timerapp.ui

import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.timerapp.R
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.others.convertLongToTimeString
import com.example.timerapp.others.setIntToNumberPicker
import com.google.android.material.switchmaterial.SwitchMaterial
import org.w3c.dom.Text

// simpleListItem & listItem
@BindingAdapter("timerTotal", "timerDetail")
fun TextView.setTotalTime(timerTotal: Int, timerDetail: String) {
    if (timerDetail != "no presetTimer") {
        val totalString = convertLongToTimeString(timerTotal)
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
    when (notificationType) {
        NotificationType.VIBRATION -> R.string.vibration
        NotificationType.ALARM -> R.string.alarm
    }
}

@BindingAdapter("timerDetail")
fun TextView.setTimerDetail(timerDetail: String) {
    if (timerDetail != "no presetTimer") {
        text = timerDetail
    }
}

// FragmentPresetTimer
@BindingAdapter("sound")
fun Spinner.setSound(notificationType: NotificationType?) {
    notificationType.let {
        if (notificationType == NotificationType.VIBRATION) {
            setSelection(0)
        } else {
            setSelection(1)
        }
    }
}

// presetTimerListItem
@BindingAdapter("customTime")
fun TextView.setPresetTime(customTime: Int){
    text =  convertLongToTimeString(customTime)
}

@BindingAdapter("notificationTime")
fun TextView.setNotificationTime(notificationTime: Int){
    val time = convertLongToTimeString(notificationTime)
    val output = "通知：　$time 前"
    text = output
}

// settingTimerFragment
@BindingAdapter("presetTime", "place")
fun NumberPicker.setNumber(presetTime: Int, place: Int) {
    if (presetTime == 0) {
        maxValue = 9
        minValue = 0
    } else {
        maxValue = 9
        minValue = 0
        val numMap = setIntToNumberPicker(presetTime)
        value = numMap[place]!!
    }
}


@BindingAdapter("preNotification")
fun SwitchMaterial.setPreNotification(preNotification: Int) {
    if (preNotification > 0) {
        isChecked = true
    }
}









