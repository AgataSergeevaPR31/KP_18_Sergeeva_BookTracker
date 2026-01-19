package com.example.libro.ui.library.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.libro.Database.Book
import com.example.libro.R
import com.example.libro.databinding.ItemBookBinding

class BookAdapter(
    private val onItemClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(
        private val binding: ItemBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.bookTitleTextView.text = book.title
            binding.bookAuthorTextView.text = book.author ?: "Автор неизвестен"

            setupRatingStars(book.rating ?: 0f)

            book.coverImage?.let { coverUrl ->
                Glide.with(binding.root.context)
                    .load(coverUrl)
                    .placeholder(R.drawable.plug)
                    .error(R.drawable.fire)
                    .into(binding.bookCoverImageView)
            } ?: run {
                binding.bookCoverImageView.setImageResource(R.drawable.plug)
            }

            binding.root.setOnClickListener {
                onItemClick(book)
            }
        }

        private fun setupRatingStars(rating: Float) {
            val starsLayout = binding.ratingStarsLayout
            starsLayout.removeAllViews()

            val fullStars = rating.toInt()
            val hasHalfStar = rating - fullStars >= 0.5f

            for (i in 0 until fullStars) {
                val star = ImageView(binding.root.context)
                star.setImageResource(R.drawable.starfull)
                star.layoutParams = LinearLayout.LayoutParams(
                    binding.root.resources.getDimensionPixelSize(R.dimen.star_size),
                    binding.root.resources.getDimensionPixelSize(R.dimen.star_size)
                )
                starsLayout.addView(star)
            }

            if (hasHalfStar) {
                val halfStar = ImageView(binding.root.context)
                halfStar.setImageResource(R.drawable.starhalf)
                halfStar.layoutParams = LinearLayout.LayoutParams(
                    binding.root.resources.getDimensionPixelSize(R.dimen.star_size),
                    binding.root.resources.getDimensionPixelSize(R.dimen.star_size)
                )
                starsLayout.addView(halfStar)
            }

            val emptyStarsCount = 5 - fullStars - if (hasHalfStar) 1 else 0
            for (i in 0 until emptyStarsCount) {
                val emptyStar = ImageView(binding.root.context)
                emptyStar.setImageResource(R.drawable.starempry)
                emptyStar.layoutParams = LinearLayout.LayoutParams(
                    binding.root.resources.getDimensionPixelSize(R.dimen.star_size),
                    binding.root.resources.getDimensionPixelSize(R.dimen.star_size)
                )
                starsLayout.addView(emptyStar)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.bookId == newItem.bookId
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem == newItem
            }
        }
    }
}