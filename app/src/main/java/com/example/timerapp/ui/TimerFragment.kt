package com.example.timerapp.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.timerapp.R
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.FragmentTimerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimerFragment : Fragment() {
    private lateinit var viewModel: TimerViewModel
    private var _binding: FragmentTimerBinding? = null
    private val binding: FragmentTimerBinding
        get() = _binding!!

    private lateinit var customCountDownTimer: CustomCountDownTimer
    private var timerStatus = Status.PROGRESS
    private var resumeFromMillis: Long = 0

    private lateinit var soundPool: SoundPool
    private var soundNotification1Id = 0
    private var soundNotification2Id = 0
    private var soundPreNotificationId = 0
    var currentSound = 1

    private lateinit var timerFinishVibrator: Vibrator

    private lateinit var presetTimerList: MutableList<PresetTimer>
    private lateinit var currentTimer: Timer
    private var others = 0L

    private var preNotificationTime: Long = 0
    private var isNotification = false

    inner class CustomCountDownTimer(
        millisInFuture: Long, countDownInterval: Long
    ) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUtilFinished: Long) {
            when (timerStatus) {
                Status.PAUSE -> {
                    resumeFromMillis = millisUtilFinished
                    cancel()
                    displayTime(millisUtilFinished)
                }
                else -> {
                    displayTime(millisUtilFinished)
                }
            }
            if (preNotificationTime > 0 && preNotificationTime >= millisUtilFinished && !isNotification) {
                notifyPreNotification(currentTimer.notificationType)
                isNotification = true
            }
        }

        override fun onFinish() {
            binding.presetMin.text = getString(R.string.initial_min)
            binding.presetSec.text = getString(R.string.initial_sec)
            presetTimerList.removeAt(0)
            if (presetTimerList.isEmpty()) {
                notifyFinishTimer(currentTimer.notificationType)
            } else {
                others -= presetTimerList.first().presetTime
                notifyPresetTimerFinish(currentTimer.notificationType)
                setFirstElement()
                customCountDownTimer = CustomCountDownTimer(presetTimerList.first().presetTime, 100)
                customCountDownTimer.start()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_timer, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        timerFinishVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                activity?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        binding.cancelBtn.setOnClickListener {
            when (timerStatus) {
                Status.PROGRESS -> {
                    binding.stopOrStartBtn.setImageResource(R.drawable.ic_pause)
                    timerStatus = Status.PAUSE
                    createCancelDialog()
                }
                else -> {
                    createCancelDialog()
                }
            }
        }

        binding.stopOrStartBtn.setOnClickListener {
            when (timerStatus) {
                Status.PROGRESS -> {
                    binding.stopOrStartBtn.setImageResource(R.drawable.ic_play_arrow)
                    timerStatus = Status.PAUSE
                }
                else -> {
                    timerStatus = Status.PROGRESS
                    binding.stopOrStartBtn.setImageResource(R.drawable.ic_pause)
                    customCountDownTimer = CustomCountDownTimer(resumeFromMillis, 100)
                    customCountDownTimer.start()
                }
            }
        }

        presetTimerList = viewModel.presetTimerList.value?.toMutableList() ?: mutableListOf()
        currentTimer = viewModel.currentTimer.value!!
        others = currentTimer.total - presetTimerList.first().presetTime

        setFirstElement()
        customCountDownTimer = CustomCountDownTimer(presetTimerList.first().presetTime, 100)
        customCountDownTimer.start()
    }

    override fun onResume() {
        super.onResume()
        soundPool = SoundPool.Builder().run {
            val audioAttributes = AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_GAME)
                build()
            }
            setMaxStreams(2)
            setAudioAttributes(audioAttributes)
            build()
        }
        soundNotification1Id = soundPool.load(context, R.raw.notification, 1)
        soundNotification2Id = soundPool.load(context, R.raw.notification, 1)
        soundPreNotificationId = soundPool.load(context, R.raw.prenotification, 1)
    }

    override fun onPause() {
        super.onPause()
        soundPool.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createCancelDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.timer_stop_message)
            .setPositiveButton(R.string.ok) { _, _ -> this.findNavController().popBackStack() }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun createNotificationDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.finish_message)
            .setPositiveButton(R.string.ok) { _, _ ->
                when (currentTimer.notificationType) {
                    NotificationType.ALARM -> if (currentSound == 1) {
                        soundPool.stop(soundNotification1Id)
                    } else {
                        soundPool.stop(soundNotification2Id)
                    }
                    NotificationType.VIBRATION -> timerFinishVibrator.cancel()
                }
                this.findNavController().popBackStack()
            }
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun displayTime(millisUtilFinished: Long) {
        val minute = millisUtilFinished / 1000L / 60L
        val second = millisUtilFinished / 1000L % 60L
        binding.presetMin.text = getString(R.string.set_min).format(minute)
        binding.presetSec.text = getString(R.string.set_sec).format(second)
        val totalMinute = (others + millisUtilFinished) / 1000L / 60L
        val totalSec = (others + millisUtilFinished) / 1000L % 60L
        binding.timerMin.text = getString(R.string.set_min).format(totalMinute)
        binding.timerSec.text = getString(R.string.set_sec).format(totalSec)
    }

    private fun setFirstElement() {
        preNotificationTime = presetTimerList.first().notificationTime
        binding.presetTimerTitle.text = presetTimerList.first().presetName
        isNotification = false
    }

    private fun finishTimerVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(300, 200),
                intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE),
                0
            )
            timerFinishVibrator.vibrate(vibrationEffect)
        } else {
            timerFinishVibrator.vibrate(longArrayOf(300, 200), 0)
        }
    }

    private fun preNotificationVibration() {
        val preNotificationVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                activity?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect =
                VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
            preNotificationVibrator.vibrate(vibrationEffect)
        } else {
            preNotificationVibrator.vibrate(300)
        }
    }

    private fun presetTimerVibration() {
        val presetTimerVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                activity?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(600, 200),
                intArrayOf(VibrationEffect.DEFAULT_AMPLITUDE, 0),
                -1
            )
            presetTimerVibrator.vibrate(vibrationEffect)
        } else {
            presetTimerVibrator.vibrate(longArrayOf(600, 200), -1)
        }
    }

    private fun notifyPresetTimerFinish(notificationType: NotificationType) {
        when (notificationType) {
            NotificationType.VIBRATION -> presetTimerVibration()
            NotificationType.ALARM -> {
                currentSound = if (currentSound == 1) {
                    soundPool.play(soundNotification2Id, 0.5f, 100f, 1, 0, 1.0f)
                    2
                } else {
                    soundPool.play(soundNotification1Id, 0.5f, 100f, 1, 0, 1.0f)
                    1
                }
            }
        }
    }

    private fun notifyPreNotification(notificationType: NotificationType) {
        when (notificationType) {
            NotificationType.VIBRATION -> preNotificationVibration()
            NotificationType.ALARM -> soundPool.play(soundPreNotificationId, 0.5f, 100f, 1, 0, 1.0f)
        }
    }

    private fun notifyFinishTimer(notificationType: NotificationType) {
        createNotificationDialog()
        when (notificationType) {
            NotificationType.VIBRATION -> finishTimerVibration()
            NotificationType.ALARM -> if (currentSound == 1) {
                soundPool.play(soundNotification2Id, 0.5f, 100f, 1, 10, 1.0f)
            } else {
                soundPool.play(soundNotification1Id, 0.5f, 100f, 1, 10, 1.0f)
            }
        }
    }
}

enum class Status { PROGRESS, PAUSE }
