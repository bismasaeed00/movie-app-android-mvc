package com.movieapp.mvc.views.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.movieapp.mvc.controllers.MoviesViewController
import com.movieapp.mvc.models.data.Movie
import com.movieapp.mvc.views.components.EmptyStateView
import com.movieapp.mvc.views.components.ErrorView

// ─── MoviesScreen ─────────────────────────────────────────────────────────────
// Mirrors: struct MoviesView: View in MoviesView.swift
//
// ROLE (MVC): Dumb View — reads StateFlows from MoviesViewController.
//             Forwards all events (loadNetworkData, refresh, loadNextPage, onAppear)
//             to the Controller. No business logic lives here.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    // Mirrors: @StateObject private var controller = MoviesViewController()
    controller: MoviesViewController = viewModel(
        factory = MoviesViewController.Factory(LocalContext.current)
    ),
    onMovieSelected: (Movie) -> Unit
) {
    // Collect StateFlows — mirrors @Published @StateObject observation in SwiftUI
    val movies by controller.movies.collectAsState()
    val isLoading by controller.isLoading.collectAsState()
    val errorMessage by controller.errorMessage.collectAsState()

    // Mirrors: .task { await controller.loadNetworkData() }
    LaunchedEffect(Unit) {
        controller.loadNetworkData()
    }

    // Mirrors: TabView { ... }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        // Mirrors: .tabItem { Label("Movies") } / Label("Favourites") }
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Movie, contentDescription = "Movies") },
                    label = { Text("Movies") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favourites") },
                    label = { Text("Favourites") }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> MoviesTab(
                movies = movies,
                isLoading = isLoading,
                errorMessage = errorMessage,
                controller = controller,
                onMovieSelected = onMovieSelected,
                modifier = Modifier.padding(paddingValues)
            )

            1 -> FavouritesTab(
                // Mirrors: controller.movies.filter { controller.isFavorite(movie: $0) }
                movies = movies.filter { controller.isFavorite(it) },
                onMovieSelected = onMovieSelected,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

// ─── MoviesTab ────────────────────────────────────────────────────────────────
// Mirrors: first tab inside TabView — NavigationStack with moviesContent + refreshable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoviesTab(
    movies: List<Movie>,
    isLoading: Boolean,
    errorMessage: String?,
    controller: MoviesViewController,
    onMovieSelected: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            // Mirrors: .navigationTitle("Movies") + ToolbarItem ProgressView
            TopAppBar(
                title = { Text("Movies", fontWeight = FontWeight.Bold, fontSize = 28.sp) },
                actions = {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            )
        }
    ) { innerPadding ->
        // Mirrors: .refreshable { await controller.refresh() }
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { controller.refresh() },          // forwards event to Controller
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mirrors: @ViewBuilder private var moviesContent: some View { ... }
            MoviesContent(
                movies = movies,
                isLoading = isLoading,
                errorMessage = errorMessage,
                controller = controller,
                onMovieSelected = onMovieSelected
            )
        }
    }
}

// ─── FavouritesTab ────────────────────────────────────────────────────────────
// Mirrors: second tab — NavigationStack { movieList(movies: filtered, hasFooter: false) }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavouritesTab(
    movies: List<Movie>,
    onMovieSelected: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Favourites", fontWeight = FontWeight.Bold, fontSize = 28.sp) }
            )
        }
    ) { innerPadding ->
        MovieList(
            movies = movies,
            hasFooter = false,
            controller = null,
            onMovieSelected = onMovieSelected,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// ─── MoviesContent ────────────────────────────────────────────────────────────
// Mirrors: @ViewBuilder private var moviesContent: some View { if/else/else chain }
@Composable
private fun MoviesContent(
    movies: List<Movie>,
    isLoading: Boolean,
    errorMessage: String?,
    controller: MoviesViewController,
    onMovieSelected: (Movie) -> Unit
) {
    when {
        // Mirrors: if let error = controller.errorMessage, controller.movies.isEmpty
        errorMessage != null && movies.isEmpty -> ErrorView(
            message = errorMessage,
            onRetry = { controller.loadNetworkData() }   // forwards event to Controller
        )

        // Mirrors: else if controller.movies.isEmpty && !controller.isLoading
        movies.isEmpty && !isLoading -> EmptyStateView()

        // Mirrors: else { movieList(movies: controller.movies, hasFooter: true) }
        else -> MovieList(
            movies = movies,
            hasFooter = true,
            controller = controller,
            onMovieSelected = onMovieSelected
        )
    }
}

// ─── MovieList ────────────────────────────────────────────────────────────────
// Mirrors: private func movieList(movies: [Movie], hasFooter: Bool) -> some View { List { ... } }
@Composable
private fun MovieList(
    movies: List<Movie>,
    hasFooter: Boolean,
    controller: MoviesViewController?,
    onMovieSelected: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Mirrors: ForEach(movies) { movie in NavigationLink(...) { movieRow(movie: movie) } }
        items(movies, key = { it.id }) { movie ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                MovieRow(
                    movie = movie,
                    onClick = { onMovieSelected(movie) }
                )
            }
        }

        // Mirrors: footerView
        if (hasFooter && controller != null) {
            item {
                FooterView(controller = controller)
            }
        }
    }
}

// ─── FooterView ───────────────────────────────────────────────────────────────
// Mirrors: @ViewBuilder private var footerView: some View { ... }
@Composable
private fun FooterView(controller: MoviesViewController) {
    val isLoading by controller.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Mirrors: if controller.isLoading { HStack { Spacer(); ProgressView(); Spacer() } }
            isLoading -> CircularProgressIndicator()

            // Mirrors: else if controller.hasMorePages { Button(controller.loadMoreTitle) { ... } }
            controller.hasMorePages -> Button(
                onClick = { controller.loadNextPage() }  // forwards event to Controller
            ) {
                Text(controller.loadMoreTitle)
            }
        }
    }
}

// ─── MovieRow ─────────────────────────────────────────────────────────────────
// Mirrors: private func movieRow(movie: Movie) -> some View { HStack { posterImage; infoView } }
@Composable
private fun MovieRow(
    movie: Movie,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Mirrors: posterImage(posterURL: movie.posterURL)
            PosterThumbnail(posterURL = movie.posterURL)

            // Mirrors: infoView(movie: movie)
            MovieInfoColumn(movie = movie, modifier = Modifier.weight(1f))
        }
    }
}

// ─── PosterThumbnail ─────────────────────────────────────────────────────────
// Mirrors: private func posterImage(posterURL: URL?) -> some View { AsyncImage { ... } .frame(width:70,height:105) }
@Composable
private fun PosterThumbnail(posterURL: String?) {
    AsyncImage(
        model = posterURL,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .width(70.dp)
            .height(105.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}

// ─── MovieInfoColumn ──────────────────────────────────────────────────────────
// Mirrors: private func infoView(movie: Movie) -> some View { VStack(alignment: .leading, spacing: 6) { ... } }
@Composable
private fun MovieInfoColumn(movie: Movie, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Mirrors: Text(movie.title).font(.headline).lineLimit(2)
        Text(
            text = movie.title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            maxLines = 2
        )

        // Mirrors: HStack { Image(systemName: "star.fill") Text(voteAverage) Text("·") Text(formattedYear) }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = String.format("%.1f", movie.voteAverage),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = movie.formattedYear,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Mirrors: Text(movie.overview).font(.caption).foregroundColor(.secondary).lineLimit(3)
        Text(
            text = movie.overview,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            lineHeight = 16.sp
        )
    }
}
