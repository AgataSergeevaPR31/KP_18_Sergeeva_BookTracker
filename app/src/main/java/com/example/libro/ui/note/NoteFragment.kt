package com.example.libro.ui.notes

import NotesAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.libro.Database.AppDatabase
import com.example.libro.Database.Note
import com.example.libro.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class NoteFragment : Fragment() {

    @Inject
    lateinit var database: AppDatabase

    private lateinit var titleTextView: TextView
    private lateinit var favoriteFilterButton: ImageButton
    private lateinit var notesCountTextView: TextView
    private lateinit var sortSpinner: Spinner
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView

    private lateinit var notesAdapter: NotesAdapter
    private var allNotes: List<Note> = emptyList()
    private var filteredNotes: List<Note> = emptyList()
    private var isFavoriteFilterActive = false
    private var currentSortOption = SortOption.ALL

    private enum class SortOption {
        ALL, DATE_DESC, DATE_ASC, PAGE_ASC, PAGE_DESC
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupAdapters()
        setupClickListeners()
        loadNotes()
    }

    private fun initViews(view: View) {
        titleTextView = view.findViewById(R.id.titleTextView)
        favoriteFilterButton = view.findViewById(R.id.favoriteFilterButton)
        notesCountTextView = view.findViewById(R.id.notesCountTextView)
        sortSpinner = view.findViewById(R.id.sortSpinner)
        notesRecyclerView = view.findViewById(R.id.notesRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyTextView = view.findViewById(R.id.emptyTextView)
    }

    private fun setupAdapters() {
        notesAdapter = NotesAdapter(
            requireContext(),
            emptyList(),
            emptyMap(),
            onFavoriteClick = { note -> toggleFavorite(note) },
            onDeleteClick = { note -> deleteNote(note) }
        )

        notesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notesAdapter
            setHasFixedSize(true)
        }

        val sortOptions = arrayOf(
            "Все",
            "По дате (новые)",
            "По дате (старые)",
            "По странице (по возрастанию)",
            "По странице (по убыванию)"
        )

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sortOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = spinnerAdapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSortOption = when (position) {
                    0 -> SortOption.ALL
                    1 -> SortOption.DATE_DESC
                    2 -> SortOption.DATE_ASC
                    3 -> SortOption.PAGE_ASC
                    4 -> SortOption.PAGE_DESC
                    else -> SortOption.ALL
                }
                applyFiltersAndSort()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupClickListeners() {
        favoriteFilterButton.setOnClickListener {
            isFavoriteFilterActive = !isFavoriteFilterActive
            updateFavoriteFilterButton()
            applyFiltersAndSort()
        }
    }

    private fun updateFavoriteFilterButton() {
        if (isFavoriteFilterActive) {
            favoriteFilterButton.setImageResource(R.drawable.heart)
            favoriteFilterButton.setColorFilter(requireContext().getColor(R.color.elements))
        } else {
            favoriteFilterButton.setImageResource(R.drawable.heartempty)
            favoriteFilterButton.setColorFilter(requireContext().getColor(R.color.text_secondary))
        }
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE

            try {
                val notes = withContext(Dispatchers.IO) {
                    database.noteDao().getAllNotes()
                }

                val bookMap = mutableMapOf<Long, Pair<String, String>>()
                val coverImageMap = mutableMapOf<Long, String?>()

                notes.forEach { note ->
                    if (!bookMap.containsKey(note.bookId)) {
                        val book = withContext(Dispatchers.IO) {
                            database.bookDao().getBookById(note.bookId)
                        }
                        book?.let {
                            bookMap[note.bookId] = Pair(it.title, it.author ?: "Автор не указан")
                            coverImageMap[note.bookId] = it.coverImage
                        } ?: run {
                            bookMap[note.bookId] = Pair("Неизвестная книга", "")
                            coverImageMap[note.bookId] = null
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    allNotes = notes
                    notesAdapter.updateBookMap(bookMap, coverImageMap)
                    applyFiltersAndSort()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка загрузки заметок", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun applyFiltersAndSort() {
        var result = allNotes

        if (isFavoriteFilterActive) {
            result = result.filter { it.isFavorite }
        }

        filteredNotes = when (currentSortOption) {
            SortOption.ALL -> result
            SortOption.DATE_DESC -> result.sortedByDescending { it.creationDate }
            SortOption.DATE_ASC -> result.sortedBy { it.creationDate }
            SortOption.PAGE_ASC -> result.sortedBy { it.pageNumber ?: 0 }
            SortOption.PAGE_DESC -> result.sortedByDescending { it.pageNumber ?: 0 }
        }
        notesAdapter.updateNotes(filteredNotes)

        updateUI()
    }

    private fun updateUI() {
        val totalNotes = allNotes.size
        val filteredCount = filteredNotes.size

        notesCountTextView.text = if (isFavoriteFilterActive) {
            "Избранные: $filteredCount из $totalNotes заметок"
        } else {
            "Всего: $filteredCount заметок"
        }

        if (filteredNotes.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
            notesRecyclerView.visibility = View.GONE

            emptyTextView.text = when {
                isFavoriteFilterActive && allNotes.isEmpty() -> "Нет заметок. Создайте первую заметку!"
                isFavoriteFilterActive -> "Нет избранных заметок"
                else -> "Нет заметок. Создайте первую заметку!"
            }
        } else {
            emptyTextView.visibility = View.GONE
            notesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun toggleFavorite(note: Note) {
        lifecycleScope.launch {
            try {
                val updatedNote = note.copy(isFavorite = !note.isFavorite)
                withContext(Dispatchers.IO) {
                    database.noteDao().updateNote(updatedNote)
                }

                val index = allNotes.indexOfFirst { it.noteId == note.noteId }
                if (index != -1) {
                    allNotes = allNotes.toMutableList().apply {
                        this[index] = updatedNote
                    }
                }

                applyFiltersAndSort()

                val message = if (updatedNote.isFavorite) {
                    "Заметка добавлена в избранное"
                } else {
                    "Заметка убрана из избранного"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка обновления заметки", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteNote(note: Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление заметки")
            .setMessage("Вы уверены, что хотите удалить эту заметку?")
            .setPositiveButton("Удалить") { dialog, _ ->
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            database.noteDao().deleteNote(note)
                        }

                        allNotes = allNotes.filterNot { it.noteId == note.noteId }
                        applyFiltersAndSort()

                        Toast.makeText(context, "Заметка удалена", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Ошибка удаления заметки", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }
}