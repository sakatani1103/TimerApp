package com.example.timerapp

import android.annotation.TargetApi
import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@TargetApi(31)
class AnimationTestRule : TestRule {
    private val currWinScale = getWindowAnimationScale()
    private val currTraScale = getTransitionAnimationScale()
    private val currDurScale = getAnimatorDurationScale()

    // スケールの取得
    private fun getWindowAnimationScale(): Float = getScale(Settings.Global.WINDOW_ANIMATION_SCALE)
    private fun getTransitionAnimationScale(): Float = getScale(Settings.Global.TRANSITION_ANIMATION_SCALE)
    private fun getAnimatorDurationScale(): Float = getScale(Settings.Global.ANIMATOR_DURATION_SCALE)

    private fun getScale(name: String): Float {
        val uiDevice = UiDevice.getInstance(
            InstrumentationRegistry.getInstrumentation()
        )
        val command = String.format("settings get global %s", name)
        val result = uiDevice.executeShellCommand(command)
        if (!result.startsWith("null")) {
            val scale = java.lang.Float.valueOf(result)
            if (scale >= 0.0f) {
                return scale
            }
        }
        return 1.0f
    }

    fun putWindowAnimationScale(scale: Float) {
        putScale(Settings.Global.WINDOW_ANIMATION_SCALE, scale)
    }

    fun putTransitionAnimationScale(scale: Float) {
        putScale(Settings.Global.TRANSITION_ANIMATION_SCALE, scale)
    }

    fun putAnimatorDurationScale(scale: Float) {
        putScale(Settings.Global.ANIMATOR_DURATION_SCALE, scale)
    }

    private fun putScale(name: String, scale: Float) {
        val uiDevice = UiDevice.getInstance(
            InstrumentationRegistry.getInstrumentation()
        )
        val command = String.format("settings put global %s %f", name, scale)
        uiDevice.executeShellCommand(command)
    }

    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                try {
                    putWindowAnimationScale(0.0f)
                    putTransitionAnimationScale(0.0f)
                    putAnimatorDurationScale(0.0f)
                    base?.evaluate()
                } finally {
                    putWindowAnimationScale(currWinScale)
                    putTransitionAnimationScale(currTraScale)
                    putAnimatorDurationScale(currDurScale)
                }
            }
        }
    }
}