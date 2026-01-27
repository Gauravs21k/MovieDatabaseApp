package com.gaurav.moviedatabaseapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gaurav.moviedatabaseapp.MovieApplication
import com.gaurav.moviedatabaseapp.databinding.FragmentBookmarksBinding
import com.gaurav.moviedatabaseapp.ui.activities.MovieDetailsActivity
import com.gaurav.moviedatabaseapp.ui.adapters.MovieAdapter
import com.gaurav.moviedatabaseapp.ui.viewmodels.MovieViewModel
import javax.inject.Inject

class BookmarksFragment : Fragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModel: MovieViewModel

    private lateinit var bookmarksAdapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
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
        bookmarksAdapter = MovieAdapter(
            onMovieClick = { movie -> navigateToDetails(movie.id) },
            onBookmarkClick = { movie -> viewModel.toggleBookmark(movie) }
        )
        binding.rvBookmarks.adapter = bookmarksAdapter
    }

    private fun observeViewModel() {
        viewModel.bookmarkedMovies.observe(viewLifecycleOwner) { movies ->
            bookmarksAdapter.submitList(movies)
        }
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
