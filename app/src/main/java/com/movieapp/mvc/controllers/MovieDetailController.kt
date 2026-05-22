package com.movieapp.mvc.controllers

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.movieapp.mvc.models.data.Movie
import com.movieapp.mvc.models.data.local.AppDatabase
import com.movieapp.mvc.models.data.remote.MovieService
import com.movieapp.mvc.models.display.MovieDetailDisplay
import com.movieapp.mvc.models.repository.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MovieDetailController(
    private val movieId: Int,
    private val service: MovieService,
    private val repository: MovieRepository
) : ViewModel() {

    private val _rawMovie = MutableStateFlow<Movie?>(null)

    private val _display = MutableStateFlow<MovieDetailDisplay?>(null)
    val display: StateFlow<MovieDetailDisplay?> = _display.asStateFlow()

    init {
        loadCachedMovie()
    }

    fun loadMovieDetails() {
        viewModelScope.launch {
            val raw = _rawMovie.value ?: return@launch
            try {
                val response = withContext(Dispatchers.IO) { service.fetchMovieDetails(raw) }
                withContext(Dispatchers.IO) { repository.update(movie = raw, detailResponse = response) }
                loadCachedMovie()
            } catch (_: Exception) {

            }
        }
    }

    fun toggleFavourite() {
        viewModelScope.launch {
            val raw = _rawMovie.value ?: return@launch
            withContext(Dispatchers.IO) {
                repository.updateMovie(movie = raw, isFavourite = !(raw.favourite ?: false))
            }
            loadCachedMovie()
        }
    }

    private fun loadCachedMovie() {
        viewModelScope.launch {
            val movie = withContext(Dispatchers.IO) {
                repository.fetchCachedMovies(id = movieId).firstOrNull()
            } ?: return@launch
            _rawMovie.value = movie
            _display.value = movie.toDisplay()
        }
    }

    private fun Movie.toDisplay(): MovieDetailDisplay = MovieDetailDisplay(
        id = id,
        title = title,
        posterUrl = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
        rating = String.format(Locale.US, "%.1f", voteAverage),
        year = if ((releaseDate?.length ?: 0) >= 4) releaseDate!!.take(4) else "N/A",
        runtime = if ((runtime ?: 0) > 0) "$runtime min" else "N/A",
        overview = overview.ifEmpty { "No overview available." },
        genres = genres?.map { it.name } ?: emptyList(),
        isFavourite = favourite ?: false
    )

    class Factory(
        private val movieId: Int,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            return MovieDetailController(
                movieId = movieId,
                service = MovieService(),
                repository = MovieRepository(db)
            ) as T
        }
    }
}
