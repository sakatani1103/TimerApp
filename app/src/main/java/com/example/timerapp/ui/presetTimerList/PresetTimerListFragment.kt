package com.example.timerapp.ui.presetTimerList

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.R
import com.example.timerapp.TimerApplication
import com.example.timerapp.adapter.PresetTimerClickListener
import com.example.timerapp.adapter.PresetTimerListAdapter
import com.example.timerapp.databinding.FragmentPresetTimerListBinding
import com.example.timerapp.others.EventObserver
import com.example.timerapp.others.Status
import com.example.timerapp.repository.DefaultTimerRepository
import com.google.android.material.snackbar.Snackbar

class PresetTimerListFragment : Fragment() {
    private var _binding: FragmentPresetTimerListBinding? = null
    private val binding: FragmentPresetTimerListBinding
        get() = _binding!!

    private val args: PresetTimerListFragmentArgs by navArgs()
    private val viewModel by viewModels<PresetTimerListViewModel> {
        PresetTimerListViewModelFactory((requireContext().applicationContext as TimerApplication).timerRepository)
    }
    private lateinit var presetTimerListAdapter: PresetTimerListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_preset_timer_list, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.start(args.timerName)
        subscribeToPresetTimerListObservers()
        setupRecyclerView()

        binding.addList.setOnClickListener {
            viewModel.navigateToSettingTimer(null)
        }

        binding.etTimerName.setOnKeyListener { _, i, keyEvent ->
            return@setOnKeyListener setKeyBoard(view, i, keyEvent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val clickListener = PresetTimerClickListener { presetTimerId ->
            viewModel.navigateToSettingTimer(presetTimerId)
        }
        presetTimerListAdapter =
            PresetTimerListAdapter(clickListener, viewLifecycleOwner)
        binding.presetTimerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = presetTimerListAdapter
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)
        }
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPos = viewHolder.adapterPosition
            val toPos = target.adapterPosition
            presetTimerListAdapter.notifyItemMoved(fromPos, toPos)
            viewModel.changePresetTimerOrder(fromPos, toPos)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.layoutPosition
            val item = presetTimerListAdapter.presetTimers[pos]
            viewModel.deletePresetTimerAndUpdateTimer(item)
        }
    }

    private fun subscribeToPresetTimerListObservers() {
        viewModel.presetTimerList.observe(viewLifecycleOwner, {
            presetTimerListAdapter.presetTimers = it
        })

        viewModel.navigateToTimer.observe(viewLifecycleOwner, EventObserver { result ->
            when (result.status) {
                Status.SUCCESS ->
                    result.data?.let { timerName ->
                        this.findNavController().navigate(
                            PresetTimerListFragmentDirections.actionPresetTimerListFragmentToTimerFragment(
                                timerName
                            )
                        )
                    }
                Status.ERROR -> createSnackbar(result.message)
            }
        })

        viewModel.navigateToSettingTimer.observe(viewLifecycleOwner, EventObserver { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { ids ->
                        val presetTimerId = ids["presetTimerId"]
                        val timerName = ids["timerName"].toString()
                        this.findNavController().navigate(
                            PresetTimerListFragmentDirections.actionPresetTimerListFragmentToSetTimerFragment(
                                presetTimerId = presetTimerId,
                                timerName = timerName
                            )
                        )
                    }
                }
                Status.ERROR -> createSnackbar(result.message)
            }

        })

        viewModel.navigateToDeletePresetTimer.observe(
            viewLifecycleOwner,
            EventObserver { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { timerName ->
                            this.findNavController().navigate(
                                PresetTimerListFragmentDirections.actionPresetTimerListFragmentToDeletePresetTimerListFragment(
                                    timerName
                                )
                            )
                        }
                    }
                    Status.ERROR -> createSnackbar(result.message)
                }
            })

        viewModel.navigateToTimerList.observe(viewLifecycleOwner, EventObserver { boolean ->
            if (boolean) {
                this.findNavController()
                    .navigate(PresetTimerListFragmentDirections.actionPresetTimerListFragmentToTimerListFragment())
            }
        })

        viewModel.deletePresetTimerStatus.observe(viewLifecycleOwner, EventObserver { result ->
            if (result.status == Status.SUCCESS) {
                Snackbar.make(
                    requireView(),
                    "${result.data!!.presetName}を削除しました。",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("取り消し") { viewModel.restorePresetTimerAndUpdateTimer(result.data) }
                    .show()
            }
        })

        viewModel.updateTimerStatus.observe(viewLifecycleOwner, EventObserver { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    Snackbar.make(binding.root, "タイマーの設定を変更しました。", Snackbar.LENGTH_LONG).show()
                    binding.timerNameLayout.error = null
                }
                Status.ERROR -> {
                    binding.etTimerName.setText(result.data?.name)
                    binding.timerNameLayout.error = result.message
                }
            }
        })
    }

    private fun setKeyBoard(view: View, i: Int, keyEvent: KeyEvent): Boolean {
        if ((keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_ENTER) ||
            (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK)
        ) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
            return true
        }
        return false
    }

    private fun createSnackbar(msg: String?) {
        msg?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }
    }
}

