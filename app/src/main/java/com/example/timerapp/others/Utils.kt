package com.example.timerapp.others

fun convertLongToTimeString(time: Int): String{

    val allSec = time / 1000
    val min = allSec / 60
    val sec = allSec % 60

    return if (sec == 0) { min.toString() + "分"
    } else {
        min.toString() + "分" + sec.toString() + "秒" }
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
