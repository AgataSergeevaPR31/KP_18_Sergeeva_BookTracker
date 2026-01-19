package com.example.libro.ui.library

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.libro.databinding.FragmentLibraryBinding
import com.example.libro.ui.library.adapter.BookAdapter
import com.example.libro.ui.library.dialog.BookActionDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var adapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupFab()
        setupSearchButton()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    private fun setupRecyclerView() {
        adapter = BookAdapter(
            onItemClick = { book ->
                showBookActionDialog(book)
            }
        )

        val layoutManager = GridLayoutManager(requireContext(), 3)
        binding.booksRecyclerView.layoutManager = layoutManager
        binding.booksRecyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.books.observe(viewLifecycleOwner) { books ->
            adapter.submitList(books)
            updateBookCount(books.size)
        }

        viewModel.filterCategories.observe(viewLifecycleOwner) { categories ->
            setupCategoryFilter(categories)
        }

        viewModel.filterGenres.observe(viewLifecycleOwner) { genres ->
            setupGenreFilter(genres)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.booksRecyclerView.visibility = View.GONE
            } else {
                binding.booksRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupCategoryFilter(categories: List<String>) {
        if (categories.isNotEmpty()) {
            val categoryAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
            )
            binding.categoryFilter.setAdapter(categoryAdapter)

            binding.categoryFilter.setText(categories[0], false)

            binding.categoryFilter.setOnItemClickListener { _, _, position, _ ->
                val selectedCategory = categories[position]
                val selectedGenre = binding.genreFilter.text.toString()
                viewModel.setFilters(selectedCategory, selectedGenre)
            }
        }
    }

    private fun setupGenreFilter(genres: List<String>) {
        if (genres.isNotEmpty()) {
            val genreAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                genres
            )
            binding.genreFilter.setAdapter(genreAdapter)

            binding.genreFilter.setText(genres[0], false)

            binding.genreFilter.setOnItemClickListener { _, _, position, _ ->
                val selectedCategory = binding.categoryFilter.text.toString()
                val selectedGenre = genres[position]
                viewModel.setFilters(selectedCategory, selectedGenre)
            }
        }
    }

    private fun setupFab() {
        binding.addBookFab.setOnClickListener {
            openAddBookActivity(null)
        }
    }

    private fun setupSearchButton() {
        binding.searchButton.setOnClickListener {
            showSearchDialog()
        }
    }

    private fun updateBookCount(count: Int) {
        binding.bookCountTextView.text =
            if (count == 0) "В вашей библиотеке нет книг"
            else "В вашей библиотеке $count ${getBookWord(count)}"
    }

    private fun getBookWord(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "книга"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "книги"
            else -> "книг"
        }
    }

    private fun showBookActionDialog(book: com.example.libro.Database.Book) {
        val dialog = BookActionDialog.newInstance(
            book = book,
            onMarkAsReading = {
                markBookAsReading(book)
            },
            onEditBook = {
                openAddBookActivity(book)
            }
        )
        dialog.show(childFragmentManager, "book_action_dialog")
    }

    private fun markBookAsReading(book: com.example.libro.Database.Book) {
        lifecycleScope.launch {
            viewModel.markBookAsReading(book.bookId)

            android.widget.Toast.makeText(
                requireContext(),
                "Книга \"${book.title}\" отмечена как читаемая",
                android.widget.Toast.LENGTH_SHORT
            ).show()

            viewModel.refreshData()
        }
    }

    private fun openAddBookActivity(book: com.example.libro.Database.Book?) {
        val intent = Intent(requireContext(), AddBookActivity::class.java)
        if (book != null) {
            intent.putExtra("book_id", book.bookId)
            intent.putExtra("is_editing", true)
        }
        startActivity(intent)
    }

    private fun showSearchDialog() {
        android.widget.Toast.makeText(
            requireContext(),
            "Поиск книг будет реализован позже",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}