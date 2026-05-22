package com.movieapp.mvc.controllers

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.movieapp.mvc.models.data.Movie
import com.movieapp.mvc.models.data.local.AppDatabase
import com.movieapp.mvc.models.data.remote.MovieService
import com.movieapp.mvc.models.repository.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// ─── MovieDetailController ────────────────────────────────────────────────────
// Mirrors: final class MovieDetailController: ObservableObject in MovieDetailController.swift
//
// Architectural mapping:
//   @MainActor                    → viewModelScope (main-thread dispatcher by default)
//   @Published private(set) var   → private MutableStateFlow + public StateFlow (read-only)
//   ObservableObject              → ViewModel (observed by Compose via collectAsState())
//   async func / await            → suspend fun / viewModelScope.launch
//
// ROLE (MVC): Controller — handles all business logic for the detail screen.
//             MovieDetailScreen (Composable) is a dumb presentation layer.

class MovieDetailController(
    initialMovie: Movie,
    private val service: MovieService,
    private val repository: MovieRepository
) : ViewModel() {

    // Mirrors: @Published private(set) var movie: Movie
    private val _movie = MutableStateFlow(initialMovie)
    val movie: StateFlow<Movie> = _movie.asStateFlow()

    // Mirrors: var rating: String { String(format: "%.1f", movie.voteAverage) }
    val rating: String
        get() = String.format(Locale.US, "%.1f", _movie.value.voteAverage)

    // Mirrors: var isFavourite: Bool { movie.favourite ?? false }
    val isFavourite: Boolean
        get() = _movie.value.favourite ?: false

    // Mirrors: var overview: String { movie.overview.isEmpty ? "No overview available." : movie.overview }
    val overview: String
        get() = _movie.value.overview.ifEmpty { "No overview available." }

    init {
        // Mirrors: init(movie: Movie) { self.movie = movie; loadCachedMovie() }
        loadCachedMovie()
    }

    // Mirrors: func loadMovieDetails() async
    fun loadMovieDetails() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    service.fetchMovieDetails(_movie.value)
                }
                withContext(Dispatchers.IO) {
                    repository.update(movie = _movie.value, detailResponse = response)
                }
                loadCachedMovie()
            } catch (e: Exception) {
                println(e.localizedMessage ?: e.message)
            }
        }
    }

    // Mirrors: func toggleFavourite()
    fun toggleFavourite() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.updateMovie(
                    movie = _movie.value,
                    isFavourite = !(_movie.value.favourite ?: false)
                )
            }
            loadCachedMovie()
        }
    }

    // Mirrors: private func loadCachedMovie()
    private fun loadCachedMovie() {
        viewModelScope.launch {
            val updatedMovie = withContext(Dispatchers.IO) {
                repository.fetchCachedMovies(id = _movie.value.id).firstOrNull()
            } ?: return@launch
            _movie.value = updatedMovie
        }
    }

    // ─── ViewModelFactory ─────────────────────────────────────────────────────
    class Factory(
        private val movie: Movie,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            return MovieDetailController(
                initialMovie = movie,
                service = MovieService(),
                repository = MovieRepository(db)
            ) as T
        }
    }
}
