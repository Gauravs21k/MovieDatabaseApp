package com.gaurav.moviedatabaseapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gaurav.moviedatabaseapp.data.local.entities.Movie
import com.gaurav.moviedatabaseapp.data.local.entities.MovieType

@Database(
    entities = [Movie::class, MovieType::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE movie_types ADD COLUMN page INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE movie_types ADD COLUMN positionInPage INTEGER NOT NULL DEFAULT 0")
                database.execSQL("DELETE FROM movie_types")
            }
        }
    }
}
