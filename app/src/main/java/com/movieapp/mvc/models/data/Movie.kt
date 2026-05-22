package com.movieapp.mvc.models.data

import com.google.gson.annotations.SerializedName

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
    val favourite: Boolean? = null,
    val genres: List<Genre>? = null,
    val runtime: Int? = null
)

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
