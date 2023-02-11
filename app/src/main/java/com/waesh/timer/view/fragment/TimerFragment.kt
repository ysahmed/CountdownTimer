package com.waesh.timer.view.fragment


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.waesh.timer.R
import com.waesh.timer.databinding.FragmentTimerBinding
import com.waesh.timer.service.TimerService


class TimerFragment : Fragment() {

    companion object {
        const val TAG = "TimerFragment"
    }

    private lateinit var binding: FragmentTimerBinding
    private lateinit var timerService: TimerService

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(service: ComponentName?, serviceBinder: IBinder?) {
            Log.i(TAG, "onServiceConnected: Bound!")
            timerService = (serviceBinder as TimerService.TimerServiceBinder).getTimerService()
            //timerService.startTimer()
            requireActivity().sendBroadcast(
                Intent(TimerService.ACTION_NOTIFICATION_BUTTON).apply {
                    putExtra(
                        TimerService.BROADCAST_INTENT_STRING_EXTRA,
                        TimerService.ACTION_TIMER_START
                    )
                }
            )
            initiateObservers()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            return
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: TimerFragmentArgs by navArgs()

        binding.progressBar.max = args.timeInMillis.toInt()

        // start service. also send duration and ringtone uri
        requireActivity().startForegroundService(
            Intent(requireActivity(), TimerService::class.java).apply {
                putExtra(TimerService.MILLIS_IN_FUTURE, args.timeInMillis)
                putExtra(TimerService.RINGTONE_URI, args.ringtoneUri)
            }
        )


        // then bind to the service anyway
        Intent(requireActivity(), TimerService::class.java).let {
            requireActivity().bindService(
                it, serviceConnection, Context.BIND_AUTO_CREATE
            )
        }

        setOnClickListener()

    }

    private fun initiateObservers() {
        timerService.formattedTimeInFuture.observe(viewLifecycleOwner) { formattedTimeInFuture ->
            binding.tvTimer.text = formattedTimeInFuture
            //Log.i(TAG, "initiateObservers: $formattedTimeInFuture")
        }

        timerService.ifTimerIsPaused.observe(viewLifecycleOwner) { ifTimerIsPaused ->
            if (ifTimerIsPaused) {
                binding.ivBtnPauseResume.setImageResource(R.drawable.btn_play)
            } else {
                binding.ivBtnPauseResume.setImageResource(R.drawable.btn_pause)
            }
        }

        timerService.isTimerStopped.observe(viewLifecycleOwner) { isTimerStopped ->
            if (isTimerStopped) {
                //Log.i(TAG, "initiateObservers: $isTimerStopped")
                //timerService.isTimerStopped.removeObservers(viewLifecycleOwner)
                unbindAndGoHome()
            }
        }

        timerService.millis.observe(viewLifecycleOwner) {
            binding.progressBar.progress = it.toInt()
        }
    }

    private fun setOnClickListener() {

        binding.ivBtnPauseResume.setOnClickListener {
            if (timerService.ifTimerIsPaused.value!!)
                requireActivity().sendBroadcast(
                    Intent(TimerService.ACTION_NOTIFICATION_BUTTON).apply {
                        putExtra(
                            TimerService.BROADCAST_INTENT_STRING_EXTRA,
                            TimerService.ACTION_TIMER_RESUME
                        )
                    }
                )
            else
                requireActivity().sendBroadcast(
                    Intent(TimerService.ACTION_NOTIFICATION_BUTTON).apply {
                        putExtra(
                            TimerService.BROADCAST_INTENT_STRING_EXTRA,
                            TimerService.ACTION_TIMER_PAUSE
                        )
                    }
                )
        }

        binding.ivBtnReset.setOnClickListener {
            requireActivity().sendBroadcast(
                Intent(TimerService.ACTION_NOTIFICATION_BUTTON).apply {
                    putExtra(
                        TimerService.BROADCAST_INTENT_STRING_EXTRA,
                        TimerService.ACTION_TIMER_STOP
                    )
                }
            )
        }
    }

    private fun unbindAndGoHome() {
        viewLifecycleOwnerLiveData.removeObservers(viewLifecycleOwner)
        requireActivity().unbindService(serviceConnection)
        findNavController().navigate(
            TimerFragmentDirections.actionTimerFragmentToHomeFragment()
        )
    }
}