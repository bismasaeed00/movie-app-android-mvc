package com.movieapp.mvc.models.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "response_entity")
data class ResponseEntity(
    @PrimaryKey val page: Long,
    val totalPages: Long
)

@Entity(
    tableName = "movie_entity",
    foreignKeys = [
        ForeignKey(
            entity = ResponseEntity::class,
            parentColumns = ["page"],
            childColumns = ["responsePage"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("responsePage")]
)
data class MovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val runtime: Long,
    val favourite: Boolean,
    val responsePage: Long?
)

@Entity(
    tableName = "genre_entity",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["id"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("movieId")]
)
data class GenreEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val id: Long,
    val name: String,
    val movieId: Long
)
