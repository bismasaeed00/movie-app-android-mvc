package com.movieapp.mvc.models.display

// Presentation model for a single row in the movies/favourites list.
// Created by the Controller; the View reads only these pre-formatted strings.
data class MovieListItemDisplay(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val rating: String,
    val year: String,
    val overview: String
)
