package com.gaurav.moviedatabaseapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gaurav.moviedatabaseapp.MovieApplication
import com.gaurav.moviedatabaseapp.data.local.entities.Movie
import com.gaurav.moviedatabaseapp.data.repository.MovieRepository
import com.gaurav.moviedatabaseapp.databinding.ActivityMovieDetailsBinding
import com.gaurav.moviedatabaseapp.ui.viewmodels.MovieViewModel
import com.gaurav.moviedatabaseapp.utils.Constants
import kotlinx.coroutines.launch
import javax.inject.Inject

class MovieDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailsBinding

    @Inject
    lateinit var repository: MovieRepository

    @Inject
    lateinit var viewModel: MovieViewModel

    private var currentMovie: Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as MovieApplication).appComponent.inject(this)
        binding = ActivityMovieDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val movieId = intent.getIntExtra("movie_id", -1)
        if (movieId != -1) {
            loadMovieDetails(movieId)
        } else {
            handleDeepLink(intent)
        }
    }

    private fun loadMovieDetails(id: Int) {
        lifecycleScope.launch {
            try {
                val movie = repository.getMovieById(id)
                if (movie != null) {
                    displayMovie(movie)
                } else {
                    Toast.makeText(this@MovieDetailsActivity, "Movie not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MovieDetailsActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMovie(movie: Movie) {
        currentMovie = movie

        binding.tvTitle.text = movie.title
        binding.tvOverview.text = movie.overview
        binding.tvReleaseDate.text = "Release: ${movie.releaseDate}"
        binding.tvRating.text = "Rating: ${movie.voteAverage}"

        Glide.with(this)
            .load(Constants.IMAGE_BASE_URL + movie.posterPath)
            .into(binding.ivPoster)

        updateBookmarkButton(movie.isBookmarked)

        binding.btnShare.setOnClickListener {
            shareMovie(movie)
        }

        binding.btnBookmark.setOnClickListener {
            toggleBookmark()
        }
    }

    private fun updateBookmarkButton(isBookmarked: Boolean) {
        binding.btnBookmark.text = if (isBookmarked) "Remove Bookmark" else "Add Bookmark"
    }

    private fun toggleBookmark() {
        currentMovie?.let { movie ->
            viewModel.toggleBookmark(movie)
            val newBookmarkStatus = !movie.isBookmarked
            currentMovie = movie.copy(isBookmarked = newBookmarkStatus)
            updateBookmarkButton(newBookmarkStatus)

            val message = if (newBookmarkStatus) "Movie bookmarked!" else "Bookmark removed!"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            val resultIntent = Intent().apply {
                putExtra("movie_id", movie.id)
                putExtra("is_bookmarked", newBookmarkStatus)
            }
            setResult(RESULT_OK, resultIntent)
        }
    }

    private fun shareMovie(movie: Movie) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, movie.title)
            putExtra(Intent.EXTRA_TEXT, "Check out this movie: ${movie.title}\n\nDeepLink: movies://details/${movie.id}")
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data
        if (data != null && data.scheme == "movies") {
            val movieId = data.lastPathSegment?.toIntOrNull()
            if (movieId != null) {
                loadMovieDetails(movieId)
            }
        }
    }
}
