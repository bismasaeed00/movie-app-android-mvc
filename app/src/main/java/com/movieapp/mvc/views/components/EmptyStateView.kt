package com.movieapp.mvc.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── EmptyStateView ───────────────────────────────────────────────────────────
// Mirrors: struct EmptyStateView: View in EmptyStateView.swift
//
// ROLE (MVC): Dumb View — no logic, no state, purely declarative presentation.

@Composable
fun EmptyStateView() {
    // Mirrors: VStack(spacing: 16) { ... }.frame(maxWidth: .infinity, maxHeight: .infinity)
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mirrors: Image(systemName: "film.stack").font(.system(size: 52)).foregroundColor(.secondary)
        Icon(
            imageVector = Icons.Filled.Movie,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Mirrors: Text("No Movies Yet").font(.title3.bold())
        Text(
            text = "No Movies Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Mirrors: Text("Pull down to load movies from the network.")
        Text(
            text = "Pull down to load movies from the network.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
