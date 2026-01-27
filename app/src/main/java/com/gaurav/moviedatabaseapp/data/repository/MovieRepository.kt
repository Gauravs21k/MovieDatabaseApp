package com.gaurav.moviedatabaseapp.data.repository

import com.gaurav.moviedatabaseapp.data.local.db.MovieDao
import com.gaurav.moviedatabaseapp.data.local.entities.Movie
import com.gaurav.moviedatabaseapp.data.remote.TmdbApi
import com.gaurav.moviedatabaseapp.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.gaurav.moviedatabaseapp.data.remote.MovieResponse
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val api: TmdbApi,
    private val movieDao: MovieDao
) {

    fun getMoviesByType(type: String): Flow<MovieResponse?> = flow {
        val local = movieDao.getMoviesByTypeSingle(type)
        if (local.isNotEmpty()) {
            emit(MovieResponse(results = local, page = 1, totalPages = 1, totalResults = local.size))
        }

        try {
            val response = when (type) {
                Constants.TYPE_TRENDING -> api.getTrendingMovies(page = 1)
                Constants.TYPE_NOW_PLAYING -> api.getNowPlayingMovies(page = 1)
                else -> null
            }

            response?.let {
                updateMoviesOfType(it.results, type)
                emit(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }

    suspend fun loadMoreMovies(type: String, page: Int): Result<MovieResponse> {
        return try {
            val response = when (type) {
                Constants.TYPE_TRENDING -> api.getTrendingMovies(page = page)
                Constants.TYPE_NOW_PLAYING -> api.getNowPlayingMovies(page = page)
                else -> return Result.failure(IllegalArgumentException("Unknown movie type: $type"))
            }

            appendMoviesOfType(response.results, type)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMovies(query: String): List<Movie> {
        val localResults = movieDao.searchMoviesLocal("%$query%")
        if (localResults.isNotEmpty()) return localResults

        return try {
            val response = api.searchMovies(query)
            response.results.map { movie ->
                val existingMovie = movieDao.getMovieById(movie.id)
                movie.copy(isBookmarked = existingMovie?.isBookmarked ?: false)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getBookmarkedMovies(): Flow<List<Movie>> = movieDao.getBookmarkedMovies()

    fun getLocalMoviesByType(type: String): Flow<List<Movie>> = movieDao.getMoviesByType(type)

    suspend fun toggleMovieBookmark(movie: Movie) {
        val newBookmarkStatus = !movie.isBookmarked
        val existingMovie = movieDao.getMovieById(movie.id)
        
        if (existingMovie != null) {
            movieDao.updateBookmarkStatus(movie.id, newBookmarkStatus)
        } else {
            movieDao.upsertMovie(movie.copy(isBookmarked = newBookmarkStatus))
        }
    }

    suspend fun getMovieById(id: Int): Movie? {
        var movie = movieDao.getMovieById(id)

        if (movie == null) {
            try {
                movie = api.getMovieById(id)
            } catch (e: Exception) {
                return null
            }
        }

        return movie
    }

    suspend fun refreshLocalTrendingMovies(): List<Movie> = movieDao.getMoviesByTypeSingle(Constants.TYPE_TRENDING)

    suspend fun refreshLocalNowPlayingMovies(): List<Movie> = movieDao.getMoviesByTypeSingle(Constants.TYPE_NOW_PLAYING)

    private suspend fun updateMoviesOfType(movies: List<Movie>, type: String) {
        movieDao.clearNonBookmarkedMoviesByType(type)
        movieDao.insertMoviesWithType(movies, type)
    }

    private suspend fun appendMoviesOfType(movies: List<Movie>, type: String) {
        movieDao.insertMoviesWithType(movies, type)
    }
}
