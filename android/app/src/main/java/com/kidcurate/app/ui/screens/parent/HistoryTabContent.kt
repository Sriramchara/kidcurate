package com.kidcurate.app.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kidcurate.app.data.*
import com.kidcurate.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HistoryTabContent(
    managedChildren: List<ChildManageItem>,
    onSelectSearchTerm: (String) -> Unit,
    onReAddVideo: (CuratedHistoryItem) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val parentPrefs = remember { ParentPreferences.get(context) }

    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Curated History", "Watch Activity", "Search Queries")

    var selectedChildFilterId by remember { mutableStateOf("") } // empty = all children

    var curatedHistory by remember { mutableStateOf<List<CuratedHistoryItem>>(emptyList()) }
    var isLoadingCurated by remember { mutableStateOf(false) }

    var watchHistory by remember { mutableStateOf<List<WatchHistoryItem>>(emptyList()) }
    var isLoadingWatch by remember { mutableStateOf(false) }

    var searchHistory by remember { mutableStateOf(parentPrefs.getRecentSearches()) }

    fun loadCuratedHistory() {
        scope.launch {
            isLoadingCurated = true
            try {
                val childIdParam = if (selectedChildFilterId.isNotBlank()) selectedChildFilterId else null
                val res = ApiClient.api.getCurationHistory(childId = childIdParam, limit = 60)
                curatedHistory = res.history
            } catch (_: Exception) {
                curatedHistory = emptyList()
            } finally {
                isLoadingCurated = false
            }
        }
    }

    fun loadWatchHistory() {
        val targetChildId = if (selectedChildFilterId.isNotBlank()) {
            selectedChildFilterId
        } else {
            managedChildren.firstOrNull()?.id ?: ""
        }

        if (targetChildId.isBlank()) {
            watchHistory = emptyList()
            return
        }

        scope.launch {
            isLoadingWatch = true
            try {
                val res = ApiClient.api.getWatchHistory(childId = targetChildId, limit = 50)
                watchHistory = res.history
            } catch (_: Exception) {
                watchHistory = emptyList()
            } finally {
                isLoadingWatch = false
            }
        }
    }

    LaunchedEffect(selectedSubTab, selectedChildFilterId) {
        if (selectedSubTab == 0) {
            loadCuratedHistory()
        } else if (selectedSubTab == 1) {
            loadWatchHistory()
        } else {
            searchHistory = parentPrefs.getRecentSearches()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Child filter chips
        if (managedChildren.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedChildFilterId.isEmpty(),
                        onClick = { selectedChildFilterId = "" },
                        label = { Text("All Children", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary,
                            selectedLabelColor = Color.White,
                        ),
                    )

                    managedChildren.forEach { child ->
                        FilterChip(
                            selected = selectedChildFilterId == child.id,
                            onClick = { selectedChildFilterId = child.id },
                            label = { Text(child.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandPrimary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }
        }

        // Sub-tabs segment
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    subTabs.forEachIndexed { index, title ->
                        val isSelected = selectedSubTab == index
                        Surface(
                            onClick = { selectedSubTab = index },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BackgroundCard else Color.Transparent,
                            shadowElevation = if (isSelected) 1.dp else 0.dp,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }

        // SubTab 0: Curated History
        if (selectedSubTab == 0) {
            if (isLoadingCurated) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    }
                }
            } else if (curatedHistory.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.PlaylistAddCheck, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No Curated History Yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "When you add videos or apply activity packs to your children's playlists, the activity audit log will appear here.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(curatedHistory, key = { it.id }) { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BackgroundCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                        shadowElevation = 1.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp, 52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = item.thumbnailUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Spacer(Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    val actionColor = when (item.action) {
                                        "removed" -> MaterialTheme.colorScheme.error
                                        "pack_applied" -> BrandViolet
                                        else -> Color(0xFF10B981)
                                    }
                                    val actionLabel = when (item.action) {
                                        "removed" -> "Removed"
                                        "pack_applied" -> "Pack Added"
                                        else -> "Curated"
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = actionColor.copy(alpha = 0.15f),
                                    ) {
                                        Text(
                                            text = actionLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = actionColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }

                                    if (item.childName.isNotBlank()) {
                                        Text(
                                            text = "for ${item.childName}",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                }

                                if (item.createdAt.isNotBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = item.createdAt.take(16).replace("T", " "),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                }
                            }

                            if (item.action == "removed") {
                                IconButton(
                                    onClick = { onReAddVideo(item) },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.AddCircleOutline,
                                        contentDescription = "Re-add video",
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SubTab 1: Watch Activity
        if (selectedSubTab == 1) {
            if (isLoadingWatch) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    }
                }
            } else if (watchHistory.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(Icons.Rounded.Visibility, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No Watch Activity Recorded",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "When your child plays videos in Kids Mode, their viewing time and completion marks will show up here.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(watchHistory) { watch ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BackgroundCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                        shadowElevation = 1.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp, 52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = watch.thumbnailUrl,
                                    contentDescription = watch.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                if (watch.completed) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(2.dp)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = "Completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp),
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = watch.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Spacer(Modifier.height(4.dp))

                                val watchedMin = watch.watchedSeconds / 60
                                val watchedSec = watch.watchedSeconds % 60
                                val watchTimeText = "${watchedMin}m ${watchedSec}s"

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = "Watched $watchTimeText",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BrandPrimary,
                                    )

                                    if (watch.completed) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                                        ) {
                                            Text(
                                                text = "Completed",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                }

                                if (!watch.watchedAt.isNullOrBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = watch.watchedAt.take(16).replace("T", " "),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SubTab 2: Search Queries
        if (selectedSubTab == 2) {
            if (searchHistory.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(Icons.Rounded.Search, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No Search History",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Queries you search for when finding safe educational videos will be logged here for convenient 1-tap re-use.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${searchHistory.size} Recent Queries",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                        )
                        TextButton(
                            onClick = {
                                parentPrefs.clearRecentSearches()
                                searchHistory = emptyList()
                            },
                        ) {
                            Text("Clear All Queries", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                items(searchHistory) { query ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BackgroundCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                        shadowElevation = 1.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectSearchTerm(query) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = query,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                FilledTonalButton(
                                    onClick = { onSelectSearchTerm(query) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp),
                                ) {
                                    Text("Search", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = {
                                        parentPrefs.removeRecentSearch(query)
                                        searchHistory = parentPrefs.getRecentSearches()
                                    },
                                    modifier = Modifier.size(30.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Remove query",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
