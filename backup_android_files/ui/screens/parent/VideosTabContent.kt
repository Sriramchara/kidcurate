package com.kidcurate.app.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kidcurate.app.data.PlaylistData
import com.kidcurate.app.ui.theme.*

@Composable
fun ManagePlaylistTab(
    childName: String,
    playlist: PlaylistData?,
    isLoading: Boolean,
    onDeleteVideo: (String) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandPrimary)
        }
        return
    }

    val items = playlist?.items ?: emptyList()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "$childName's Assigned Videos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    val totalMins = (playlist?.totalDurationSeconds ?: 0) / 60
                    Text(
                        text = "${items.size} videos • ${totalMins}m total watch time",
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }

                Button(
                    onClick = onNavigateToSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Video", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (items.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BackgroundCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Rounded.VideoLibrary,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "No Videos Assigned Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Search YouTube to curate safe, wholesome videos for $childName.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToSearch,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        ) {
                            Text("Find Wholesome Videos")
                        }
                    }
                }
            }
        } else {
            itemsIndexed(items, key = { _, video -> video.itemId }) { index, video ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BackgroundCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    if (isLandscape) {
                        // Landscape: compact horizontal card
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.width(130.dp).height(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.08f)),
                            ) {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current).data(video.thumbnailUrl).crossfade(true).build(),
                                    contentDescription = video.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                Surface(shape = RoundedCornerShape(6.dp), color = BrandPrimary, modifier = Modifier.align(Alignment.TopStart).padding(5.dp)) {
                                    Text(text = "#${index + 1}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = video.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(3.dp))
                                Text(text = video.channelName, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Spacer(Modifier.width(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(onClick = { onMoveUp(index) }, enabled = index > 0, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move Up", tint = if (index > 0) TextPrimary else TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onMoveDown(index) }, enabled = index < items.size - 1, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move Down", tint = if (index < items.size - 1) TextPrimary else TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDeleteVideo(video.itemId) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = BrandCoral, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
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
                                color = BrandPrimary,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }

                            val mins = (video.durationSeconds ?: 0) / 60
                            val secs = (video.durationSeconds ?: 0) % 60
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
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
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                IconButton(
                                    onClick = { onMoveUp(index) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.ArrowUpward,
                                        contentDescription = "Move Up",
                                        tint = if (index > 0) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                IconButton(
                                    onClick = { onMoveDown(index) },
                                    enabled = index < items.size - 1,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.ArrowDownward,
                                        contentDescription = "Move Down",
                                        tint = if (index < items.size - 1) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteVideo(video.itemId) },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete Video",
                                        tint = BrandCoral,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                    } // end portrait else
                }
            }
        }
    }
}
