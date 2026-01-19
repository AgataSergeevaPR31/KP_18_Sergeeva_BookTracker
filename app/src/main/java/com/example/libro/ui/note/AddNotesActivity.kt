package com.example.libro.ui.note

import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.libro.Database.AppDatabase
import com.example.libro.Database.Note
import com.example.libro.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddNotesActivity : AppCompatActivity() {

    @Inject
    lateinit var database: AppDatabase

    private lateinit var backButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var addImageButton: ImageButton
    private lateinit var pageNumberEditText: EditText
    private lateinit var noteContentEditText: EditText
    private lateinit var noteImageView: ImageView
    private lateinit var deleteImageButton: FloatingActionButton

    private var bookId: Long = -1
    private var maxPageNumber: Int = 0
    private var currentPhotoPath: String? = null
    private var imageUri: Uri? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let { uri ->
                noteImageView.setImageURI(uri)
                noteImageView.visibility = ImageView.VISIBLE
                deleteImageButton.visibility = FloatingActionButton.VISIBLE
            }
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = uri
            noteImageView.setImageURI(uri)
            noteImageView.visibility = ImageView.VISIBLE
            deleteImageButton.visibility = FloatingActionButton.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notes)

        initViews()
        setupCategorySpinner()
        setupClickListeners()

        bookId = intent.getLongExtra(EXTRA_BOOK_ID, -1)
        maxPageNumber = intent.getIntExtra(EXTRA_MAX_PAGE, 0)

        if (bookId == -1L) {
            Toast.makeText(this, "Ошибка: ID книги не передан", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)
        categorySpinner = findViewById(R.id.categorySpinner)
        addImageButton = findViewById(R.id.addImageButton)
        pageNumberEditText = findViewById(R.id.pageNumberEditText)
        noteContentEditText = findViewById(R.id.noteContentEditText)
        noteImageView = findViewById(R.id.noteImageView)
        deleteImageButton = findViewById(R.id.deleteImageButton)
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf(
            "Цитата",
            "Резюме",
            "Мысль",
            "Вопрос"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            saveNote()
        }

        addImageButton.setOnClickListener {
            showImageSourceDialog()
        }

        deleteImageButton.setOnClickListener {
            removeImage()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Сделать фото", "Выбрать из галереи", "Отмена")

        AlertDialog.Builder(this)
            .setTitle("Добавить изображение")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickImageFromGallery()
                }
            }
            .show()
    }

    private fun takePhoto() {
        val photoFile = createImageFile()
        photoFile?.let {
            val photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            imageUri = photoUri
            takePicture.launch(photoUri)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun pickImageFromGallery() {
        pickImage.launch("image/*")
    }

    private fun removeImage() {
        noteImageView.setImageURI(null)
        noteImageView.visibility = ImageView.GONE
        deleteImageButton.visibility = FloatingActionButton.GONE
        imageUri = null
        currentPhotoPath = null
    }

    private fun saveNote() {
        val category = categorySpinner.selectedItem as String
        val pageNumberText = pageNumberEditText.text.toString()
        val noteText = noteContentEditText.text.toString().trim()
        val pageNumber = pageNumberText.toIntOrNull()

        if (noteText.isEmpty()) {
            Toast.makeText(this, "Введите текст заметки", Toast.LENGTH_SHORT).show()
            return
        }

        if (pageNumber == null) {
            Toast.makeText(this, "Введите номер страницы", Toast.LENGTH_SHORT).show()
            return
        }

        if (pageNumber > maxPageNumber) {
            Toast.makeText(this, "Номер страницы не может быть больше $maxPageNumber", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val note = Note(
                    bookId = bookId,
                    noteText = noteText,
                    creationDate = Date(),
                    pageNumber = pageNumber,
                    isFavorite = false,
                    category = category,
                    imagePath = currentPhotoPath
                )

                withContext(Dispatchers.IO) {
                    database.noteDao().insertNote(note)
                }

                Toast.makeText(this@AddNotesActivity, "Заметка сохранена", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@AddNotesActivity, "Ошибка при сохранении заметки: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val EXTRA_BOOK_ID = "extra_book_id"
        const val EXTRA_MAX_PAGE = "extra_max_page"
    }
}