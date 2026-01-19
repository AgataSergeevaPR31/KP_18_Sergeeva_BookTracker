package com.example.libro.ui.library

import androidx.lifecycle.*
import com.example.libro.Database.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val categoryDao: CategoryDao,
    private val bookCategoryDao: BookCategoryDao,
    private val genreDao: GenreDao,
    private val bookGenreDao: BookGenreDao
) : ViewModel() {

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books

    private val _filterCategories = MutableLiveData<List<String>>()
    val filterCategories: LiveData<List<String>> = _filterCategories

    private val _filterGenres = MutableLiveData<List<String>>()
    val filterGenres: LiveData<List<String>> = _filterGenres

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentCategoryFilter = "Все"
    private var currentGenreFilter = "Все жанры"

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            refreshBooks()

            val categories = categoryDao.getAllCategories()
            val categoryNames = listOf("Все") + categories.map { it.name }
            _filterCategories.value = categoryNames

            val genres = genreDao.getAllGenres()
            val genreNames = listOf("Все жанры") + genres.map { it.name }
            _filterGenres.value = genreNames

            _isLoading.value = false
        }
    }

    fun setFilters(category: String, genre: String) {
        currentCategoryFilter = category
        currentGenreFilter = genre
        refreshBooks()
    }

    suspend fun markBookAsReading(bookId: Long) {
        val book = bookDao.getBookById(bookId)

        if (book == null) {
            throw IllegalArgumentException("Книга с ID $bookId не найдена")
        }

        val updatedBook = book.copy(
            isRead = false,
            currentPage = if (book.currentPage == 0) 1 else book.currentPage,
            startDate = book.startDate ?: Date()
        )
        bookDao.updateBook(updatedBook)

        val readingCategory = categoryDao.getAllCategories()
            .find { it.name == "Читаю сейчас" }

        if (readingCategory != null) {
            bookCategoryDao.deleteBookCategoriesForBook(bookId)

            val bookCategory = BookCategory(
                bookId = bookId,
                categoryId = readingCategory.categoryId,
                addedDate = Date(),
                orderInCategory = 0,
                notes = "Прогресс: ${updatedBook.currentPage}/${updatedBook.pageCount} стр."
            )
            bookCategoryDao.insertBookCategory(bookCategory)
        }
    }

    fun refreshData() {
        refreshBooks()
    }

    private fun refreshBooks() {
        viewModelScope.launch {
            _isLoading.value = true

            val allBooks = bookDao.getAllBooks()

            val filteredBooks = if (currentCategoryFilter == "Все" && currentGenreFilter == "Все жанры") {
                allBooks
            } else {
                filterBooksByCategoryAndGenre(allBooks, currentCategoryFilter, currentGenreFilter)
            }

            _books.value = filteredBooks
            _isLoading.value = false
        }
    }

    private suspend fun filterBooksByCategoryAndGenre(
        allBooks: List<Book>,
        categoryName: String,
        genreName: String
    ): List<Book> {
        var booksByGenre = if (genreName != "Все жанры") {
            val genre = genreDao.getAllGenres().find { it.name == genreName }
            if (genre != null) {
                val bookGenres = bookGenreDao.getAllBookGenres()
                val bookIdsWithGenre = bookGenres
                    .filter { it.genreId == genre.genreId }
                    .map { it.bookId }
                    .toSet()

                allBooks.filter { bookIdsWithGenre.contains(it.bookId) }
            } else {
                allBooks
            }
        } else {
            allBooks
        }

        var filteredBooks = if (categoryName != "Все") {
            if (categoryName == "Читаю сейчас") {
                val readingCategory = categoryDao.getAllCategories()
                    .find { it.name == "Читаю сейчас" }
                if (readingCategory != null) {
                    val bookCategories = bookCategoryDao.getBookCategoriesByCategoryId(readingCategory.categoryId)
                    val bookIds = bookCategories.map { it.bookId }.toSet()
                    booksByGenre.filter { bookIds.contains(it.bookId) }
                } else {
                    emptyList()
                }
            } else {
                val category = categoryDao.getAllCategories().find { it.name == categoryName }
                if (category != null) {
                    val bookCategories = bookCategoryDao.getBookCategoriesByCategoryId(category.categoryId)
                    val bookIds = bookCategories.map { it.bookId }.toSet()
                    booksByGenre.filter { bookIds.contains(it.bookId) }
                } else {
                    emptyList()
                }
            }
        } else {
            booksByGenre
        }

        return filteredBooks
    }
}