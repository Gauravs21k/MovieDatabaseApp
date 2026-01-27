package com.gaurav.moviedatabaseapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.gaurav.moviedatabaseapp.MovieApplication
import com.gaurav.moviedatabaseapp.databinding.FragmentHomeBinding
import com.gaurav.moviedatabaseapp.ui.activities.MovieDetailsActivity
import com.gaurav.moviedatabaseapp.ui.adapters.MovieAdapter
import com.gaurav.moviedatabaseapp.ui.adapters.MovieTabsAdapter
import com.gaurav.moviedatabaseapp.ui.viewmodels.MovieViewModel
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModel: MovieViewModel

    private lateinit var tabsAdapter: MovieTabsAdapter
    private lateinit var searchAdapter: MovieAdapter
    private var isSearchActive = false
    private lateinit var backPressedCallback: OnBackPressedCallback

    private val movieDetailsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val movieId = data?.getIntExtra("movie_id", -1) ?: -1
            val isBookmarked = data?.getBooleanExtra("is_bookmarked", false) ?: false

            if (movieId != -1) {
                updateSearchMovieBookmarkStatus(movieId, isBookmarked)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity().application as MovieApplication).appComponent.inject(this)

        setupBackPressedCallback()
        setupRecyclerView()
        setupTabs()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        searchAdapter = MovieAdapter(
            onMovieClick = { movie -> navigateToDetailsForResult(movie.id) },
            onBookmarkClick = { movie -> viewModel.toggleBookmark(movie) }
        )
        binding.rvSearch.apply {
            adapter = searchAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setupBackPressedCallback() {
        backPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (isSearchActive) {
                    exitSearchMode()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    private fun exitSearchMode() {
        binding.searchEditText.setText("")
        binding.searchEditText.clearFocus()
        showTabs()
        isSearchActive = false
        backPressedCallback.isEnabled = false
    }

    private fun setupTabs() {
        tabsAdapter = MovieTabsAdapter(requireActivity())
        binding.viewPager.adapter = tabsAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabsAdapter.getTabTitle(position)
        }.attach()
    }

    private fun setupSearch() {
        binding.searchEditText.apply {
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                handleSearchFocusChange(hasFocus)
            }

            addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    handleSearchTextChange(s.toString().trim())
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
            })

            setOnEditorActionListener { _, _, _ ->
                if (text.toString().trim().isEmpty()) {
                    clearFocus()
                    showTabs()
                }
                false
            }
        }
    }

    private fun handleSearchFocusChange(hasFocus: Boolean) {
        if (hasFocus) {
            activateSearchMode()
        } else if (binding.searchEditText.text.toString().trim().isEmpty()) {
            deactivateSearchMode()
        }
    }

    private fun handleSearchTextChange(query: String) {
        if (!binding.searchEditText.hasFocus() && query.isEmpty()) {
            showTabs()
            return
        }

        if (binding.searchEditText.hasFocus()) {
            showSearchResults()
            if (query.isNotEmpty()) {
                viewModel.onSearchQueryChanged(query)
            } else {
                showEmptySearchState()
            }
        }
    }

    private fun activateSearchMode() {
        showSearchResults()
        isSearchActive = true
        backPressedCallback.isEnabled = true

        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            viewModel.onSearchQueryChanged(query)
        } else {
            showEmptySearchState()
        }
    }

    private fun deactivateSearchMode() {
        isSearchActive = false
        backPressedCallback.isEnabled = false
    }

    private fun showEmptySearchState() {
        viewModel.clearSearchResults()
    }

    private fun showSearchResults() {
        binding.tabLayout.visibility = View.GONE
        binding.viewPager.visibility = View.GONE
        binding.rvSearch.visibility = View.VISIBLE
    }

    private fun showTabs() {
        binding.tabLayout.visibility = View.VISIBLE
        binding.viewPager.visibility = View.VISIBLE
        binding.rvSearch.visibility = View.GONE

        isSearchActive = false
        backPressedCallback.isEnabled = false
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner) { movies ->
            searchAdapter.submitList(movies)
        }
    }

    private fun navigateToDetailsForResult(movieId: Int) {
        val intent = Intent(requireContext(), MovieDetailsActivity::class.java).apply {
            putExtra("movie_id", movieId)
        }
        movieDetailsLauncher.launch(intent)
    }

    private fun updateSearchMovieBookmarkStatus(movieId: Int, isBookmarked: Boolean) {
        val currentList = searchAdapter.currentList.toMutableList()
        val updatedList = currentList.map { movie ->
            if (movie.id == movieId) {
                movie.copy(isBookmarked = isBookmarked)
            } else {
                movie
            }
        }
        searchAdapter.submitList(updatedList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
