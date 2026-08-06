package com.movieapp.mvc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.movieapp.mvc.views.screens.MovieDetailScreen
import com.movieapp.mvc.views.screens.MoviesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme { AppNavigation() }
        }
    }
}

@Composable
private fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "movies") {
        composable("movies") {
            MoviesScreen(
                onMovieSelected = { movieId -> navController.navigate("detail/$movieId") }
            )
        }

        composable(
            route = "detail/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
            MovieDetailScreen(
                movieId = movieId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
