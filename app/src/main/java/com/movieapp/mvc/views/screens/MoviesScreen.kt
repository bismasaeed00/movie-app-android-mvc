package com.movieapp.mvc.views.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

    // One Scaffold per root screen, carrying both the top bar and the tab bar, so
    // that window insets are applied exactly once. The tab bar is supplied to the
    // root screens rather than wrapped around navigation, so detail screens render
    // full-height without it.
    when (selectedTab) {
        0 -> MoviesTab(
            movies = movies,
            isLoading = isLoading,
            errorMessage = errorMessage,
            hasMorePages = hasMorePages,
            loadMoreTitle = loadMoreTitle,
            selectedTab = selectedTab,
            onSelectTab = { controller.selectTab(it) },
            onRefresh = { controller.refresh() },
            onRetry = { controller.loadNetworkData() },
            onLoadMore = { controller.loadNextPage() },
            onMovieSelected = onMovieSelected
        )
        1 -> FavouritesTab(
            movies = favouriteMovies,
            selectedTab = selectedTab,
            onSelectTab = { controller.selectTab(it) },
            onMovieSelected = onMovieSelected
        )
    }
}

@Composable
private fun AppBottomBar(selectedTab: Int, onSelectTab: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onSelectTab(0) },
            icon = { Icon(Icons.Filled.VideoLibrary, contentDescription = "Movies") },
            label = { Text("Movies") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onSelectTab(1) },
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favourites") },
            label = { Text("Favourites") }
        )
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
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onMovieSelected: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movies") },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .wrapContentWidth()
                        )
                    }
                }
            )
        },
        bottomBar = { AppBottomBar(selectedTab = selectedTab, onSelectTab = onSelectTab) }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                errorMessage != null && movies.isEmpty() -> {
                    ErrorView(message = errorMessage, onRetry = onRetry)
                }
                movies.isEmpty() && !isLoading -> {
                    EmptyStateView()
                }
                else -> {
                    MovieList(
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavouritesTab(
    movies: List<MovieListItemDisplay>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    onMovieSelected: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Favourites") })
        },
        bottomBar = { AppBottomBar(selectedTab = selectedTab, onSelectTab = onSelectTab) }
    ) { innerPadding ->
        if (movies.isEmpty()) {
            EmptyStateView(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.HeartBroken,
                        contentDescription = null,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                },
                title = "No Favourites Yet",
                message = "Mark movies as favourite on the detail page."
            )
        } else {
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
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(movies, key = { it.id }) { item ->
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                MovieRow(item = item, onClick = { onMovieSelected(item.id) })
            }
        }

        if (hasFooter) {
            item {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    hasMorePages -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = onLoadMore) {
                                Text(loadMoreTitle)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieRow(item: MovieListItemDisplay, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            PosterImage(posterUrl = item.posterUrl)
            Spacer(modifier = Modifier.width(14.dp))
            InfoSection(
                title = item.title,
                rating = item.rating,
                releaseYear = item.year,
                overview = item.overview
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PosterImage(posterUrl: String?) {
    Box(
        modifier = Modifier
            .size(width = 70.dp, height = 105.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.matchParentSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.Movie,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    rating: String,
    releaseYear: String,
    overview: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = " $rating · $releaseYear",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = overview,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
