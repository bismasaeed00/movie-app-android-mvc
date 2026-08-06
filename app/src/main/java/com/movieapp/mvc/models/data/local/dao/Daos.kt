package com.movieapp.mvc.models.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.movieapp.mvc.models.data.local.entities.GenreEntity
import com.movieapp.mvc.models.data.local.entities.MovieEntity
import com.movieapp.mvc.models.data.local.entities.ResponseEntity

@Dao
interface ResponseDao {
    @Query("SELECT * FROM response_entity WHERE page = :page LIMIT 1")
    fun getByPage(page: Long): ResponseEntity?

    @Query("SELECT * FROM response_entity")
    fun getAll(): List<ResponseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ResponseEntity)
}

@Dao
interface MovieDao {

    @Query("SELECT * FROM movie_entity")
    fun getAll(): List<MovieEntity>

    @Query("SELECT * FROM movie_entity WHERE id = :id")
    fun getById(id: Long): MovieEntity?

    @Query("SELECT id FROM movie_entity WHERE favourite = 1")
    fun getFavouriteIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: MovieEntity)

    @Update
    fun update(entity: MovieEntity)

    @Query("UPDATE movie_entity SET favourite = :isFavourite WHERE id = :id")
    fun updateFavourite(id: Long, isFavourite: Boolean)
}

@Dao
interface GenreDao {

    @Query("SELECT * FROM genre_entity WHERE movieId = :movieId")
    fun getByMovieId(movieId: Long): List<GenreEntity>

    @Query("DELETE FROM genre_entity WHERE movieId = :movieId")
    fun deleteByMovieId(movieId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: GenreEntity)
}
