package com.gaurav.moviedatabaseapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaurav.moviedatabaseapp.data.local.entities.Movie
import com.gaurav.moviedatabaseapp.data.repository.MovieRepository
import com.gaurav.moviedatabaseapp.utils.Constants
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class MovieViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _trendingMovies = MutableLiveData<List<Movie>>()
    val trendingMovies: LiveData<List<Movie>> = _trendingMovies

    private val _nowPlayingMovies = MutableLiveData<List<Movie>>()
    val nowPlayingMovies: LiveData<List<Movie>> = _nowPlayingMovies

    private val _searchResults = MutableLiveData<List<Movie>>()
    val searchResults: LiveData<List<Movie>> = _searchResults

    private val _bookmarkedMovies = MutableLiveData<List<Movie>>()
    val bookmarkedMovies: LiveData<List<Movie>> = _bookmarkedMovies

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoadingMore = MutableLiveData<Boolean>()
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val searchQuery = MutableStateFlow("")

    private var trendingCurrentPage = 1
    private var trendingTotalPages = 1
    private var nowPlayingCurrentPage = 1
    private var nowPlayingTotalPages = 1

    init {
        setupSearch()
        setupReactiveQueries()
    }

    private fun setupReactiveQueries() {
        viewModelScope.launch {
            repository.getLocalMoviesByType(Constants.TYPE_TRENDING)
                .collect { movies ->
                    _trendingMovies.value = movies
                }
        }

        viewModelScope.launch {
            repository.getLocalMoviesByType(Constants.TYPE_NOW_PLAYING)
                .collect { movies ->
                    _nowPlayingMovies.value = movies
                }
        }
    }

    fun fetchTrendingMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getMoviesByType(com.gaurav.moviedatabaseapp.utils.Constants.TYPE_TRENDING)
                .catch { e -> _error.value = e.message }
                .collect { response ->
                    response?.let {
                        _trendingMovies.value = it.results
                        trendingCurrentPage = it.page
                        trendingTotalPages = it.totalPages
                    }
                    _isLoading.value = false
                }
        }
    }

    fun fetchNowPlayingMovies() {
        viewModelScope.launch {
            repository.getMoviesByType(com.gaurav.moviedatabaseapp.utils.Constants.TYPE_NOW_PLAYING)
                .catch { e -> _error.value = e.message }
                .collect { response ->
                    response?.let {
                        _nowPlayingMovies.value = it.results
                        nowPlayingCurrentPage = it.page
                        nowPlayingTotalPages = it.totalPages
                    }
                }
        }
    }

    fun initializePaginationState() {
        trendingCurrentPage = 1
        trendingTotalPages = 1
        nowPlayingCurrentPage = 1
        nowPlayingTotalPages = 1
    }

    @OptIn(FlowPreview::class)
    private fun setupSearch() {
        viewModelScope.launch {
            searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        flow { emit(repository.searchMovies(query)) }
                    }
                }
                .collect { results ->
                    _searchResults.value = results
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun clearSearchResults() {
        searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun fetchBookmarkedMovies() {
        viewModelScope.launch {
            repository.getBookmarkedMovies()
                .catch { e -> _error.value = e.message }
                .collect { movies ->
                    _bookmarkedMovies.value = movies
                }
        }
    }

    fun toggleBookmark(movie: Movie) {
        viewModelScope.launch {
            try {
                repository.toggleMovieBookmark(movie)
                refreshMovieInAllLists(movie.id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private suspend fun refreshMovieInAllLists(movieId: Int) {
        val updatedMovie = repository.getMovieById(movieId) ?: return

        _searchResults.updateMovie(updatedMovie)

        if (updatedMovie.isBookmarked) {
            fetchBookmarkedMovies()
        }
    }

    private fun MutableLiveData<List<Movie>>.updateMovie(updatedMovie: Movie) {
        value = value?.map { movie ->
            if (movie.id == updatedMovie.id) updatedMovie else movie
        }
    }



    fun loadMoreTrendingMovies() {
        if (_isLoadingMore.value == true || trendingCurrentPage >= trendingTotalPages) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = trendingCurrentPage + 1

            repository.loadMoreMovies(
                com.gaurav.moviedatabaseapp.utils.Constants.TYPE_TRENDING,
                nextPage
            ).fold(
                onSuccess = { response ->
                    trendingCurrentPage = response.page
                    trendingTotalPages = response.totalPages
                    val updatedMovies = repository.refreshLocalTrendingMovies()
                    _trendingMovies.value = updatedMovies
                },
                onFailure = { e ->
                    _error.value = e.message
                }
            )
            _isLoadingMore.value = false
        }
    }

    fun loadMoreNowPlayingMovies() {
        if (_isLoadingMore.value == true || nowPlayingCurrentPage >= nowPlayingTotalPages) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = nowPlayingCurrentPage + 1

            repository.loadMoreMovies(
                com.gaurav.moviedatabaseapp.utils.Constants.TYPE_NOW_PLAYING,
                nextPage
            ).fold(
                onSuccess = { response ->
                    nowPlayingCurrentPage = response.page
                    nowPlayingTotalPages = response.totalPages
                    val updatedMovies = repository.refreshLocalNowPlayingMovies()
                    _nowPlayingMovies.value = updatedMovies
                },
                onFailure = { e ->
                    _error.value = e.message
                }
            )
            _isLoadingMore.value = false
        }
    }

    fun canLoadMoreTrending(): Boolean = trendingCurrentPage < trendingTotalPages
    fun canLoadMoreNowPlaying(): Boolean = nowPlayingCurrentPage < nowPlayingTotalPages
}
