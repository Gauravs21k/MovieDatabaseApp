package com.gaurav.moviedatabaseapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "movie_types",
    primaryKeys = ["movieId", "type"],
    foreignKeys = [
        ForeignKey(
            entity = Movie::class,
            parentColumns = ["id"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MovieType(
    val movieId: Int,
    val type: String // trending, now_playing, search
)