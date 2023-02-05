package com.waesh.timer.view.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.waesh.timer.HomeViewModel
import com.waesh.timer.HomeViewModelFactory
import com.waesh.timer.application.TimerApplication
import com.waesh.timer.databinding.FragmentHomeBinding
import com.waesh.timer.service.TimerService
import com.waesh.timer.util.PrefKey
import com.waesh.timer.view.adapter.AdapterItemClickListener
import com.waesh.timer.view.adapter.PresetAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel by viewModels<HomeViewModel> {
        HomeViewModelFactory((requireActivity().application as TimerApplication).repository)
    }

    private val adapter = PresetAdapter(object : AdapterItemClickListener {
        override fun setPickerFromPreset(duration: Long) {
            Log.i("kkkCat", "setPicker: $duration")
            this@HomeFragment.setPicker(duration)
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

        val recyclerView = binding.rvPresets
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        viewModel.timerPresets.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

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
                    HomeFragmentDirections.actionHomeFragmentToTimerFragment(getTimeMillisFromPickers())
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.edit().putLong(PrefKey.PICKER_STATE_MILLIS, getTimeMillisFromPickers()).apply()
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
}