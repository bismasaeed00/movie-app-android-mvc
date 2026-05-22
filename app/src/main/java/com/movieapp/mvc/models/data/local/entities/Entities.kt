package com.movieapp.mvc.models.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ─── Room Entities ────────────────────────────────────────────────────────────
// Mirrors: CoreData entities in MovieAppMVC.xcdatamodeld
//   - ResponseEntity  → ResponseEntity  (page, total_pages, one-to-many → movies)
//   - MovieEntity     → MovieEntity     (all scalar attributes + FK to response)
//   - GenreEntity     → GenreEntity     (id, name + FK to movie)

/**
 * Mirrors CoreData: ResponseEntity
 * Attributes: page (Int64), total_pages (Int64)
 * Relationship: movies (to-many → MovieEntity)
 */
@Entity(tableName = "response_entity")
data class ResponseEntity(
    @PrimaryKey val page: Long,
    val totalPages: Long
)

/**
 * Mirrors CoreData: MovieEntity
 * Attributes: id, title, overview, posterPath, releaseDate, voteAverage, runtime, favourite
 * Relationship: response (to-one → ResponseEntity), genres (to-many → GenreEntity)
 */
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
    // FK column — mirrors CoreData inverse "response" relationship
    val responsePage: Long?
)

/**
 * Mirrors CoreData: GenreEntity
 * Attributes: id (Int64), name (String)
 * Relationship: movie (to-one → MovieEntity)
 */
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
    // FK column — mirrors CoreData inverse "movie" relationship
    val movieId: Long
)
