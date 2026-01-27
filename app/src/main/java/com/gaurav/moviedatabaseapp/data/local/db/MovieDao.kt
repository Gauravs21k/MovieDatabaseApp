package com.gaurav.moviedatabaseapp.data.local.db

import androidx.room.*
import com.gaurav.moviedatabaseapp.data.local.entities.Movie
import com.gaurav.moviedatabaseapp.data.local.entities.MovieType
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("""
        SELECT m.* FROM movies m
        INNER JOIN movie_types mt ON m.id = mt.movieId
        WHERE mt.type = :type
        ORDER BY mt.page ASC, mt.positionInPage ASC
    """)
    fun getMoviesByType(type: String): Flow<List<Movie>>

    @Query("""
        SELECT m.* FROM movies m
        INNER JOIN movie_types mt ON m.id = mt.movieId
        WHERE mt.type = :type
        ORDER BY mt.page ASC, mt.positionInPage ASC
    """)
    suspend fun getMoviesByTypeSingle(type: String): List<Movie>

    @Query("SELECT * FROM movies WHERE isBookmarked = 1")
    fun getBookmarkedMovies(): Flow<List<Movie>>


    @Upsert
    suspend fun upsertMovies(movies: List<Movie>)

    @Upsert
    suspend fun upsertMovie(movie: Movie)

    @Query("UPDATE movies SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: Int, isBookmarked: Boolean)

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: Int): Movie?

    @Query("SELECT * FROM movies WHERE title LIKE :query")
    suspend fun searchMoviesLocal(query: String): List<Movie>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMovieTypes(movieTypes: List<MovieType>)

    @Query("DELETE FROM movie_types WHERE type = :type")
    suspend fun clearMovieTypesByType(type: String)

    @Transaction
    suspend fun insertMoviesWithType(movies: List<Movie>, type: String, page: Int = 1) {
        upsertMovies(movies)
        val movieTypes = movies.mapIndexed { index, movie ->
            MovieType(movie.id, type, page, index)
        }
        insertMovieTypes(movieTypes)
    }

    @Transaction
    suspend fun clearNonBookmarkedMoviesByType(type: String) {
        deleteNonBookmarkedTypes(type)
        deleteOrphanedNonBookmarkedMovies()
    }

    @Query("DELETE FROM movie_types WHERE type = :type AND movieId NOT IN (SELECT id FROM movies WHERE isBookmarked = 1)")
    suspend fun deleteNonBookmarkedTypes(type: String)

    @Query("""
        DELETE FROM movies
        WHERE isBookmarked = 0
        AND id NOT IN (SELECT DISTINCT movieId FROM movie_types)
    """)
    suspend fun deleteOrphanedNonBookmarkedMovies()
}
