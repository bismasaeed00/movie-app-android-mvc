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

// ─── MoviesViewController ─────────────────────────────────────────────────────
// Mirrors: final class MoviesViewController: ObservableObject in MoviesViewController.swift
//
// Architectural mapping:
//   @MainActor                    → viewModelScope (main-thread dispatcher by default)
//   @Published private(set) var   → private MutableStateFlow + public StateFlow (read-only)
//   ObservableObject              → ViewModel (observed by Compose via collectAsState())
//   async func / await            → suspend fun / viewModelScope.launch
//
// ROLE (MVC): Controller — owns all business logic and state.
//             Views (Composables) are dumb; they only read StateFlows and forward events.

class MoviesViewController(
    private val service: MovieService,
    private val repository: MovieRepository
) : ViewModel() {

    // Mirrors: @Published private(set) var movies: [Movie] = []
    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    // Mirrors: @Published private(set) var favoriteMovieIDs: [Int] = []
    private val _favoriteMovieIDs = MutableStateFlow<List<Int>>(emptyList())
    val favoriteMovieIDs: StateFlow<List<Int>> = _favoriteMovieIDs.asStateFlow()

    // Mirrors: @Published private(set) var isLoading: Bool = false
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mirrors: @Published private(set) var errorMessage: String?
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Mirrors: private var currentPage = 1
    private var currentPage = 1

    // Mirrors: private var totalPages = 0
    private var totalPages = 0

    // Mirrors: var hasMorePages: Bool { (currentPage + 1) < totalPages }
    val hasMorePages: Boolean
        get() = (currentPage + 1) < totalPages

    // Mirrors: var loadMoreTitle: String { "Next Page: \(currentPage + 1) out of \(totalPages)" }
    val loadMoreTitle: String
        get() = "Next Page: ${currentPage + 1} out of $totalPages"

    // Mirrors: func loadNetworkData() async
    fun loadNetworkData() {
        viewModelScope.launch { fetchMoviesResponse() }
    }

    // Mirrors: func refresh() async — pull-to-refresh: clear cache, fetch fresh data
    fun refresh() {
        currentPage = 1
        viewModelScope.launch { fetchMoviesResponse() }
    }

    // Mirrors: func loadNextPage() async
    fun loadNextPage() {
        currentPage += 1
        viewModelScope.launch { fetchMoviesResponse() }
    }

    // Mirrors: func isFavorite(movie: Movie) -> Bool
    fun isFavorite(movie: Movie): Boolean = favoriteMovieIDs.value.contains(movie.id)

    // Mirrors: func onAppear()
    fun onAppear() {
        loadCachedData()
    }

    // Mirrors: private func loadCachedData()
    private fun loadCachedData() {
        viewModelScope.launch {
            val moviesData = withContext(Dispatchers.IO) { repository.fetchCachedMovies() }
            val paginationInfo = withContext(Dispatchers.IO) { repository.fetchPaginationInfo() }

            currentPage = if (paginationInfo.first > 0) paginationInfo.first else 1
            totalPages = paginationInfo.second

            if (moviesData.isEmpty()) return@launch

            _movies.value = moviesData
            _favoriteMovieIDs.value = withContext(Dispatchers.IO) {
                repository.fetchFavouriteMovieIDs()
            }
        }
    }

    // Mirrors: private func fetchMoviesResponse() async
    private suspend fun fetchMoviesResponse() {
        _isLoading.value = true
        _errorMessage.value = null

        try {
            val fetched = withContext(Dispatchers.IO) { service.fetchMovies(page = currentPage) }
            withContext(Dispatchers.IO) { repository.save(fetched, currentPage) }
            loadCachedData()
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage ?: e.message
        }

        _isLoading.value = false
    }

    // ─── ViewModelFactory ─────────────────────────────────────────────────────
    // Required to inject dependencies; mirrors SwiftUI's @StateObject init path
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            return MoviesViewController(
                service = MovieService(),
                repository = MovieRepository(db)
            ) as T
        }
    }
}
