package com.waesh.timer.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.waesh.timer.application.TimerApplication
import com.waesh.timer.databinding.FragmentSaveDialogBinding
import com.waesh.timer.model.entity.TimerPreset
import com.waesh.timer.viewmodel.HomeViewModel
import com.waesh.timer.viewmodel.HomeViewModelFactory

class SaveEditDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSaveDialogBinding
    private val viewModel by activityViewModels<HomeViewModel> {
        HomeViewModelFactory((requireActivity().application as TimerApplication).repository)
    }

    private lateinit var presetToBeEdited: TimerPreset

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

        when (tag) {
            TAG_NEW_PRESET ->
                arguments?.let {
                    binding.apply {
                        hourPicker.value = it.getInt("HOUR")
                        minutePicker.value = it.getInt("MINUTE")
                        secondPicker.value = it.getInt("SECOND")
                    }
                }
            TAG_EDIT_PRESET -> {
                presetToBeEdited = viewModel.getFirstSelection()
                setPicker(presetToBeEdited.duration)
                binding.etPresetName.setText(presetToBeEdited.name)
                //TODO ringtone name
            }

        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnDone.setOnClickListener {
            when (tag){
                TAG_NEW_PRESET -> {
                    if (binding.etPresetName.text.toString().isNotEmpty()) {
                        viewModel.insertNew(
                            TimerPreset(
                                name = binding.etPresetName.text.toString(),
                                duration = getTimeMillisFromPickers()
                            )
                        )
                        dismiss()
                    } else showToast()
                }

                TAG_EDIT_PRESET -> {
                    if (binding.etPresetName.text.toString().isNotEmpty()){

                        val updatedPreset = TimerPreset(
                            name = binding.etPresetName.text.toString(),
                            duration = getTimeMillisFromPickers()
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

    private fun showToast(){
        Toast.makeText(
            requireActivity(),
            "Preset name cannot be empty.",
            Toast.LENGTH_SHORT
        ).show()
    }
    companion object{
        const val TAG_NEW_PRESET = "NEW_PRESET"
        const val TAG_EDIT_PRESET = "EDIT_PRESET"
    }
}