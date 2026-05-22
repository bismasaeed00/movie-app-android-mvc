package com.movieapp.mvc.models.repository

import com.movieapp.mvc.models.data.Genre
import com.movieapp.mvc.models.data.Movie
import com.movieapp.mvc.models.data.MovieDetailsResponse
import com.movieapp.mvc.models.data.MovieResponse
import com.movieapp.mvc.models.data.local.AppDatabase
import com.movieapp.mvc.models.data.local.entities.GenreEntity
import com.movieapp.mvc.models.data.local.entities.MovieEntity
import com.movieapp.mvc.models.data.local.entities.ResponseEntity

// ─── MovieRepository ──────────────────────────────────────────────────────────
// Mirrors: final class MovieRepository in MovieRepository.swift
// CoreData → Room mapping:
//   stack.context.fetch(request)      → dao.getAll() / dao.getById() etc.
//   stack.saveContext()               → Room auto-commits per DAO call;
//                                       runInTransaction used for multi-step writes
//   NSFetchRequest predicates         → SQL WHERE clauses in @Query annotations

class MovieRepository(private val db: AppDatabase) {

    // ─── save(response:for:) ──────────────────────────────────────────────────
    // Mirrors: func save(response: MovieResponse, for page: Int)
    fun save(response: MovieResponse, for page: Int) {
        db.runInTransaction {
            val cachedResponse = db.responseDao().getByPage(page.toLong())

            if (cachedResponse != null) {
                // Mirrors: "If page exists → update existing movies"
                val existingMovies = getExistingMovieIDs(page.toLong())

                for (movie in response.results) {
                    val existingEntity = existingMovies[movie.id.toLong()]
                    if (existingEntity != null) {
                        // Mirrors: existingMovie.title = movie.title; etc.
                        db.movieDao().update(
                            existingEntity.copy(
                                title = movie.title,
                                overview = movie.overview,
                                posterPath = movie.posterPath,
                                voteAverage = movie.voteAverage
                            )
                        )
                    } else {
                        // Mirrors: let movieEntity = createNewMovieEntity(movie: movie); cachedResponse.addToMovies(movieEntity)
                        val newEntity = createNewMovieEntity(movie, responsePage = page.toLong())
                        db.movieDao().insert(newEntity)
                    }
                }
            } else {
                // Mirrors: "No cached page → create new response entity"
                val responseEntity = ResponseEntity(
                    page = response.page.toLong(),
                    totalPages = response.totalPages.toLong()
                )
                db.responseDao().insert(responseEntity)

                for (movie in response.results) {
                    val movieEntity = createNewMovieEntity(movie, responsePage = response.page.toLong())
                    db.movieDao().insert(movieEntity)
                }
            }
        }
    }

    // ─── update(movie:detailResponse:) ───────────────────────────────────────
    // Mirrors: func update(movie: Movie, detailResponse: MovieDetailsResponse)
    fun update(movie: Movie, detailResponse: MovieDetailsResponse) {
        db.runInTransaction {
            val entity = db.movieDao().getById(movie.id.toLong()) ?: return@runInTransaction

            for (genre in detailResponse.genres) {
                val genreEntity = GenreEntity(
                    id = genre.id.toLong(),
                    name = genre.name,
                    movieId = entity.id          // mirrors: entity?.addToGenres(genreEntity)
                )
                db.genreDao().insert(genreEntity)
            }

            // Mirrors: entity?.runtime = Int64(detailResponse.runtime)
            db.movieDao().update(entity.copy(runtime = detailResponse.runtime.toLong()))
        }
    }

    // ─── fetchCachedMovies(id:) ───────────────────────────────────────────────
    // Mirrors: func fetchCachedMovies(id: Int? = nil) -> [Movie]
    fun fetchCachedMovies(id: Int? = null): List<Movie> {
        val entities = if (id != null) {
            // Mirrors: NSPredicate(format: "id == %@", NSNumber(value: id))
            listOfNotNull(db.movieDao().getById(id.toLong()))
        } else {
            db.movieDao().getAll()
        }

        return entities.map { entity ->
            // Mirrors: let genreSet = entity.genres as? Set<GenreEntity> ?? []
            val genreList = db.genreDao().getByMovieId(entity.id).map { genreEntity ->
                Genre(id = genreEntity.id.toInt(), name = genreEntity.name)
            }

            Movie(
                id = entity.id.toInt(),
                title = entity.title,
                overview = entity.overview,
                posterPath = entity.posterPath,
                releaseDate = entity.releaseDate,
                voteAverage = entity.voteAverage,
                favourite = entity.favourite,
                genres = genreList,
                runtime = entity.runtime.toInt()
            )
        }
    }

    // ─── fetchFavouriteMovieIDs() ─────────────────────────────────────────────
    // Mirrors: func fetchFavouriteMovieIDs() -> [Int]
    fun fetchFavouriteMovieIDs(): List<Int> {
        // Mirrors: NSPredicate(format: "favourite == %@", NSNumber(value: true))
        return db.movieDao().getFavouriteIds().map { it.toInt() }
    }

    // ─── fetchPaginationInfo() ────────────────────────────────────────────────
    // Mirrors: func fetchPaginationInfo() -> (highestPage: Int, totalPages: Int)
    fun fetchPaginationInfo(): Pair<Int, Int> {
        val entities = db.responseDao().getAll()

        if (entities.isEmpty()) return Pair(0, 0)

        // Mirrors: entities.map { Int($0.page) }.max() ?? 0
        val highestPage = entities.maxOfOrNull { it.page.toInt() } ?: 0

        // Mirrors: Int(entities.first?.total_pages ?? 0)
        val totalPages = entities.firstOrNull()?.totalPages?.toInt() ?: 0

        return Pair(highestPage, totalPages)
    }

    // ─── updateMovie(movie:isFavourite:) ──────────────────────────────────────
    // Mirrors: func updateMovie(movie: Movie, isFavourite: Bool)
    fun updateMovie(movie: Movie, isFavourite: Boolean) {
        // Mirrors: NSPredicate(format: "id == %d", movie.id), fetchLimit = 1
        db.movieDao().updateFavourite(id = movie.id.toLong(), isFavourite = isFavourite)
    }

    // ─── private helpers ──────────────────────────────────────────────────────

    /** Mirrors: private func createNewMovieEntity(movie: Movie) -> MovieEntity */
    private fun createNewMovieEntity(movie: Movie, responsePage: Long?): MovieEntity =
        MovieEntity(
            id = movie.id.toLong(),
            title = movie.title,
            overview = movie.overview,
            posterPath = movie.posterPath,
            releaseDate = movie.releaseDate,
            voteAverage = movie.voteAverage,
            runtime = 0L,
            favourite = false,
            responsePage = responsePage
        )

    /**
     * Mirrors: private func getExistingMovieIDs(responses: [ResponseEntity]) -> [Int: MovieEntity]
     * Builds a lookup map of existing MovieEntities for the given page.
     */
    private fun getExistingMovieIDs(page: Long): Map<Long, MovieEntity> {
        return db.movieDao().getAll()
            .filter { it.responsePage == page }
            .associateBy { it.id }
    }
}
