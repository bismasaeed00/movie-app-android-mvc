package com.movieapp.mvc.views.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.movieapp.mvc.controllers.MovieDetailController
import com.movieapp.mvc.models.data.Movie

// ─── MovieDetailScreen ────────────────────────────────────────────────────────
// Mirrors: struct MovieDetailView: View in MovieDetailView.swift
//
// ROLE (MVC): Dumb View — reads state from MovieDetailController via StateFlow.
//             Forwards user events (toggleFavourite, loadMovieDetails) to the Controller.
//             Contains zero business logic.

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MovieDetailScreen(
    // Mirrors: @StateObject private var controller: MovieDetailController
    //          init(controller: MovieDetailController) { _controller = StateObject(wrappedValue: controller) }
    movie: Movie,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val controller: MovieDetailController = viewModel(
        key = "detail_${movie.id}",
        factory = MovieDetailController.Factory(movie, context)
    )

    // Collect StateFlows — mirrors @Published @StateObject observation in SwiftUI
    val currentMovie by controller.movie.collectAsState()

    // Mirrors: .task { await controller.loadMovieDetails() }
    LaunchedEffect(movie.id) {
        controller.loadMovieDetails()
    }

    Scaffold(
        // Mirrors: .navigationTitle(controller.movie.title).navigationBarTitleDisplayMode(.inline)
        topBar = {
            TopAppBar(
                title = { Text(currentMovie.title, maxLines = 1) },
                actions = {
                    // Mirrors: ToolbarItem(placement: .navigationBarTrailing) { favouriteButton }
                    FavouriteButton(
                        isFavourite = controller.isFavourite,
                        onToggle = { controller.toggleFavourite() }  // forwards event to Controller
                    )
                }
            )
        }
    ) { paddingValues ->
        // Mirrors: ScrollView { VStack(alignment: .leading, spacing: 20) { ... } }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Mirrors: posterView
            PosterView(posterURL = currentMovie.posterURL)

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mirrors: Text(controller.movie.title).font(.title.bold())
                Text(
                    text = currentMovie.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                // Mirrors: HStack(spacing: 12) { Label(formattedYear) Label(rating) }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentMovie.formattedYear,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color.Yellow)
                        Text(
                            text = controller.rating,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // Mirrors: HStack { Text("Runtime: ").font(.headline) Text("\(runtime ?? 0) min") }
                Row {
                    Text("Runtime: ", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${currentMovie.runtime ?: 0} min",
                        fontSize = 14.sp
                    )
                }

                HorizontalDivider()

                // Mirrors: Text("Overview").font(.headline)
                Text("Overview", fontWeight = FontWeight.SemiBold)

                // Mirrors: Text(controller.overview).font(.body).foregroundColor(.secondary)
                Text(
                    text = controller.overview,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Mirrors: genreChips — FlowLayout of genre Capsule chips
                val genres = currentMovie.genres
                if (!genres.isNullOrEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        genres.forEach { genre ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Blue
                            ) {
                                Text(
                                    text = genre.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ─── FavouriteButton ──────────────────────────────────────────────────────────
// Mirrors: private var favouriteButton: some View { Button { controller.toggleFavourite() } ... }
@Composable
private fun FavouriteButton(
    isFavourite: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
            tint = if (isFavourite) Color.Red else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─── PosterView ───────────────────────────────────────────────────────────────
// Mirrors: private var posterView: some View { AsyncImage(url: ...) ... }
@Composable
private fun PosterView(posterURL: String?) {
    AsyncImage(
        model = posterURL,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
    )
}
