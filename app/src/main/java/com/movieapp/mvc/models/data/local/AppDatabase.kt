package com.movieapp.mvc.models.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.movieapp.mvc.models.data.local.dao.GenreDao
import com.movieapp.mvc.models.data.local.dao.MovieDao
import com.movieapp.mvc.models.data.local.dao.ResponseDao
import com.movieapp.mvc.models.data.local.entities.GenreEntity
import com.movieapp.mvc.models.data.local.entities.MovieEntity
import com.movieapp.mvc.models.data.local.entities.ResponseEntity

// ─── RoomDatabase ─────────────────────────────────────────────────────────────
// Mirrors: CoreDataStack.swift
//   - Singleton pattern (companion object) → static let shared = CoreDataStack()
//   - persistentContainer / viewContext    → RoomDatabase instance / DAOs
//   - saveContext()                        → Room transactions are auto-committed per DAO call;
//                                            explicit transactions used in MovieRepository

@Database(
    entities = [ResponseEntity::class, MovieEntity::class, GenreEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun responseDao(): ResponseDao
    abstract fun movieDao(): MovieDao
    abstract fun genreDao(): GenreDao

    companion object {
        // Mirrors: static let shared = CoreDataStack()
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Mirrors: private init() { /* restrict re-init of singletons */ }
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "MovieAppMVC"           // mirrors NSPersistentContainer(name: "MovieAppMVC")
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
