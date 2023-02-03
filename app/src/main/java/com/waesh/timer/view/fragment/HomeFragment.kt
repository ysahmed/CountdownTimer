package com.waesh.timer.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.waesh.timer.databinding.FragmentHomeBinding
import com.waesh.timer.service.TimerService

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivStart.setOnClickListener {
            requireActivity().sendBroadcast(
                Intent(TimerService.ACTION_NOTIFICATION_BUTTON).apply {
                    putExtra(
                        TimerService.BROADCAST_INTENT_STRING_EXTRA,
                        TimerService.ACTION_SERVICE_DISMISS
                    )
                }
            )
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToTimerFragment(getTimeMillis())
            )
        }
    }

    private fun getTimeMillis(): Long {
        return (binding.hourPicker.value.toLong() * 3600 +
                binding.minutePicker.value.toLong() * 60 +
                binding.secondPicker.value.toLong()) * 1000
    }
}