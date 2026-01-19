package com.example.libro.ui.timer

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.libro.databinding.DialogTimePickerBinding

class TimePickerDialog(
    private val initialHours: Int = 0,
    private val initialMinutes: Int = 0,
    private val initialSeconds: Int = 0,
    private val onTimeSelected: (hours: Int, minutes: Int, seconds: Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogTimePickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTimePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.numberPickerHour.minValue = 0
        binding.numberPickerHour.maxValue = 23
        binding.numberPickerHour.value = initialHours

        binding.numberPickerMinute.minValue = 0
        binding.numberPickerMinute.maxValue = 59
        binding.numberPickerMinute.value = initialMinutes

        binding.numberPickerSecond.minValue = 0
        binding.numberPickerSecond.maxValue = 59
        binding.numberPickerSecond.value = initialSeconds

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TimePickerDialog"
    }
}