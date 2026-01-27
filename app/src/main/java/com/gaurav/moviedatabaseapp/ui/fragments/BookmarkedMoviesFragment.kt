package com.gaurav.moviedatabaseapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.gaurav.moviedatabaseapp.MovieApplication
import com.gaurav.moviedatabaseapp.databinding.FragmentMovieListBinding
import com.gaurav.moviedatabaseapp.ui.activities.MovieDetailsActivity
import com.gaurav.moviedatabaseapp.ui.adapters.MovieAdapter
import com.gaurav.moviedatabaseapp.ui.viewmodels.MovieViewModel
import javax.inject.Inject

class BookmarkedMoviesFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModel: MovieViewModel

    private lateinit var movieAdapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity().application as MovieApplication).appComponent.inject(this)

        setupRecyclerView()
        observeViewModel()
        viewModel.fetchBookmarkedMovies()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(
            onMovieClick = { movie -> navigateToDetails(movie.id) },
            onBookmarkClick = { movie -> viewModel.toggleBookmark(movie) }
        )
        binding.recyclerView.apply {
            adapter = movieAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun observeViewModel() {
        viewModel.bookmarkedMovies.observe(viewLifecycleOwner) { movies ->
            movieAdapter.submitList(movies)
            updateEmptyState(movies.isEmpty())
        }

        binding.retryText.setOnClickListener {
            viewModel.fetchBookmarkedMovies()
        }
    }

    private fun updateEmptyState(showEmpty: Boolean) {
        if (showEmpty) {
            binding.emptyTitle.text = "No bookmarked movies"
            binding.emptyMessage.text = "Movies you bookmark will appear here"
            binding.retryText.visibility = View.GONE
        }
        binding.emptyTitle.visibility = if (showEmpty) View.VISIBLE else View.GONE
        binding.emptyMessage.visibility = if (showEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (showEmpty) View.GONE else View.VISIBLE
    }

    private fun navigateToDetails(movieId: Int) {
        val intent = Intent(requireContext(), MovieDetailsActivity::class.java).apply {
            putExtra("movie_id", movieId)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}