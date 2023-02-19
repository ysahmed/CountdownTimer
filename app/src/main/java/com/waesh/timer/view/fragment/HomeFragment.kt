package com.waesh.timer.view.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.waesh.timer.R
import com.waesh.timer.application.TimerApplication
import com.waesh.timer.databinding.FragmentHomeBinding
import com.waesh.timer.model.entity.TimerPreset
import com.waesh.timer.service.TimerService
import com.waesh.timer.util.Constants
import com.waesh.timer.util.PrefKey
import com.waesh.timer.view.adapter.AdapterItemClickListener
import com.waesh.timer.view.adapter.PresetAdapter
import com.waesh.timer.viewmodel.HomeViewModel
import com.waesh.timer.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView

    private var mainMenu: Menu? = null

    private var selectionAdapterPositions = mutableListOf<Int>()
    private var ringtoneUri = Constants.DEFAULT_TONE_URI_STRING

    private val viewModel by activityViewModels<HomeViewModel> {
        HomeViewModelFactory((requireActivity().application as TimerApplication).repository)
    }

    // Adapter initialization
    private val adapter = PresetAdapter(object : AdapterItemClickListener {
        override fun setTimer(duration: Long, ringtoneUri: String) {
            this@HomeFragment.setPicker(duration)
            this@HomeFragment.ringtoneUri = ringtoneUri
        }

        override fun addSelection(preset: TimerPreset, position: Int) {
            viewModel.addSelection(preset)
            selectionAdapterPositions.add(position)
            checkSelectionCount()
        }

        override fun removeSelection(preset: TimerPreset, position: Int) {
            viewModel.removeSelection(preset)
            selectionAdapterPositions.remove(position)
            checkSelectionCount()
        }

        private fun checkSelectionCount() {
            when (selectionAdapterPositions.size == 1) {
                true -> mainMenu?.findItem(R.id.action_edit)?.isVisible = true
                false -> mainMenu?.findItem(R.id.action_edit)?.isVisible = false
            }
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedPreferences =
            requireActivity().getSharedPreferences(PrefKey.PREFERENCES, Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPicker(sharedPreferences.getLong(PrefKey.PICKER_STATE_MILLIS, 5000))

        recyclerView = binding.rvPresets
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        viewModel.timerPresets.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            Log.i("HOME", "onViewCreated: UPDATE")
        }


        adapter.selectionMode.observe(viewLifecycleOwner) { selectionMode ->
            if (selectionMode) {
                // hide start button
                binding.ivStart.visibility = View.GONE
                // show edit delete buttons
                showMenu()

            } else {
                selectionAdapterPositions.forEach {
                    recyclerView.findViewHolderForAdapterPosition(it)?.let { holder ->
                        holder.itemView
                            .findViewById<ImageView>(R.id.iv_check)
                            .visibility = View.INVISIBLE
                    }
                }
                // empty selectionAdapterPositions
                selectionAdapterPositions = mutableListOf()
                // hide edit delete buttons
                hideMenu()
                // show start button
                binding.ivStart.visibility = View.VISIBLE
            }
        }

        setOnClickListener()
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.edit().putLong(PrefKey.PICKER_STATE_MILLIS, getTimeMillisFromPickers())
            .apply()
    }


    private fun setOnClickListener() {
        // start timer
        binding.ivStart.setOnClickListener {

            lifecycleScope.launch {
                requireActivity().sendBroadcast(
                    Intent(TimerService.ACTION_NOTIFICATION_BUTTON).apply {
                        putExtra(
                            TimerService.BROADCAST_INTENT_STRING_EXTRA,
                            TimerService.ACTION_SERVICE_DISMISS
                        )
                    }
                )
                delay(50)

                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToTimerFragment(
                        getTimeMillisFromPickers(),
                        ringtoneUri
                    )
                )
            }
        }

        // new preset
        binding.tvAdd.setOnClickListener {
            SaveEditDialogFragment().apply {
                val bundle = Bundle().apply {
                    putInt("HOUR", binding.hourPicker.value)
                    putInt("MINUTE", binding.minutePicker.value)
                    putInt("SECOND", binding.secondPicker.value)
                }
                arguments = bundle
            }.show(requireActivity().supportFragmentManager,
                SaveEditDialogFragment.TAG_NEW_PRESET
                )
        }
    }

    private fun getTimeMillisFromPickers(): Long {
        return (binding.hourPicker.value.toLong() * 3600 +
                binding.minutePicker.value.toLong() * 60 +
                binding.secondPicker.value.toLong()) * 1000
    }

    private fun setPicker(millis: Long) {
        binding.hourPicker.value = ((millis / (1000 * 3600)) % 24).toInt()
        binding.minutePicker.value = ((millis / (1000 * 60)) % 60).toInt()
        binding.secondPicker.value = ((millis / 1000) % 60).toInt()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        mainMenu = menu
        menuInflater.inflate(R.menu.edit_delete_menu, menu)
        hideMenu()
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_delete -> {
                viewModel.deleteSelections()
                adapter.cancelSelectionMode()
                selectionAdapterPositions = mutableListOf()
                true
            }
            R.id.action_edit -> {
                SaveEditDialogFragment()
                    .show(
                        requireActivity().supportFragmentManager,
                        SaveEditDialogFragment.TAG_EDIT_PRESET
                    )
                adapter.cancelSelectionMode()
                true
            }
            R.id.action_cancel -> {
                adapter.cancelSelectionMode()
                true
            }
            else -> false
        }
    }

    private fun hideMenu() {
        mainMenu?.findItem(R.id.action_delete)?.isVisible = false
        mainMenu?.findItem(R.id.action_edit)?.isVisible = false
        mainMenu?.findItem(R.id.action_cancel)?.isVisible = false
    }

    private fun showMenu() {
        mainMenu?.findItem(R.id.action_delete)?.isVisible = true
        mainMenu?.findItem(R.id.action_edit)?.isVisible = true
        mainMenu?.findItem(R.id.action_cancel)?.isVisible = true
    }
}