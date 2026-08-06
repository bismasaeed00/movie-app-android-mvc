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

sealed class MovieServiceError(message: String) : Exception(message) {
    object InvalidURL : MovieServiceError("Invalid URL.")
    data class NetworkError(val error: Throwable) : MovieServiceError("Network error: ${error.message}")
    data class DecodingError(val error: Throwable) : MovieServiceError("Decoding error: ${error.message}")
    data class HttpError(val code: Int) : MovieServiceError("HTTP error $code.")
}

interface TmdbApiService {

    // Sorted by vote_count rather than popularity so that the dataset
    // participants work with is stable across sessions. Popularity is
    // recomputed daily by TMDB, which would give participants run in different
    // weeks different movies, different genres, and therefore different task
    // difficulty. Vote counts only accumulate, and the gaps between adjacent
    // titles on these pages are large enough that the ordering does not change
    // over the study period. `language` is pinned so that titles do not vary
    // with device locale.
    @GET("discover/movie")
    suspend fun fetchMovies(
        @Query("api_key") apiKey: String,
        @Query("sort_by") sortBy: String = "vote_count.desc",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int
    ): Response<MovieResponse>

    @GET("movie/{movieId}")
    suspend fun fetchMovieDetails(
        @Path("movieId") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Response<MovieDetailsResponse>
}

class MovieService {

    private val apiKey = "3db0f53e603350e7a4fda7a09419f20a"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: TmdbApiService = retrofit.create(TmdbApiService::class.java)

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
