package com.gaurav.moviedatabaseapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gaurav.moviedatabaseapp.data.local.entities.Movie
import com.gaurav.moviedatabaseapp.data.local.entities.MovieType

@Database(
    entities = [Movie::class, MovieType::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
