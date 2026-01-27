package com.gaurav.moviedatabaseapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gaurav.moviedatabaseapp.R
import com.gaurav.moviedatabaseapp.data.local.entities.Movie
import com.gaurav.moviedatabaseapp.databinding.ItemMovieBinding
import com.gaurav.moviedatabaseapp.utils.Constants

class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit,
    private val onBookmarkClick: (Movie) -> Unit,
    private val onLoadMore: (() -> Unit)? = null
) : ListAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    private var isLoadingMore = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))

        if (position >= itemCount - 5 && onLoadMore != null && !isLoadingMore) {
            isLoadingMore = true
            onLoadMore.invoke()
        }
    }

    fun resetLoadingState() {
        isLoadingMore = false
    }

    inner class MovieViewHolder(private val binding: ItemMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.tvTitle.text = movie.title
            binding.tvReleaseDate.text = movie.releaseDate?.substring(0, 4)

            val rating = (movie.voteAverage ?: 0.0) / 2.0
            binding.tvRating.rating = rating.toFloat()

            Glide.with(itemView.context)
                .load(Constants.IMAGE_BASE_URL + movie.posterPath)
                .into(binding.ivPoster)

            val bookmarkIcon = if (movie.isBookmarked) {
                R.drawable.ic_bookmarked
            } else {
                R.drawable.ic_un_bookmarked
            }
            binding.ivBookmark.setImageResource(bookmarkIcon)

            binding.root.setOnClickListener { onMovieClick(movie) }
            binding.ivBookmark.setOnClickListener { onBookmarkClick(movie) }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}
