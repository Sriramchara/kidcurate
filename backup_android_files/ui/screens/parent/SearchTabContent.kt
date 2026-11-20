package com.kidcurate.app.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.data.VideoSearchResultItem
import com.kidcurate.app.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchAndAddTab(
    selectedChildName: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onPerformSearch: () -> Unit,
    searchResults: List<VideoSearchResultItem>,
    isSearching: Boolean,
    onAddVideo: (VideoSearchResultItem) -> Unit,
    searchSuggestions: List<String> = emptyList(),
    existingVideoIds: Set<String> = emptySet(),
) {
    val context = LocalContext.current
    val parentPrefs = remember { ParentPreferences.get(context) }
    var recentSearches by remember { mutableStateOf(parentPrefs.getRecentSearches()) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val handleSearch: (String) -> Unit = { queryToSearch ->
        val trimmed = queryToSearch.trim()
        if (trimmed.isNotBlank()) {
            parentPrefs.addRecentSearch(trimmed)
            recentSearches = parentPrefs.getRecentSearches()
        }
        onPerformSearch()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Search bar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BackgroundCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = BrandPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search safe educational YouTube videos...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { handleSearch(searchQuery) }),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Button(
                            onClick = { handleSearch(searchQuery) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Search", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Dynamic suggestions if typing
            if (searchSuggestions.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BackgroundCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        searchSuggestions.take(6).forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSearchQueryChange(suggestion)
                                        handleSearch(suggestion)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = suggestion,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isSearching) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Searching safe YouTube videos for \"$searchQuery\"...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandPrimary,
                        )
                    }
                }
            }
        } else if (searchResults.isEmpty()) {
            if (searchQuery.isBlank() && recentSearches.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Recent Searches",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                )
                            }
                            TextButton(
                                onClick = {
                                    parentPrefs.clearRecentSearches()
                                    recentSearches = emptyList()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "Clear All",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            recentSearches.forEach { queryItem ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = BackgroundCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                                    shadowElevation = 1.dp,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .clickable {
                                                    onSearchQueryChange(queryItem)
                                                    handleSearch(queryItem)
                                                },
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                Icons.Rounded.Search,
                                                contentDescription = null,
                                                tint = BrandPrimary,
                                                modifier = Modifier.size(15.dp),
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = queryItem,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary,
                                            )
                                        }
                                        Spacer(Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                parentPrefs.removeRecentSearch(queryItem)
                                                recentSearches = parentPrefs.getRecentSearches()
                                            },
                                            modifier = Modifier.size(24.dp),
                                        ) {
                                            Icon(
                                                Icons.Rounded.Close,
                                                contentDescription = "Remove search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No videos found for \"$searchQuery\"" else "Search Safe YouTube Videos",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tip: You can search any educational topic (Phonics, Science, Animals) or paste ANY direct YouTube video link to add it instantly!",
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(searchResults, key = { it.youtubeId }) { video ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BackgroundCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    shadowElevation = 1.dp,
                ) {
                    if (isLandscape) {
                        // Landscape: compact horizontal card
                        val isAlreadyAdded = existingVideoIds.contains(video.youtubeId)
                        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(130.dp).height(76.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.08f))) {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current).data(video.thumbnailUrl).crossfade(true).build(),
                                    contentDescription = video.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                if (video.durationSeconds != null && video.durationSeconds > 0) {
                                    val mins = video.durationSeconds / 60; val secs = video.durationSeconds % 60
                                    Surface(shape = RoundedCornerShape(5.dp), color = Color.Black.copy(alpha = 0.82f), modifier = Modifier.align(Alignment.BottomEnd).padding(5.dp)) {
                                        Text(text = String.format(java.util.Locale.US, "%d:%02d", mins, secs), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = video.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(3.dp))
                                Text(text = video.channelName, fontSize = 11.sp, color = TextSecondary)
                            }
                            Spacer(Modifier.width(8.dp))
                            if (isAlreadyAdded) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = BrandSuccess, modifier = Modifier.size(28.dp))
                            } else {
                                Button(onClick = { onAddVideo(video) }, colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.08f)),
                        ) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(LocalContext.current)
                                    .data(video.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = video.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandSuccess,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Parent review required",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                            if (video.durationSeconds != null && video.durationSeconds > 0) {
                                val mins = video.durationSeconds / 60
                                val secs = video.durationSeconds % 60
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.82f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp),
                                ) {
                                    Text(
                                        text = String.format(java.util.Locale.US, "%d:%02d", mins, secs),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = video.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = video.channelName,
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )

                        Spacer(Modifier.height(10.dp))

                        val isAlreadyAdded = existingVideoIds.contains(video.youtubeId)
                        if (isAlreadyAdded) {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = BrandSuccess.copy(alpha = 0.15f),
                                    disabledContentColor = BrandSuccess,
                                ),
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandSuccess)
                                Spacer(Modifier.width(6.dp))
                                Text("Already in $selectedChildName's Playlist", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandSuccess)
                            }
                        } else {
                            Button(
                                onClick = { onAddVideo(video) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Add to $selectedChildName's Playlist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    } // end portrait else
                }
            }
        }
    }
}
