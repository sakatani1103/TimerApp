package com.example.timerapp.others

import com.example.timerapp.database.PresetTimer
import com.google.common.truth.Truth.assertThat

import org.junit.Test

class UtilsTest {
    @Test
    fun getTimeString_zeroInput_returnsEmpty() {
        val time = convertLongToTimeString(0)
        assertThat(time).isEqualTo("")
    }

    @Test
    fun getTimeString_nineMillionInput_returns150min() {
        val time = convertLongToTimeString(9000000)
        assertThat(time).isEqualTo("150分")
    }

    @Test
    fun getTimeString_thirtyThousandInput_returns30sec() {
        val time = convertLongToTimeString(30000)
        assertThat(time).isEqualTo("30秒")
    }

    @Test
    fun getTimeString_nineMillionThirtyThousandInput_returns150min30sec() {
        val time = convertLongToTimeString(9030000)
        assertThat(time).isEqualTo("150分30秒")
    }

    @Test
    fun getMapForNumberPicker_nineMillionThirtyThousandInput_returns15030() {
        val time = setIntToNumberPicker(9030000)
        assertThat(time[1]).isEqualTo(1)
        assertThat(time[2]).isEqualTo(5)
        assertThat(time[3]).isEqualTo(0)
        assertThat(time[4]).isEqualTo(3)
        assertThat(time[5]).isEqualTo(0)
    }

    @Test
    fun getTimeFromNumberPicker_15030_return9030000() {
        val millSecTime = getPresetTimeFromNumberPicker(1,5,0,3,0)
        assertThat(millSecTime).isEqualTo(9030000)
    }

    @Test
    fun getMapForNotificationNumberPicker_millionThousandInput_returns10() {
        val time = setPreNotification(610000)
        assertThat(time["min"]).isEqualTo(10)
        assertThat(time["sec"]).isEqualTo(10)
    }

    @Test
    fun getTimerDetail_noPresetTimer_returnsEmptyString() {
        val input = mutableListOf<PresetTimer>()
        val text = convertDetail(input)
        assertThat(text).isEqualTo("no presetTimer")
    }

    @Test
    fun getTimerDetail_onePresetTimer_returnsNotificationString() {
        val presetTimer = PresetTimer("timer1", "preset1", 1, 9000000, 600000)
        val input = mutableListOf(presetTimer)
        val text = convertDetail(input)
        assertThat(text).isEqualTo("通知: 10分前")
    }

    @Test
    fun getTimerDetail_onePresetTimerNoNotification_returnsNotificationString() {
        val presetTimer = PresetTimer("timer1", "preset1", 1, 9000000, 0)
        val input = mutableListOf(presetTimer)
        val text = convertDetail(input)
        assertThat(text).isEqualTo("通知: なし")
    }

    @Test
    fun getNotificationTimeFromNumberPicker_50_returnThreeHundredThousand() {
        val notificationTime = getNotificationFromNumberPicker(5,0)
        assertThat(notificationTime).isEqualTo(300000)
    }

    @Test
    fun getTimerDetail_threePresetTimer_returnsDetailString() {
        val presetTimer1 = PresetTimer("timer1", "preset1", 1, 9000000, 600000)
        val presetTimer2 = PresetTimer("timer1", "preset2", 2, 9000000, 600000)
        val presetTimer3 = PresetTimer("timer1", "preset3", 3, 9000000, 0)
        val input = mutableListOf(presetTimer1, presetTimer2, presetTimer3)
        val text = convertDetail(input)
        assertThat(text).isEqualTo( "preset1\t150分\t通知: 10分前\n" +
                "preset2\t150分\t通知: 10分前\n"+
            "preset3\t150分\t通知: なし\n")
    }
}