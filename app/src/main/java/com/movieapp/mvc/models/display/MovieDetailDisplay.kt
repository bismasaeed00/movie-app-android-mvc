package com.movieapp.mvc.models.display

// Presentation model for the detail screen.
// Created by the Controller; the View renders these values directly with no further transformation.
data class MovieDetailDisplay(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val rating: String,
    val year: String,
    val runtime: String,
    val overview: String,
    val genres: List<String>,
    val isFavourite: Boolean
)
