package com.example.libro.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.libro.R
import com.example.libro.databinding.DialogCurrentPageBinding

class CurrentPageDialog(
    private val totalPages: Int,
    private val currentPage: Int = 0,
    private val onPageSelected: (Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogCurrentPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCurrentPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTotalPages.text = getString(R.string.of_pages, totalPages)

        binding.numberPicker.minValue = 0
        binding.numberPicker.maxValue = totalPages
        binding.numberPicker.value = currentPage

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CurrentPageDialog"
    }
}