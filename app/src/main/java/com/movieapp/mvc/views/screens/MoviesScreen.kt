package com.movieapp.mvc.views.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.movieapp.mvc.models.display.MovieListItemDisplay
import com.movieapp.mvc.views.components.EmptyStateView
import com.movieapp.mvc.views.components.ErrorView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    controller: MoviesViewController = viewModel(
        factory = MoviesViewController.Factory(LocalContext.current)
    ),
    onMovieSelected: (Int) -> Unit
) {
    val movies by controller.movies.collectAsState()
    val favouriteMovies by controller.favouriteMovies.collectAsState()
    val isLoading by controller.isLoading.collectAsState()
    val errorMessage by controller.errorMessage.collectAsState()
    val selectedTab by controller.selectedTab.collectAsState()
    val hasMorePages by controller.hasMorePages.collectAsState()
    val loadMoreTitle by controller.loadMoreTitle.collectAsState()

    // Consume system back at the root screen so the user cannot accidentally exit
    // to the launcher via the back gesture/button
    BackHandler {}

    LaunchedEffect(Unit) { controller.loadNetworkData() }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { controller.selectTab(0) },
                    icon = { Icon(Icons.Filled.Movie, contentDescription = "Movies") },
                    label = { Text("Movies") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { controller.selectTab(1) },
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
                hasMorePages = hasMorePages,
                loadMoreTitle = loadMoreTitle,
                onRefresh = { controller.refresh() },
                onRetry = { controller.loadNetworkData() },
                onLoadMore = { controller.loadNextPage() },
                onMovieSelected = onMovieSelected,
                modifier = Modifier.padding(paddingValues)
            )
            1 -> FavouritesTab(
                movies = favouriteMovies,
                onMovieSelected = onMovieSelected,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoviesTab(
    movies: List<MovieListItemDisplay>,
    isLoading: Boolean,
    errorMessage: String?,
    hasMorePages: Boolean,
    loadMoreTitle: String,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onMovieSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Movies", fontWeight = FontWeight.Bold, fontSize = 28.sp) },
                actions = {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            MoviesContent(
                movies = movies,
                isLoading = isLoading,
                errorMessage = errorMessage,
                hasMorePages = hasMorePages,
                loadMoreTitle = loadMoreTitle,
                onRetry = onRetry,
                onLoadMore = onLoadMore,
                onMovieSelected = onMovieSelected
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavouritesTab(
    movies: List<MovieListItemDisplay>,
    onMovieSelected: (Int) -> Unit,
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
            isLoading = false,
            hasMorePages = false,
            loadMoreTitle = "",
            onLoadMore = {},
            onMovieSelected = onMovieSelected,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun MoviesContent(
    movies: List<MovieListItemDisplay>,
    isLoading: Boolean,
    errorMessage: String?,
    hasMorePages: Boolean,
    loadMoreTitle: String,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onMovieSelected: (Int) -> Unit
) {
    when {
        errorMessage != null && movies.isEmpty() -> ErrorView(
            message = errorMessage,
            onRetry = onRetry
        )
        movies.isEmpty() && !isLoading -> EmptyStateView()
        else -> MovieList(
            movies = movies,
            hasFooter = true,
            isLoading = isLoading,
            hasMorePages = hasMorePages,
            loadMoreTitle = loadMoreTitle,
            onLoadMore = onLoadMore,
            onMovieSelected = onMovieSelected
        )
    }
}

@Composable
private fun MovieList(
    movies: List<MovieListItemDisplay>,
    hasFooter: Boolean,
    isLoading: Boolean,
    hasMorePages: Boolean,
    loadMoreTitle: String,
    onLoadMore: () -> Unit,
    onMovieSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(movies, key = { it.id }) { item ->
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                MovieRow(item = item, onClick = { onMovieSelected(item.id) })
            }
        }

        if (hasFooter) {
            item {
                FooterView(
                    isLoading = isLoading,
                    hasMorePages = hasMorePages,
                    loadMoreTitle = loadMoreTitle,
                    onLoadMore = onLoadMore
                )
            }
        }
    }
}

@Composable
private fun FooterView(
    isLoading: Boolean,
    hasMorePages: Boolean,
    loadMoreTitle: String,
    onLoadMore: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            hasMorePages -> Button(onClick = onLoadMore) { Text(loadMoreTitle) }
        }
    }
}

@Composable
private fun MovieRow(item: MovieListItemDisplay, onClick: () -> Unit) {
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
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            PosterThumbnail(posterUrl = item.posterUrl)
            MovieInfoColumn(item = item, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PosterThumbnail(posterUrl: String?) {
    AsyncImage(
        model = posterUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.width(70.dp).height(105.dp).clip(RoundedCornerShape(8.dp))
    )
}

@Composable
private fun MovieInfoColumn(item: MovieListItemDisplay, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = item.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 2)

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
            Text(text = item.rating, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = item.year, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Text(
            text = item.overview,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            lineHeight = 16.sp
        )
    }
}
