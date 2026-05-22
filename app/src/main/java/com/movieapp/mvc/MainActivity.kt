package com.movieapp.mvc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.movieapp.mvc.models.data.Movie
import com.movieapp.mvc.views.screens.MovieDetailScreen
import com.movieapp.mvc.views.screens.MoviesScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// ─── MainActivity ─────────────────────────────────────────────────────────────
// Mirrors: @main struct MovieApp: App { WindowGroup { MoviesView() } } in MovieApp.swift
//
// Sets up the Compose content root and the navigation graph that connects
// MoviesScreen → MovieDetailScreen, mirroring the NavigationStack / NavigationLink
// established in MoviesView.swift.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val gson = remember { Gson() }

                // Mirrors: NavigationStack { ... NavigationLink(destination: MovieDetailView(...)) }
                NavHost(
                    navController = navController,
                    startDestination = "movies"
                ) {
                    // Mirrors: MoviesView() — the root WindowGroup scene
                    composable("movies") {
                        MoviesScreen(
                            onMovieSelected = { movie ->
                                val json = URLEncoder.encode(
                                    gson.toJson(movie),
                                    StandardCharsets.UTF_8.toString()
                                )
                                navController.navigate("detail/$json")
                            }
                        )
                    }

                    // Mirrors: NavigationLink destination: MovieDetailView(controller: MovieDetailController(movie: movie))
                    composable(
                        route = "detail/{movieJson}",
                        arguments = listOf(navArgument("movieJson") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val json = URLDecoder.decode(
                            backStackEntry.arguments?.getString("movieJson") ?: "",
                            StandardCharsets.UTF_8.toString()
                        )
                        val movie = gson.fromJson(json, Movie::class.java)
                        MovieDetailScreen(
                            movie = movie,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

// Helper — avoids importing an extra remember import at call site
@Composable
private fun <T> remember(calculation: () -> T): T =
    androidx.compose.runtime.remember { calculation() }
