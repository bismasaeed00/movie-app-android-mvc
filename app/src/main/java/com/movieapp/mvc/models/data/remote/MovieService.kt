package com.movieapp.mvc.models.data.remote

import com.movieapp.mvc.models.data.Movie
import com.movieapp.mvc.models.data.MovieDetailsResponse
import com.movieapp.mvc.models.data.MovieResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ─── Error type ───────────────────────────────────────────────────────────────
// Mirrors: enum MovieServiceError in MovieService.swift

sealed class MovieServiceError(message: String) : Exception(message) {
    object InvalidURL : MovieServiceError("Invalid URL.")
    data class NetworkError(val cause: Throwable) : MovieServiceError("Network error: ${cause.localizedMessage}")
    data class DecodingError(val cause: Throwable) : MovieServiceError("Decoding error: ${cause.localizedMessage}")
    data class HttpError(val code: Int) : MovieServiceError("HTTP error $code.")
}

// ─── Retrofit interface ───────────────────────────────────────────────────────
// Mirrors the two URLSession calls inside MovieService.swift

interface TmdbApiService {

    /** Mirrors: func fetchMovies(page: Int) async throws -> MovieResponse */
    @GET("discover/movie")
    suspend fun fetchMovies(
        @Query("api_key") apiKey: String,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("page") page: Int
    ): Response<MovieResponse>

    /** Mirrors: func fetchMovieDetails(_ movie: Movie) async throws -> MovieDetailsResponse */
    @GET("movie/{movieId}")
    suspend fun fetchMovieDetails(
        @Path("movieId") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Response<MovieDetailsResponse>
}

// ─── Service class ────────────────────────────────────────────────────────────
// Mirrors: final class MovieService in MovieService.swift

class MovieService {

    // Mirrors: private let apiKey = "..."
    private val apiKey = "3db0f53e603350e7a4fda7a09419f20a"

    // Mirrors: private let moviesBaseURL = "https://api.themoviedb.org/3/discover/movie"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: TmdbApiService = retrofit.create(TmdbApiService::class.java)

    /** Mirrors: func fetchMovies(page: Int = 1) async throws -> MovieResponse */
    suspend fun fetchMovies(page: Int = 1): MovieResponse {
        val response = try {
            api.fetchMovies(apiKey = apiKey, page = page)
        } catch (e: Exception) {
            throw MovieServiceError.NetworkError(e)
        }

        if (!response.isSuccessful) throw MovieServiceError.HttpError(response.code())

        return response.body() ?: throw MovieServiceError.DecodingError(
            Exception("Empty response body")
        )
    }

    /** Mirrors: func fetchMovieDetails(_ movie: Movie) async throws -> MovieDetailsResponse */
    suspend fun fetchMovieDetails(movie: Movie): MovieDetailsResponse {
        val response = try {
            api.fetchMovieDetails(movieId = movie.id, apiKey = apiKey)
        } catch (e: Exception) {
            throw MovieServiceError.NetworkError(e)
        }

        if (!response.isSuccessful) throw MovieServiceError.HttpError(response.code())

        return response.body() ?: throw MovieServiceError.DecodingError(
            Exception("Empty response body")
        )
    }
}
