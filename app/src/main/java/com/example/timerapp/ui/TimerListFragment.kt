package com.example.timerapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.R
import com.example.timerapp.adapter.TimerListAdapter
import com.example.timerapp.adapter.TimerListListener
import com.example.timerapp.databinding.FragmentTimerListBinding
import com.example.timerapp.others.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimerListFragment: Fragment() {

    var viewModel: TimerViewModel? = null

    private lateinit var timerListAdapter: TimerListAdapter

    private var _binding: FragmentTimerListBinding? = null
    private val binding: FragmentTimerListBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_timer_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = viewModel ?: ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        subscribeToTimerListObservers()
        setupRecyclerView()

        binding.addList.setOnClickListener {
            if (viewModel!!.numberOfTimers.value!! >= Constants.TIMER_NUM){
                Snackbar.make(binding.root, "登録できるタイマーは${Constants.TIMER_NUM}までです。",
                Snackbar.LENGTH_LONG)
                    .show()
        } else {
                createDialog(null, null)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        0, RIGHT
    ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.layoutPosition
            // ViewModelの設定後、削除機能の追加
        }
    }

    private fun setupRecyclerView() {
        timerListAdapter =
            TimerListAdapter( clickListener = TimerListListener { name ->
                viewModel?.navigateToPresetTimer(name)
            }, viewLifecycleOwner)

        binding.timerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerListAdapter
        }
    }

    private fun subscribeToTimerListObservers() {
        viewModel?.navigateToPresetTimer?.observe(viewLifecycleOwner, Observer { name ->
            name?.let {
                //Snackbar.make(binding.root, "${name}", Snackbar.LENGTH_LONG).show()
                viewModel!!.getCurrentTimer(name)
                this.findNavController().navigate(
                    TimerListFragmentDirections.actionTimerListFragmentToPresetTimerListFragment(name
                    )
                )
                viewModel!!.doneNavigateToPresetTimer()
            }
        })

        viewModel?.timerItems?.observe(viewLifecycleOwner, Observer {
            timerListAdapter.timerItems = it
        })

        viewModel?.insertTimerItemStatus?.observe(viewLifecycleOwner, Observer {

        // if (it.status == Status.ERROR) setError(errorLayout)
        })
    }

    private fun createDialog(inputName: String?, msg: String?) {
        val inflater = requireActivity().layoutInflater
        val createTimerView = inflater.inflate(R.layout.dialog_create_timer, null)
        val newName = createTimerView.findViewById<EditText>(R.id.et_timer_name)
        val errorLayout = createTimerView.findViewById<TextInputLayout>(R.id.layout_timer_name)

        if (msg != null){
            newName.setText(inputName)
            errorLayout.error = msg
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(createTimerView)
            .setPositiveButton(R.string.save) { _, _ -> insertTimerName(newName.text.toString())}
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun insertTimerName(newName: String) {
        if(newName.isEmpty()){
            createDialog(newName, "タイマー名が入力されていません。")
            return
        }
        if(newName.length > Constants.MAX_NAME_LENGTH){
            createDialog(newName, "タイマー名は${Constants.MAX_NAME_LENGTH}文字までです。")
            return
        }
        val currentTimerNameList = viewModel?.timerNamesList?.value
        if(currentTimerNameList != null) {
            if (currentTimerNameList.contains(newName)){
                createDialog(newName, "入力したタイマー名は使用されています。")
            }
            return
        }
        viewModel?.insertTimer(newName)
    }
}

