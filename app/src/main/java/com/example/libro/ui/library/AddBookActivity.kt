package com.example.libro.ui.library

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.libro.Database.*
import com.example.libro.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddBookActivity : AppCompatActivity() {

    @Inject
    lateinit var database: AppDatabase

    private lateinit var titleEditText: EditText
    private lateinit var authorEditText: EditText
    private lateinit var pageCountEditText: EditText
    private lateinit var publisherEditText: EditText
    private lateinit var publicationYearEditText: EditText
    private lateinit var publicationMonthEditText: EditText
    private lateinit var genreAutoCompleteTextView: AutoCompleteTextView
    private lateinit var bookCoverImageView: ImageView
    private lateinit var saveButton: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var ratingText: TextView

    private lateinit var purchasedRadioButton: RadioButton
    private lateinit var lentRadioButton: RadioButton
    private lateinit var otherRadioButton: RadioButton

    private lateinit var purchaseDateEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var purchaseLocationEditText: EditText
    private lateinit var purchaseNotesEditText: EditText
    private lateinit var purchasedFieldsLayout: LinearLayout

    private lateinit var borrowDateEditText: EditText
    private lateinit var expectedReturnDateEditText: EditText
    private lateinit var actualReturnDateEditText: EditText
    private lateinit var borrowedByEditText: EditText
    private lateinit var returnedCheckBox: CheckBox
    private lateinit var actualReturnDateLayout: LinearLayout
    private lateinit var lentFieldsLayout: LinearLayout

    private var currentPhotoPath: String? = null
    private var currentBookId: Long = -1
    private var isEditing = false
    private var selectedGenres = mutableListOf<String>()
    private var genreAdapter: ArrayAdapter<String>? = null

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            showPermissionDeniedDialog("Для добавления фотографии с камеры необходимо разрешение на использование камеры")
        }
    }

    private val requestGalleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openGallery()
        } else {
            showPermissionDeniedDialog("Для выбора фотографии из галереи необходимо разрешение на доступ к хранилищу")
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let { path ->
                val imageFile = File(path)
                if (imageFile.exists()) {
                    bookCoverImageView.setImageURI(Uri.fromFile(imageFile))
                }
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                bookCoverImageView.setImageURI(uri)
                currentPhotoPath = getRealPathFromURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        currentBookId = intent.getLongExtra("book_id", -1)
        isEditing = currentBookId != -1L

        setupViews()
        setupClickListeners()
        setupDatePickers()
        setupRatingBar()

        lifecycleScope.launch {
            loadInitialData()
        }
    }

    private suspend fun loadInitialData() {
        try {
            loadGenres()

            if (isEditing) {
                runOnUiThread {
                    saveButton.text = "Сохранить изменения"
                }
                loadBookData()
            } else {
                runOnUiThread {
                    saveButton.text = "Добавить книгу"
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this@AddBookActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViews() {
        titleEditText = findViewById(R.id.titleEditText)
        authorEditText = findViewById(R.id.authorEditText)
        pageCountEditText = findViewById(R.id.pageCountEditText)
        publisherEditText = findViewById(R.id.publisherEditText)
        publicationYearEditText = findViewById(R.id.publicationYearEditText)
        publicationMonthEditText = findViewById(R.id.publicationMonthEditText)
        genreAutoCompleteTextView = findViewById(R.id.genreAutoCompleteTextView)
        bookCoverImageView = findViewById(R.id.bookCoverImageView)
        saveButton = findViewById(R.id.saveButton)
        ratingBar = findViewById(R.id.ratingBar)
        ratingText = findViewById(R.id.ratingText)

        purchasedRadioButton = findViewById(R.id.purchasedRadioButton)
        lentRadioButton = findViewById(R.id.lentRadioButton)
        otherRadioButton = findViewById(R.id.otherRadioButton)

        purchaseDateEditText = findViewById(R.id.purchaseDateEditText)
        priceEditText = findViewById(R.id.priceEditText)
        purchaseLocationEditText = findViewById(R.id.purchaseLocationEditText)
        purchaseNotesEditText = findViewById(R.id.purchaseNotesEditText)
        purchasedFieldsLayout = findViewById(R.id.purchasedFieldsLayout)

        borrowDateEditText = findViewById(R.id.borrowDateEditText)
        expectedReturnDateEditText = findViewById(R.id.expectedReturnDateEditText)
        actualReturnDateEditText = findViewById(R.id.actualReturnDateEditText)
        borrowedByEditText = findViewById(R.id.borrowedByEditText)
        returnedCheckBox = findViewById(R.id.returnedCheckBox)
        actualReturnDateLayout = findViewById(R.id.actualReturnDateLayout)
        lentFieldsLayout = findViewById(R.id.lentFieldsLayout)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun setupClickListeners() {
        bookCoverImageView.setOnClickListener {
            showImagePickerDialog()
        }

        saveButton.setOnClickListener {
            saveBook()
        }

        findViewById<RadioGroup>(R.id.bookTypeRadioGroup).setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.purchasedRadioButton -> {
                    purchasedFieldsLayout.visibility = View.VISIBLE
                    lentFieldsLayout.visibility = View.GONE
                }
                R.id.lentRadioButton -> {
                    purchasedFieldsLayout.visibility = View.GONE
                    lentFieldsLayout.visibility = View.VISIBLE
                }
                R.id.otherRadioButton -> {
                    purchasedFieldsLayout.visibility = View.GONE
                    lentFieldsLayout.visibility = View.GONE
                }
            }
        }

        returnedCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            actualReturnDateLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun setupRatingBar() {
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            ratingText.text = String.format(Locale.getDefault(), "%.1f", rating)
        }
    }

    private suspend fun loadGenres() {
        try {
            val genres = database.genreDao().getAllGenres()
            val genreNames = genres.map { it.name }

            runOnUiThread {
                genreAdapter = ArrayAdapter(
                    this@AddBookActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    genreNames
                )
                genreAutoCompleteTextView.setAdapter(genreAdapter)

                genreAutoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
                    val selectedGenre = genreAdapter?.getItem(position)
                    selectedGenre?.let {
                        if (!selectedGenres.contains(it)) {
                            selectedGenres.add(it)
                            updateGenreText()
                        }
                    }
                    genreAutoCompleteTextView.setText("")
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun updateGenreText() {
        genreAutoCompleteTextView.hint = if (selectedGenres.isNotEmpty()) {
            selectedGenres.joinToString(", ")
        } else {
            "Жанр (необязательно)"
        }
    }

    private fun setupDatePickers() {
        val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        purchaseDateEditText.setOnClickListener {
            showDatePickerDialog(purchaseDateEditText, dateFormatter)
        }

        borrowDateEditText.setOnClickListener {
            showDatePickerDialog(borrowDateEditText, dateFormatter)
        }

        expectedReturnDateEditText.setOnClickListener {
            showDatePickerDialog(expectedReturnDateEditText, dateFormatter)
        }

        actualReturnDateEditText.setOnClickListener {
            showDatePickerDialog(actualReturnDateEditText, dateFormatter)
        }
    }

    private fun showDatePickerDialog(editText: EditText, dateFormatter: SimpleDateFormat) {
        val calendar = Calendar.getInstance()

        val currentDate = editText.text.toString()
        if (currentDate.isNotEmpty()) {
            try {
                val parsedDate = dateFormatter.parse(currentDate)
                parsedDate?.let {
                    calendar.time = it
                }
            } catch (e: Exception) {
            }
        }

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                editText.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Сделать фото", "Выбрать из галереи", "Отмена")

        MaterialAlertDialogBuilder(this)
            .setTitle("Выберите обложку")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> checkGalleryPermission()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionRationaleDialog(
                    "Разрешение на использование камеры",
                    "Для добавления фотографии с камеры необходимо предоставить разрешение"
                ) {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }

            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                showPermissionRationaleDialog(
                    "Разрешение на доступ к галерее",
                    "Для выбора фотографии из галереи необходимо предоставить разрешение"
                ) {
                    requestGalleryPermissionLauncher.launch(permission)
                }
            }

            else -> {
                requestGalleryPermissionLauncher.launch(permission)
            }
        }
    }

    private fun showPermissionRationaleDialog(title: String, message: String, onRequestPermission: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Разрешить") { dialog, which ->
                onRequestPermission()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showPermissionDeniedDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Разрешение не предоставлено")
            .setMessage("$message\n\nХотите перейти в настройки приложения, чтобы разрешить доступ?")
            .setPositiveButton("Настройки") { dialog, which ->
                openAppSettings()
            }
            .setNegativeButton("Позже", null)
            .show()
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось открыть настройки", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = createImageFile()
            photoFile?.let {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                cameraLauncher.launch(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось открыть камеру", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось открыть галерею", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File? {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            ).apply {
                currentPhotoPath = absolutePath
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось создать файл изображения", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uri.toString()
            } else {
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        cursor.getString(columnIndex)
                    } else null
                }
            }
        } catch (e: Exception) {
            uri.toString()
        }
    }

    private suspend fun loadBookData() {
        try {
            val book = database.bookDao().getBookById(currentBookId)
            book?.let {
                val lentBook = database.lentBookDao().getLentBookByBookId(currentBookId)
                val purchasedBook = database.purchasedBookDao().getPurchasedBookByBookId(currentBookId)

                runOnUiThread {
                    titleEditText.setText(it.title)
                    authorEditText.setText(it.author ?: "")
                    pageCountEditText.setText(it.pageCount.toString())
                    publisherEditText.setText(it.publisher ?: "")
                    publicationYearEditText.setText(it.publicationYear?.toString() ?: "")
                    publicationMonthEditText.setText(it.publicationMonth?.toString() ?: "")

                    it.coverImage?.let { imagePath ->
                        if (imagePath.isNotEmpty()) {
                            Glide.with(this@AddBookActivity)
                                .load(imagePath)
                                .placeholder(R.drawable.plug)
                                .into(bookCoverImageView)
                            currentPhotoPath = imagePath
                        }
                    }

                    it.rating?.let { rating ->
                        ratingBar.rating = rating
                        ratingText.text = String.format(Locale.getDefault(), "%.1f", rating)
                    }

                    when {
                        purchasedBook != null -> {
                            purchasedRadioButton.isChecked = true
                            purchasedFieldsLayout.visibility = View.VISIBLE
                        }
                        lentBook != null -> {
                            lentRadioButton.isChecked = true
                            lentFieldsLayout.visibility = View.VISIBLE

                            lentBook.borrowedBy?.let { borrowedBy ->
                                if (borrowedBy.isNotEmpty()) {
                                    borrowedByEditText.setText(borrowedBy)
                                }
                            }

                            if (lentBook.actualReturnDate != null) {
                                returnedCheckBox.isChecked = true
                                actualReturnDateLayout.visibility = View.VISIBLE
                            }
                        }
                        else -> {
                            otherRadioButton.isChecked = true
                        }
                    }
                }

                if (purchasedBook != null) {
                    loadPurchasedBookData()
                }
                if (lentBook != null) {
                    loadLentBookData()
                }
            }

            loadBookGenres()

        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this@AddBookActivity, "Ошибка загрузки данных книги", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun loadPurchasedBookData() {
        try {
            val purchasedBook = database.purchasedBookDao().getPurchasedBookByBookId(currentBookId)
            purchasedBook?.let {
                val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                runOnUiThread {
                    purchaseDateEditText.setText(dateFormatter.format(it.purchaseDate))
                    priceEditText.setText(it.price?.toString() ?: "")
                    purchaseLocationEditText.setText(it.purchaseLocation ?: "")
                    purchaseNotesEditText.setText(it.note ?: "")
                }
            }
        } catch (e: Exception) {
        }
    }

    private suspend fun loadLentBookData() {
        try {
            val lentBook = database.lentBookDao().getLentBookByBookId(currentBookId)
            lentBook?.let {
                val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                runOnUiThread {
                    borrowedByEditText.setText(it.borrowedBy ?: "")
                    borrowDateEditText.setText(it.borrowDate?.let { date -> dateFormatter.format(date) } ?: "")
                    expectedReturnDateEditText.setText(it.expectedReturnDate?.let { date -> dateFormatter.format(date) } ?: "")

                    if (it.actualReturnDate != null) {
                        returnedCheckBox.isChecked = true
                        actualReturnDateLayout.visibility = View.VISIBLE
                        actualReturnDateEditText.setText(dateFormatter.format(it.actualReturnDate))

                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadBookGenres() {
        try {
            val bookGenres = database.bookGenreDao().getGenresForBook(currentBookId)
            selectedGenres.clear()

            bookGenres.forEach { bookGenre ->
                val genre = database.genreDao().getGenreById(bookGenre.genreId)
                genre?.let {
                    selectedGenres.add(it.name)
                }
            }

            runOnUiThread {
                updateGenreText()
            }
        } catch (e: Exception) {
        }
    }

    private fun saveBook() {
        if (!validateInput()) {
            return
        }

        lifecycleScope.launch {
            try {
                if (isEditing) {
                    updateExistingBook()
                } else {
                    createNewBook()
                }

                runOnUiThread {
                    val message = if (isEditing) "Изменения сохранены!" else "Книга успешно добавлена!"
                    Toast.makeText(this@AddBookActivity, message, Toast.LENGTH_SHORT).show()

                    setResult(Activity.RESULT_OK)
                    finish()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    val message = if (isEditing) "Ошибка при сохранении изменений" else "Ошибка при добавлении книги"
                    Toast.makeText(this@AddBookActivity, "$message: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun updateExistingBook() {
        val existingBook = database.bookDao().getBookById(currentBookId)
        if (existingBook == null) {
            throw Exception("Книга не найдена")
        }

        val isPurchased = purchasedRadioButton.isChecked
        val isLent = lentRadioButton.isChecked
        val returned = returnedCheckBox.isChecked

        val updatedBook = createBookFromInput().copy(
            bookId = currentBookId,
            isRead = existingBook.isRead,
            currentPage = existingBook.currentPage,
            totalReadingTime = existingBook.totalReadingTime,
            startDate = existingBook.startDate,
            isPurchased = isPurchased,
            isLent = if (isLent && !returned) true else false
        )

        database.bookDao().updateBook(updatedBook)

        updatePurchaseOrLentInfo()
        updateBookGenres()
    }

    private suspend fun updatePurchaseOrLentInfo() {
        database.purchasedBookDao().deletePurchasedBookByBookId(currentBookId)
        database.lentBookDao().deleteLentBookByBookId(currentBookId)

        when {
            purchasedRadioButton.isChecked -> savePurchasedBook(currentBookId)
            lentRadioButton.isChecked && !returnedCheckBox.isChecked -> saveLentBook(currentBookId)
        }
    }

    private suspend fun updateBookGenres() {
        database.bookGenreDao().deleteGenresForBook(currentBookId)
        saveGenres(currentBookId)
    }

    private suspend fun createNewBook(): Long {
        val book = createBookFromInput()
        val bookId = database.bookDao().insertBook(book)

        when {
            purchasedRadioButton.isChecked -> savePurchasedBook(bookId)
            lentRadioButton.isChecked -> saveLentBook(bookId) // Убрать проверку на returnedCheckBox
        }

        saveGenres(bookId)
        addToDefaultCategory(bookId)

        return bookId
    }

    private suspend fun savePurchasedBook(bookId: Long) {
        try {
            if (purchasedRadioButton.isChecked && purchaseDateEditText.text.isNotEmpty()) {
                val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                val purchasedBook = priceEditText.text.toString().toDoubleOrNull()?.let {
                    PurchasedBook(
                        bookId = bookId,
                        purchaseDate = dateFormatter.parse(purchaseDateEditText.text.toString()) ?: Date(),
                        price = it,
                        purchaseLocation = purchaseLocationEditText.text.toString().trim().takeIf { it.isNotEmpty() },
                        note = purchaseNotesEditText.text.toString().trim().takeIf { it.isNotEmpty() }
                    )
                }

                if (purchasedBook != null) {
                    database.purchasedBookDao().insertPurchasedBook(purchasedBook)
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun saveLentBook(bookId: Long) {
        try {
            if (lentRadioButton.isChecked) {
                val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                val lentBook = LentBook(
                    bookId = bookId,
                    borrowedBy = borrowedByEditText.text.toString().trim(),
                    borrowDate = dateFormatter.parse(borrowDateEditText.text.toString()) ?: Date(),
                    expectedReturnDate = dateFormatter.parse(expectedReturnDateEditText.text.toString()),
                    actualReturnDate = if (returnedCheckBox.isChecked && actualReturnDateEditText.text.isNotEmpty()) {
                        dateFormatter.parse(actualReturnDateEditText.text.toString())
                    } else null
                )

                database.lentBookDao().insertLentBook(lentBook)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun saveGenres(bookId: Long) {
        try {
            selectedGenres.forEach { genreName ->
                val genres = database.genreDao().getAllGenres()
                val genre = genres.find { it.name == genreName }

                if (genre != null) {
                    val bookGenre = BookGenre(bookId = bookId, genreId = genre.genreId)
                    database.bookGenreDao().insertBookGenre(bookGenre)
                } else {
                    val newGenre = Genre(name = genreName)
                    val newGenreId = database.genreDao().insertGenre(newGenre)

                    val bookGenre = BookGenre(bookId = bookId, genreId = newGenreId)
                    database.bookGenreDao().insertBookGenre(bookGenre)
                }
            }
        } catch (e: Exception) {
        }
    }

    private suspend fun addToDefaultCategory(bookId: Long) {
        try {
            val existingCategories = database.bookCategoryDao().getCategoriesForBook(bookId)
            if (existingCategories.isEmpty()) {
                val categories = database.categoryDao().getAllCategories()
                val wantToReadCategory = categories.find { it.name == "Хочу прочитать" }

                wantToReadCategory?.let { category ->
                    val bookCategory = BookCategory(
                        bookId = bookId,
                        categoryId = category.categoryId,
                        addedDate = Date(),
                        orderInCategory = 0,
                        notes = "Добавлено в библиотеку"
                    )
                    database.bookCategoryDao().insertBookCategory(bookCategory)
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun createBookFromInput(): Book {
        val rating = if (ratingBar.rating > 0) ratingBar.rating else null
        val isPurchased = purchasedRadioButton.isChecked
        val isLent = lentRadioButton.isChecked && !returnedCheckBox.isChecked

        return Book(
            title = titleEditText.text.toString().trim(),
            author = authorEditText.text.toString().trim().takeIf { it.isNotEmpty() },
            pageCount = pageCountEditText.text.toString().toInt(),
            isRead = false,
            currentPage = 0,
            totalReadingTime = 0,
            publisher = publisherEditText.text.toString().trim().takeIf { it.isNotEmpty() },
            publicationYear = publicationYearEditText.text.toString().trim().toIntOrNull(),
            publicationMonth = publicationMonthEditText.text.toString().trim().toIntOrNull(),
            coverImage = currentPhotoPath,
            bookType = Book.BookType.PRINTED,
            rating = rating,
            isPurchased = isPurchased,
            isLent = isLent
        )
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (titleEditText.text.toString().trim().isEmpty()) {
            (titleEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error =
                "Введите название книги"
            isValid = false
        } else {
            (titleEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error = null
        }

        if (pageCountEditText.text.toString().trim().isEmpty()) {
            (pageCountEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error =
                "Введите количество страниц"
            isValid = false
        } else {
            try {
                val pages = pageCountEditText.text.toString().toInt()
                if (pages <= 0) {
                    (pageCountEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error =
                        "Количество страниц должно быть больше 0"
                    isValid = false
                } else {
                    (pageCountEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error = null
                }
            } catch (e: NumberFormatException) {
                (pageCountEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error =
                    "Введите корректное число"
                isValid = false
            }
        }

        if (purchasedRadioButton.isChecked) {
            if (purchaseDateEditText.text.toString().trim().isEmpty()) {
                (purchaseDateEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error =
                    "Введите дату покупки"
                isValid = false
            } else {
                (purchaseDateEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error = null
            }

            if (priceEditText.text.toString().trim().isEmpty()) {
                (priceEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error =
                    "Введите цену"
                isValid = false
            } else {
                try {
                    val price = priceEditText.text.toString().toDoubleOrNull()
                    if (price == null || price < 0) {
                        (priceEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error =
                            "Введите корректную цену"
                        isValid = false
                    } else {
                        (priceEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error = null
                    }
                } catch (e: NumberFormatException) {
                    (priceEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error =
                        "Введите корректную цену"
                    isValid = false
                }
            }
        }

        if (lentRadioButton.isChecked) {
            if (borrowDateEditText.text.toString().trim().isEmpty()) {
                (borrowDateEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error =
                    "Введите дату взятия"
                isValid = false
            } else {
                (borrowDateEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error = null
            }

            if (expectedReturnDateEditText.text.toString().trim().isEmpty()) {
                (expectedReturnDateEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error =
                    "Введите дату возврата"
                isValid = false
            } else {
                (expectedReturnDateEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error = null
            }

            if (returnedCheckBox.isChecked && actualReturnDateEditText.text.toString().trim().isEmpty()) {
                (actualReturnDateEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error =
                    "Введите фактическую дату возврата"
                isValid = false
            } else {
                (actualReturnDateEditText.parent as? com.google.android.material.textfield.TextInputLayout)?.error = null
            }
        }

        return isValid
    }
}