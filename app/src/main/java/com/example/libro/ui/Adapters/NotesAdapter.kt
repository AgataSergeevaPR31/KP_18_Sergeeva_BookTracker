package com.example.libro.ui.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.libro.Database.Note
import com.example.libro.R
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(
    private val context: android.content.Context,
    private var notes: List<Note>,
    private var bookMap: Map<Long, Pair<String, String>>,
    private var coverImageMap: Map<Long, String?> = emptyMap(),
    private val onFavoriteClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    fun updateNotes(newNotes: List<Note>) {
        this.notes = newNotes
        notifyDataSetChanged()
    }

    fun updateBookMap(newBookMap: Map<Long, Pair<String, String>>, newCoverImageMap: Map<Long, String?> = emptyMap()) {
        this.bookMap = newBookMap
        this.coverImageMap = newCoverImageMap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        val bookInfo = bookMap[note.bookId]
        val coverImagePath = coverImageMap[note.bookId]

        holder.noteTextTextView.text = note.noteText
        holder.dateTextView.text = dateFormat.format(note.creationDate)

        if (bookInfo != null) {
            holder.bookTitleTextView.text = bookInfo.first
            holder.bookAuthorTextView.text = bookInfo.second
        } else {
            holder.bookTitleTextView.text = "Неизвестная книга"
            holder.bookAuthorTextView.text = ""
        }

        if (note.pageNumber != null && note.pageNumber > 0) {
            holder.textPageNumber.text = "Стр. ${note.pageNumber}"
            holder.textPageNumber.visibility = View.VISIBLE
        } else {
            holder.textPageNumber.visibility = View.GONE
        }

        if (!note.category.isNullOrEmpty()) {
            holder.categoryTextView.text = note.category
            holder.categoryTextView.visibility = View.VISIBLE
        } else {
            holder.categoryTextView.visibility = View.GONE
        }

        if (note.isFavorite) {
            holder.favoriteButton.setImageResource(R.drawable.heart)
        } else {
            holder.favoriteButton.setImageResource(R.drawable.heartempty)
        }

        if (!coverImagePath.isNullOrEmpty()) {
            Glide.with(context)
                .load(coverImagePath)
                .placeholder(R.drawable.plug)
                .into(holder.bookCoverImageView)
        } else {
            Glide.with(context)
                .load(R.drawable.plug)
                .into(holder.bookCoverImageView)
        }

        holder.favoriteButton.setOnClickListener {
            onFavoriteClick(note)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(note)
        }
    }

    override fun getItemCount(): Int = notes.size

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTextTextView: TextView = itemView.findViewById(R.id.noteTextTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val bookTitleTextView: TextView = itemView.findViewById(R.id.bookTitleTextView)
        val bookAuthorTextView: TextView = itemView.findViewById(R.id.bookAuthorTextView)
        val textPageNumber: TextView = itemView.findViewById(R.id.textPageNumber)
        val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val bookCoverImageView: ImageView = itemView.findViewById(R.id.bookCoverImageView)
    }
}