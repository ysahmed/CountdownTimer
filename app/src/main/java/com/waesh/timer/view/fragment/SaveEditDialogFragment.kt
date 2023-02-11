package com.waesh.timer.view.fragment

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.waesh.timer.application.TimerApplication
import com.waesh.timer.databinding.FragmentSaveDialogBinding
import com.waesh.timer.model.entity.TimerPreset
import com.waesh.timer.util.Constants
import com.waesh.timer.viewmodel.HomeViewModel
import com.waesh.timer.viewmodel.HomeViewModelFactory

class SaveEditDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSaveDialogBinding
    private val viewModel by activityViewModels<HomeViewModel> {
        HomeViewModelFactory((requireActivity().application as TimerApplication).repository)
    }

    private lateinit var presetToBeEdited: TimerPreset
    private var ringToneTitle: String? = null
    private var ringToneUri: Uri? = null
    private val ringtonePicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                it.data?.let { data ->
                    ringToneUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        data.getParcelableExtra(
                            RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                            Uri::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    }
                    ringToneTitle = RingtoneManager.getRingtone(requireActivity(), ringToneUri)
                        .getTitle(requireActivity())
                    binding.tvRingtoneName.text = ringToneTitle
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSaveDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize view & variables with value
        when (tag) {
            TAG_NEW_PRESET ->
                arguments?.let {
                    binding.apply {
                        hourPicker.value = it.getInt("HOUR")
                        minutePicker.value = it.getInt("MINUTE")
                        secondPicker.value = it.getInt("SECOND")
                        tvRingtoneName.text = Constants.DEFAULT_TONE_NAME
                    }
                }
            TAG_EDIT_PRESET -> {
                presetToBeEdited = viewModel.getFirstSelection()
                ringToneTitle = presetToBeEdited.ringTone_name
                ringToneUri = Uri.parse(presetToBeEdited.ringtone_uri)
                setPicker(presetToBeEdited.duration)
                binding.apply {
                    etPresetName.setText(presetToBeEdited.name)
                    tvRingtoneName.text = presetToBeEdited.ringTone_name
                }
            }

        }

        // pick ringtone
        binding.cvRingtonePicker.setOnClickListener {
            ringtonePicker.launch(
                Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                    putExtra(
                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        // tag cannot be anything else, right?
                        if (tag == TAG_EDIT_PRESET) ringToneUri else Uri.parse(Constants.DEFAULT_TONE_URI_STRING)
                    )
                }
            )
        }

        // cancel
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // done
        binding.btnDone.setOnClickListener {
            when (tag) {
                TAG_NEW_PRESET -> {
                    if (binding.etPresetName.text.toString().isNotEmpty()) {
                        viewModel.insertNew(
                            TimerPreset(
                                name = binding.etPresetName.text.toString(),
                                duration = getTimeMillisFromPickers(),
                                ringTone_name = ringToneTitle ?: Constants.DEFAULT_TONE_NAME,
                                ringtone_uri = if (ringToneUri == null) Constants.DEFAULT_TONE_URI_STRING else ringToneUri.toString()
                            )
                        )
                        dismiss()
                    } else showToast()
                }

                TAG_EDIT_PRESET -> {
                    if (binding.etPresetName.text.toString().isNotEmpty()) {

                        val updatedPreset = TimerPreset(
                            name = binding.etPresetName.text.toString(),
                            duration = getTimeMillisFromPickers(),
                            ringTone_name = ringToneTitle!!,
                            ringtone_uri = ringToneUri.toString()
                        ).apply {
                            id = presetToBeEdited.id
                        }

                        viewModel.update(updatedPreset)
                        dismiss()
                    } else showToast()
                }

            }
        }
    }

    private fun setPicker(millis: Long) {
        binding.hourPicker.value = ((millis / (1000 * 3600)) % 24).toInt()
        binding.minutePicker.value = ((millis / (1000 * 60)) % 60).toInt()
        binding.secondPicker.value = ((millis / 1000) % 60).toInt()
    }

    private fun getTimeMillisFromPickers(): Long {
        return (binding.hourPicker.value.toLong() * 3600 +
                binding.minutePicker.value.toLong() * 60 +
                binding.secondPicker.value.toLong()) * 1000
    }


    private fun showToast() {
        Toast.makeText(
            requireActivity(),
            "Preset name cannot be empty.",
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        const val TAG_NEW_PRESET = "NEW_PRESET"
        const val TAG_EDIT_PRESET = "EDIT_PRESET"

    }


}