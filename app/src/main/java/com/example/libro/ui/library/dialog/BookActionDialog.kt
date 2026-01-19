package com.example.libro.ui.library.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.libro.Database.Book
import com.example.libro.R

class BookActionDialog : DialogFragment() {

    private var onMarkAsReading: (() -> Unit)? = null
    private var onEditBook: (() -> Unit)? = null

    companion object {
        private const val ARG_BOOK = "book"

        fun newInstance(
            book: Book,
            onMarkAsReading: () -> Unit,
            onEditBook: () -> Unit
        ): BookActionDialog {
            val dialog = BookActionDialog()
            dialog.onMarkAsReading = onMarkAsReading
            dialog.onEditBook = onEditBook

            val args = Bundle()
            args.putSerializable(ARG_BOOK, book)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_book_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.markAsReadingButton).setOnClickListener {
            onMarkAsReading?.invoke()
            dismiss()
        }

        view.findViewById<View>(R.id.editBookButton).setOnClickListener {
            onEditBook?.invoke()
            dismiss()
        }

        view.findViewById<View>(R.id.cancelButton).setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}