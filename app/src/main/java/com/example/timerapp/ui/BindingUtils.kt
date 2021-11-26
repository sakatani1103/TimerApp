package com.example.timerapp.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.opengl.Visibility
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.navigation.ActivityNavigatorExtras
import com.example.timerapp.R
import com.example.timerapp.R.*
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.others.convertLongToTimeString
import com.example.timerapp.others.setIntToNumberPicker
import com.example.timerapp.others.setPreNotification
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import org.w3c.dom.Text

// simpleListItem & listItem
@BindingAdapter("timerTotal", "timerDetail")
fun TextView.setTotalTime(timerTotal: Int, timerDetail: String) {
    if (timerDetail != "no presetTimer" && timerTotal > 0) {
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
    text = if (notificationTime > 0) {
        val time = convertLongToTimeString(notificationTime)
        val output = "通知：　$time 前"
        output
    } else {
        ""
    }
}

// settingTimerFragment
@BindingAdapter("presetTime", "place")
fun NumberPicker.setNumber(presetTime: Int, place: Int) {
    maxValue = 9
    minValue = 0
    if (presetTime > 0) {
        val numMap = setIntToNumberPicker(presetTime)
        value = numMap[place]!!
    }
}

// delete function
@BindingAdapter("selectedColor")
fun MaterialCardView.setSelectedColor(selectedColor: Boolean){
    if (selectedColor) {
        setBackgroundColor(context.getColor(color.light_purple))
    } else {
        setBackgroundColor(context.getColor(color.white))
    }
}

@BindingAdapter("selectedImage")
fun ImageView.setSelectedImage(selectedImage: Boolean){
    if(selectedImage){
        setImageResource(R.drawable.ic_check)
    } else {
        setImageResource(R.drawable.ic_check_outline)
    }
}

// notificationTimerDialog
@BindingAdapter("temporalNotificationTime", "timePlace", "temporalPresetTime")
fun NumberPicker.setPresetTimerNumber(temporalNotificationTime: Int, timePlace: Int, temporalPresetTime: Int){
    // presetTimeに応じて設定できる範囲を決める
    val numMap = setPreNotification(temporalPresetTime)
    // min単位で時間が設定されている場合は分設定のみ変更
    if (numMap["min"]!! > 0){
        when(timePlace) {
            1 -> {
                maxValue = numMap["min"]!!
                minValue = 0
            }
            2 -> {
                maxValue = 60
                minValue = 0
            }
        }
    } else {
        when(timePlace){
            1 -> {
                maxValue = 999
                minValue = 0
            }
            2 -> {
                maxValue = 60
                minValue = 0
            }
        }
    }
    // notificationTimeが設定されている場合には初期値をセット
    if (temporalNotificationTime > 0) {
        val setNumMap = setPreNotification(temporalNotificationTime)
        if (setNumMap["min"]!! > 0) {
            when(timePlace) {
                1 -> value = setNumMap["min"]!!
                2 -> value = setNumMap["sec"]!!
            }
        } else {
            value = setNumMap["sec"]!!
        }
    }
}

@BindingAdapter("presetItems")
fun CardView.setInitialMessage(presetItems: List<PresetTimer>?){
    isGone = presetItems != null && presetItems.count() > 0
}









