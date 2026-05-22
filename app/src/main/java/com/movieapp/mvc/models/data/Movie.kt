package com.movieapp.mvc.models.data

import com.google.gson.annotations.SerializedName

// ─── Network Response DTOs ────────────────────────────────────────────────────
// Mirrors: MovieResponse, Movie, MovieDetailsResponse, Genre structs in Movie.swift

data class MovieResponse(
    val results: List<Movie>,
    val page: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    // NOTE: Not part of network payload; injected from Room after DB round-trip
    val favourite: Boolean? = null,
    val genres: List<Genre>? = null,
    val runtime: Int? = null
) {
    /** Mirrors: var posterURL: URL? in Movie.swift */
    val posterURL: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

    /** Mirrors: var formattedYear: String in Movie.swift */
    val formattedYear: String
        get() = if ((releaseDate?.length ?: 0) >= 4) releaseDate!!.take(4) else "N/A"
}

data class MovieDetailsResponse(
    val id: Int,
    val runtime: Int,
    val genres: List<Genre> = emptyList()
)

data class GenreResponse(
    val genres: List<Genre>
)

data class Genre(
    val id: Int,
    val name: String
)
