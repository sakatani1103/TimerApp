package com.example.timerapp.others

import com.example.timerapp.database.PresetTimer
import org.junit.Assert.*
import org.junit.Test

class UtilsTest {
    @Test
    fun getTimeString_zeroInput_returnsEmpty() {
        val time = convertLongToTimeString(0)
        assertEquals(time, "")
    }

    @Test
    fun getTimeString_nineMillionInput_returns150min() {
        val time = convertLongToTimeString(9000000)
        assertEquals(time, "150分")
    }

    @Test
    fun getTimeString_thirtyThousandInput_returns30sec() {
        val time = convertLongToTimeString(30000)
        assertEquals(time, "30秒")
    }

    @Test
    fun getTimeString_nineMillionThirtyThousandInput_returns150min30sec() {
        val time = convertLongToTimeString(9030000)
        assertEquals(time, "150分30秒")
    }

    @Test
    fun getMapForNumberPicker_nineMillionThirtyThousandInput_returns15030() {
        val time = setIntToNumberPicker(9030000)
        val correct = mapOf(1 to 1, 2 to 5, 3 to 0, 4 to 3, 5 to 0)
        assertEquals(time, correct)
    }

    @Test
    fun getMapForNotificationNumberPicker_millionThousandInput_returns10() {
        val time = setPreNotification(610000)
        val correct = mapOf("min" to 10, "sec" to 10)
        assertEquals(time, correct)
    }

    @Test
    fun getTimerDetail_noPresetTimer_returnsEmptyString() {
        val input = mutableListOf<PresetTimer>()
        val text = convertDetail(input)
        assertEquals(text, "")
    }

    @Test
    fun getTimerDetail_onePresetTimer_returnsNotificationString() {
        val presetTimer = PresetTimer("timer1", "preset1", 1, 9000000, 600000)
        val input = mutableListOf(presetTimer)
        val text = convertDetail(input)
        assertEquals(text, "通知: 10分前")
    }

    @Test
    fun getTimerDetail_onePresetTimerNoNotification_returnsNotificationString() {
        val presetTimer = PresetTimer("timer1", "preset1", 1, 9000000, 0)
        val input = mutableListOf(presetTimer)
        val text = convertDetail(input)
        assertEquals(text, "通知: なし")
    }

    @Test
    fun getTimerDetail_threePresetTimer_returnsDetailString() {
        val presetTimer1 = PresetTimer("timer1", "preset1", 1, 9000000, 600000)
        val presetTimer2 = PresetTimer("timer1", "preset2", 2, 9000000, 600000)
        val presetTimer3 = PresetTimer("timer1", "preset3", 3, 9000000, 0)
        val input = mutableListOf(presetTimer1, presetTimer2, presetTimer3)
        val text = convertDetail(input)
        assertEquals(text, "preset1\t150分\t通知: 10分前\n" +
                "preset2\t150分\t通知: 10分前\n"+
            "preset3\t150分\t通知: なし\n")
    }
}