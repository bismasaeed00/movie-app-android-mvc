package com.movieapp.mvc.models.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.movieapp.mvc.models.data.local.entities.GenreEntity
import com.movieapp.mvc.models.data.local.entities.MovieEntity
import com.movieapp.mvc.models.data.local.entities.ResponseEntity

// ─── DAOs ─────────────────────────────────────────────────────────────────────
// Each @Query mirrors an NSFetchRequest predicate used in MovieRepository.swift

@Dao
interface ResponseDao {

    /** Mirrors: NSFetchRequest<ResponseEntity> with predicate "page == %d" */
    @Query("SELECT * FROM response_entity WHERE page = :page LIMIT 1")
    fun getByPage(page: Long): ResponseEntity?

    /** Mirrors: NSFetchRequest<ResponseEntity> with no predicate (all pages) */
    @Query("SELECT * FROM response_entity")
    fun getAll(): List<ResponseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ResponseEntity)
}

@Dao
interface MovieDao {

    /** Mirrors: NSFetchRequest<MovieEntity> with no predicate */
    @Query("SELECT * FROM movie_entity")
    fun getAll(): List<MovieEntity>

    /** Mirrors: NSFetchRequest<MovieEntity> with predicate "id == %@" */
    @Query("SELECT * FROM movie_entity WHERE id = :id")
    fun getById(id: Long): MovieEntity?

    /** Mirrors: NSFetchRequest<MovieEntity> with predicate "favourite == true", propertiesToFetch: ["id"] */
    @Query("SELECT id FROM movie_entity WHERE favourite = 1")
    fun getFavouriteIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: MovieEntity)

    /** Mirrors: existingMovie.title = …; existingMovie.voteAverage = … etc. */
    @Update
    fun update(entity: MovieEntity)

    /** Mirrors: matchedMovie.favourite = isFavourite */
    @Query("UPDATE movie_entity SET favourite = :isFavourite WHERE id = :id")
    fun updateFavourite(id: Long, isFavourite: Boolean)
}

@Dao
interface GenreDao {

    /** Mirrors: entity.genres as? Set<GenreEntity> lookup by movie id */
    @Query("SELECT * FROM genre_entity WHERE movieId = :movieId")
    fun getByMovieId(movieId: Long): List<GenreEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: GenreEntity)
}
