package com.example.timerapp.ui.timer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.timerapp.R
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.FragmentTimerBinding
import com.example.timerapp.others.EventObserver
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TimerFragment : Fragment() {
    private var _binding: FragmentTimerBinding? = null
    private val binding: FragmentTimerBinding
        get() = _binding!!

    private val args: TimerFragmentArgs by navArgs()
    private val viewModel by viewModels<TimerViewModel>()

    private lateinit var soundPool: SoundPool
    private var soundNotification1Id = 0
    private var soundNotification2Id = 0
    private var soundPreNotificationId = 0
    var currentSound = 1

    private var status = Status.PROGRESS

    private lateinit var timerFinishVibrator: Vibrator

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
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.start(args.timerName)
        subscribeToTimerNotification()

        timerFinishVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                activity?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        binding.cancelBtn.setOnClickListener {
            if (status == Status.PROGRESS) {
                binding.stopOrStartBtn.setImageResource(R.drawable.ic_play_arrow)
                viewModel.timerPause()
                status = Status.PAUSE
            }
            createCancelDialog()
        }


        binding.stopOrStartBtn.setOnClickListener {
            when (status) {
                Status.PROGRESS -> {
                    status = Status.PAUSE
                    binding.stopOrStartBtn.setImageResource(R.drawable.ic_play_arrow)
                    viewModel.timerPause()
                }
                else -> {
                    status = Status.PROGRESS
                    binding.stopOrStartBtn.setImageResource(R.drawable.ic_pause)
                    viewModel.timerStart()
                }
            }
        }
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

    private fun subscribeToTimerNotification() {
        viewModel.notifyPreNotification.observe ( viewLifecycleOwner,  EventObserver{ type ->
            notifyPreNotification(type)
        })
        viewModel.notifyPresetTimerFinish.observe(viewLifecycleOwner, EventObserver{ type ->
            notifyPresetTimerFinish(type)
        })
        viewModel.notifyTimerFinish.observe(viewLifecycleOwner, EventObserver {type ->
            notifyFinishTimer(type)
        })
    }

    private fun createCancelDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.timer_stop_message)
            .setPositiveButton(R.string.ok) { _, _ -> this.findNavController().popBackStack() }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun createNotificationDialog(notificationType: NotificationType) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.finish_message)
            .setPositiveButton(R.string.ok) { _, _ ->
                when (notificationType) {
                    NotificationType.ALARM -> if (currentSound == 1) {
                        soundPool.stop(soundNotification1Id)
                    } else {
                        soundPool.stop(soundNotification2Id)
                    }
                    NotificationType.VIBRATION -> timerFinishVibrator.cancel()
                }
                this.findNavController().navigate(TimerFragmentDirections.actionTimerFragmentToTimerListFragment())
            }
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
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
        createNotificationDialog(notificationType)
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

