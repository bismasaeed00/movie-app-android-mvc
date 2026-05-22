package com.movieapp.mvc.controllers

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.movieapp.mvc.models.data.Movie
import com.movieapp.mvc.models.data.local.AppDatabase
import com.movieapp.mvc.models.data.remote.MovieService
import com.movieapp.mvc.models.display.MovieListItemDisplay
import com.movieapp.mvc.models.repository.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MoviesViewController(
    private val service: MovieService,
    private val repository: MovieRepository
) : ViewModel() {

    private val _movies = MutableStateFlow<List<MovieListItemDisplay>>(emptyList())
    val movies: StateFlow<List<MovieListItemDisplay>> = _movies.asStateFlow()

    private val _favouriteMovies = MutableStateFlow<List<MovieListItemDisplay>>(emptyList())
    val favouriteMovies: StateFlow<List<MovieListItemDisplay>> = _favouriteMovies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _hasMorePages = MutableStateFlow(false)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    private val _loadMoreTitle = MutableStateFlow("")
    val loadMoreTitle: StateFlow<String> = _loadMoreTitle.asStateFlow()

    private var currentPage = 1
    private var totalPages = 0

    init {
        loadCachedData()
    }

    fun selectTab(index: Int) { _selectedTab.value = index }

    fun loadNetworkData() {
        viewModelScope.launch { fetchMoviesResponse() }
    }

    fun refresh() {
        currentPage = 1
        viewModelScope.launch { fetchMoviesResponse() }
    }

    fun loadNextPage() {
        currentPage += 1
        viewModelScope.launch { fetchMoviesResponse() }
    }

    private fun loadCachedData() {
        viewModelScope.launch {
            val allMovies = withContext(Dispatchers.IO) { repository.fetchCachedMovies() }
            val paginationInfo = withContext(Dispatchers.IO) { repository.fetchPaginationInfo() }

            currentPage = if (paginationInfo.first > 0) paginationInfo.first else 1
            totalPages = paginationInfo.second

            _hasMorePages.value = (currentPage + 1) < totalPages
            _loadMoreTitle.value = "Next Page: ${currentPage + 1} out of $totalPages"

            _movies.value = allMovies.map { it.toListItem() }
            _favouriteMovies.value = allMovies
                .filter { it.favourite == true }
                .map { it.toListItem() }
        }
    }

    private suspend fun fetchMoviesResponse() {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val fetched = withContext(Dispatchers.IO) { service.fetchMovies(page = currentPage) }
            withContext(Dispatchers.IO) { repository.save(fetched, currentPage) }
            loadCachedData()
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
        _isLoading.value = false
    }

    private fun Movie.toListItem(): MovieListItemDisplay = MovieListItemDisplay(
        id = id,
        title = title,
        posterUrl = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
        rating = String.format(Locale.US, "%.1f", voteAverage),
        year = if ((releaseDate?.length ?: 0) >= 4) releaseDate!!.take(4) else "N/A",
        overview = overview
    )

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
