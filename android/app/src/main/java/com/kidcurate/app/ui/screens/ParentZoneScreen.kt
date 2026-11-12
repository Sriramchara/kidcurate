package com.kidcurate.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.kidcurate.app.data.*
import com.kidcurate.app.ui.components.GlassBackgroundAura
import com.kidcurate.app.ui.components.SoundHaptics
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.ui.components.ThematicPill
import com.kidcurate.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ParentZoneScreen(
    profiles: List<ChildProfileItem>,
    onThemeChanged: (String) -> Unit = {},
    onBackToChildMode: () -> Unit,
) {
    BackHandler(onBack = onBackToChildMode)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val parentPrefs = remember { ParentPreferences.get(context) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Videos", "Search", "Profiles", "Settings")

    // Managed Children state
    var managedChildren by remember { mutableStateOf<List<ChildManageItem>>(emptyList()) }
    var selectedChildId by remember { mutableStateOf(profiles.firstOrNull()?.id ?: "") }
    var selectedChildName by remember { mutableStateOf(profiles.firstOrNull()?.name ?: "Child") }

    // Child's Playlist state
    var currentPlaylist by remember { mutableStateOf<PlaylistData?>(null) }
    var isLoadingPlaylist by remember { mutableStateOf(false) }

    // Search state
    var searchQuery by remember { mutableStateOf("solar system science for kids") }
    var searchResults by remember { mutableStateOf<List<VideoSearchResultItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Dialog states
    var showEditChildDialog by remember { mutableStateOf<ChildManageItem?>(null) }
    var showAddChildDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ChildManageItem?>(null) }

    // Status feedback
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun refreshManagedChildren() {
        scope.launch {
            try {
                val list = ApiClient.api.getManageChildren()
                managedChildren = list
                if (selectedChildId.isEmpty() && list.isNotEmpty()) {
                    selectedChildId = list.first().id
                    selectedChildName = list.first().name
                }
            } catch (_: Exception) {}
        }
    }

    fun loadChildPlaylist(childId: String) {
        if (childId.isBlank()) return
        isLoadingPlaylist = true
        scope.launch {
            try {
                val resp = ApiClient.api.getTodaysPlaylist(childId)
                currentPlaylist = resp.playlist
            } catch (e: Exception) {
                statusMessage = "Could not load playlist: ${e.message}"
            } finally {
                isLoadingPlaylist = false
            }
        }
    }

    fun performSearch(query: String) {
        if (query.isBlank()) return
        isSearching = true
        scope.launch {
            try {
                searchResults = ApiClient.api.searchVideos(query.trim())
            } catch (e: Exception) {
                statusMessage = "Search error: ${e.message}"
            } finally {
                isSearching = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshManagedChildren()
        if (selectedChildId.isNotEmpty()) {
            loadChildPlaylist(selectedChildId)
        }
        if (searchResults.isEmpty()) {
            performSearch(searchQuery)
        }
    }

    LaunchedEffect(selectedChildId) {
        if (selectedChildId.isNotEmpty()) {
            loadChildPlaylist(selectedChildId)
        }
    }

    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length >= 3 && q != "solar system science for kids") {
            kotlinx.coroutines.delay(850)
            performSearch(q)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = BackgroundCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            IconButton(onClick = onBackToChildMode) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextPrimary,
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Parent Control Center",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                    )
                                }
                                Text(
                                    text = "Manage playlists, videos & profiles",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onBackToChildMode,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(
                                Icons.Rounded.ChildCare,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Kids Mode", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Navigation Tabs
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = BackgroundCard,
                        contentColor = BrandPrimary,
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                icon = {
                                    val icon = when (index) {
                                        0 -> Icons.Rounded.PlayCircle
                                        1 -> Icons.Rounded.Search
                                        2 -> Icons.Rounded.Face
                                        else -> Icons.Rounded.Settings
                                    }
                                    Icon(icon, contentDescription = title, modifier = Modifier.size(18.dp))
                                },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        },
        containerColor = BackgroundLight,
        modifier = Modifier.navigationBarsPadding(),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GlassBackgroundAura()
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
            // Status banner if present
            if (statusMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = statusMessage!!,
                            color = BrandPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { statusMessage = null },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Dismiss",
                                tint = BrandPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            // Child Selector Bar (for tabs 0 and 1: Videos and Search & Add)
            if (selectedTabIndex == 0 || selectedTabIndex == 1) {
                Surface(
                    color = SurfaceCard,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Child:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 10.dp),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(if (managedChildren.isNotEmpty()) managedChildren.map { ChildProfileItem(it.id, it.name, it.avatar) } else profiles) { child ->
                                val isSelected = selectedChildId == child.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else BackgroundCard,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else SurfaceCardBorder,
                                    ),
                                    modifier = Modifier.clickable {
                                        selectedChildId = child.id
                                        selectedChildName = child.name
                                    },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        ThematicAvatar(name = child.name, avatar = child.avatar, size = 22.dp, iconSize = 12.dp)
                                        Text(
                                            text = child.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) BrandPrimary else TextPrimary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> ManagePlaylistTab(
                    childName = selectedChildName,
                    playlist = currentPlaylist,
                    isLoading = isLoadingPlaylist,
                    onDeleteVideo = { itemId ->
                        scope.launch {
                            try {
                                ApiClient.api.removeVideoFromChildPlaylist(selectedChildId, itemId)
                                statusMessage = "Video removed from playlist."
                                loadChildPlaylist(selectedChildId)
                            } catch (e: Exception) {
                                statusMessage = "Error removing video: ${e.message}"
                            }
                        }
                    },
                    onMoveUp = { index ->
                        val items = currentPlaylist?.items?.toMutableList() ?: return@ManagePlaylistTab
                        if (index > 0) {
                            val temp = items[index]
                            items[index] = items[index - 1]
                            items[index - 1] = temp
                            val orderedIds = items.map { it.itemId }
                            scope.launch {
                                try {
                                    ApiClient.api.reorderChildPlaylist(selectedChildId, ReorderPlaylistRequest(orderedIds))
                                    loadChildPlaylist(selectedChildId)
                                } catch (e: Exception) {
                                    statusMessage = "Error reordering: ${e.message}"
                                }
                            }
                        }
                    },
                    onMoveDown = { index ->
                        val items = currentPlaylist?.items?.toMutableList() ?: return@ManagePlaylistTab
                        if (index < items.size - 1) {
                            val temp = items[index]
                            items[index] = items[index + 1]
                            items[index + 1] = temp
                            val orderedIds = items.map { it.itemId }
                            scope.launch {
                                try {
                                    ApiClient.api.reorderChildPlaylist(selectedChildId, ReorderPlaylistRequest(orderedIds))
                                    loadChildPlaylist(selectedChildId)
                                } catch (e: Exception) {
                                    statusMessage = "Error reordering: ${e.message}"
                                }
                            }
                        }
                    },
                    onNavigateToSearch = { selectedTabIndex = 1 },
                )

                1 -> SearchAndAddTab(
                    selectedChildName = selectedChildName,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onPerformSearch = { performSearch(searchQuery) },
                    searchResults = searchResults,
                    isSearching = isSearching,
                    onAddVideo = { video ->
                        scope.launch {
                            try {
                                ApiClient.api.addVideoToChildPlaylist(
                                    selectedChildId,
                                    AddVideoToChildRequest(
                                        youtubeId = video.youtubeId,
                                        title = video.title,
                                        channelName = video.channelName,
                                        thumbnailUrl = video.thumbnailUrl,
                                        durationSeconds = video.durationSeconds ?: 300,
                                    ),
                                )
                                statusMessage = "Added \"${video.title}\" to $selectedChildName's playlist!"
                                loadChildPlaylist(selectedChildId)
                            } catch (e: Exception) {
                                statusMessage = "Error adding video: ${e.message}"
                            }
                        }
                    },
                )

                2 -> ChildProfilesTab(
                    children = managedChildren,
                    onEditChild = { child -> showEditChildDialog = child },
                    onDeleteChild = { child -> showDeleteConfirmDialog = child },
                    onAddNewChild = { showAddChildDialog = true },
                )

                3 -> SecurityTab(
                    parentPrefs = parentPrefs,
                    onThemeChanged = onThemeChanged,
                )
            }
        }
    }
}

    // Edit Child Profile Dialog
    if (showEditChildDialog != null) {
        val child = showEditChildDialog!!
        var nameInput by remember { mutableStateOf(child.name) }
        var ageInput by remember { mutableStateOf(child.age.toString()) }
        var pinInput by remember { mutableStateOf(child.pin) }
        var avatarInput by remember { mutableStateOf(child.avatar) }
        val avatarChoices = listOf("compass", "sparkles", "rocket", "heart", "waves", "flame", "shield", "palette", "star")

        AlertDialog(
            onDismissRequest = { showEditChildDialog = null },
            title = { Text("Edit Child Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Child Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) ageInput = it },
                        label = { Text("Age (1-18)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                        label = { Text("Child 4-digit PIN") },
                        placeholder = { Text("••••") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Select Character Avatar:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        items(avatarChoices) { av ->
                            val isSelected = avatarInput == av
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        SoundHaptics.playPop(view, haptic)
                                        avatarInput = av
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(
                                            2.5.dp,
                                            if (isSelected) BrandPrimary else Color.Transparent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ThematicAvatar(
                                        name = av,
                                        avatar = av,
                                        size = 40.dp,
                                        iconSize = 20.dp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = av.replaceFirstChar { it.uppercase() },
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) BrandPrimary else TextSecondary,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedAge = ageInput.toIntOrNull() ?: child.age
                        scope.launch {
                            try {
                                ApiClient.api.updateChild(
                                    child.id,
                                    ChildUpdateRequest(
                                        name = nameInput.trim(),
                                        age = parsedAge,
                                        avatar = avatarInput,
                                        pin = pinInput.trim(),
                                    ),
                                )
                                statusMessage = "Updated profile for ${nameInput.trim()}!"
                                refreshManagedChildren()
                            } catch (e: Exception) {
                                statusMessage = "Error updating child: ${e.message}"
                            } finally {
                                showEditChildDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditChildDialog = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Add Child Profile Dialog
    if (showAddChildDialog) {
        var nameInput by remember { mutableStateOf("") }
        var ageInput by remember { mutableStateOf("7") }
        var pinInput by remember { mutableStateOf("1234") }
        var avatarInput by remember { mutableStateOf("compass") }
        val avatarChoices = listOf("compass", "sparkles", "rocket", "heart", "waves", "flame", "shield", "palette", "star")

        AlertDialog(
            onDismissRequest = { showAddChildDialog = false },
            title = { Text("Add New Child Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Child Name") },
                        placeholder = { Text("e.g. Maya") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) ageInput = it },
                        label = { Text("Age (1-18)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                        label = { Text("4-digit Login PIN") },
                        placeholder = { Text("••••") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Select Character Avatar:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        items(avatarChoices) { av ->
                            val isSelected = avatarInput == av
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        SoundHaptics.playPop(view, haptic)
                                        avatarInput = av
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(
                                            2.5.dp,
                                            if (isSelected) BrandPrimary else Color.Transparent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ThematicAvatar(
                                        name = av,
                                        avatar = av,
                                        size = 40.dp,
                                        iconSize = 20.dp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = av.replaceFirstChar { it.uppercase() },
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) BrandPrimary else TextSecondary,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedAge = ageInput.toIntOrNull() ?: 7
                        scope.launch {
                            try {
                                ApiClient.api.createChild(
                                    ChildCreateRequest(
                                        name = nameInput.trim(),
                                        age = parsedAge,
                                        avatar = avatarInput,
                                        pin = pinInput.trim(),
                                    ),
                                )
                                statusMessage = "Created profile for ${nameInput.trim()}!"
                                refreshManagedChildren()
                            } catch (e: Exception) {
                                statusMessage = "Error adding child: ${e.message}"
                            } finally {
                                showAddChildDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    enabled = nameInput.isNotBlank() && pinInput.length == 4,
                ) {
                    Text("Create Profile")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddChildDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Delete Child Profile Confirmation
    if (showDeleteConfirmDialog != null) {
        val child = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Child Profile", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete ${child.name}'s profile and all assigned playlists? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                ApiClient.api.deleteChild(child.id)
                                statusMessage = "${child.name}'s profile was deleted."
                                refreshManagedChildren()
                            } catch (e: Exception) {
                                statusMessage = "Error deleting child: ${e.message}"
                            } finally {
                                showDeleteConfirmDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

// ─── TAB 1: MANAGE PLAYLIST & ADDED VIDEOS ─────────────────────────────────

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
            itemsIndexed(items) { index, video ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BackgroundCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    shadowElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Position badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        // Thumbnail
                        AsyncImage(
                            model = video.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(width = 80.dp, height = 48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )

                        Spacer(Modifier.width(10.dp))

                        // Video details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = video.channelName,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                )
                                Text("•", fontSize = 10.sp, color = TextSecondary)
                                val mins = (video.durationSeconds ?: 0) / 60
                                val secs = (video.durationSeconds ?: 0) % 60
                                Text(
                                    text = String.format("%02d:%02d", mins, secs),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BrandPrimary,
                                )
                            }
                        }

                        // Reorder & Delete actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onMoveUp(index) },
                                enabled = index > 0,
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.ArrowUpward,
                                    contentDescription = "Move Up",
                                    tint = if (index > 0) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(16.dp),
                                )
                            }

                            IconButton(
                                onClick = { onMoveDown(index) },
                                enabled = index < items.size - 1,
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.ArrowDownward,
                                    contentDescription = "Move Down",
                                    tint = if (index < items.size - 1) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(16.dp),
                                )
                            }

                            IconButton(
                                onClick = { onDeleteVideo(video.itemId) },
                                modifier = Modifier.size(30.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete Video",
                                    tint = BrandCoral,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── TAB 2: SEARCH & ADD VIDEOS ───────────────────────────────────────────

@Composable
fun SearchAndAddTab(
    selectedChildName: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onPerformSearch: () -> Unit,
    searchResults: List<VideoSearchResultItem>,
    isSearching: Boolean,
    onAddVideo: (VideoSearchResultItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                        placeholder = { Text("Search YouTube for educational videos...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onPerformSearch() }),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    if (searchQuery.isNotEmpty()) {
                        Button(
                            onClick = onPerformSearch,
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

            Spacer(Modifier.height(10.dp))

            // Quick Topic Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val topics = listOf("Heidi Cartoons", "Moral Stories", "Space & Planets", "Math Fun", "Phonics ABC", "SciShow Kids")
                items(topics) { topic ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceCard,
                        modifier = Modifier.clickable {
                            onSearchQueryChange(topic)
                            onPerformSearch()
                        },
                    ) {
                        Text(
                            text = topic,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
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
                            text = if (searchQuery.isNotBlank()) "No videos found for \"$searchQuery\"" else "Search Safe YouTube Videos",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tip: You can search topics like 'Heidi cartoon', 'Phonics', 'Solar system' or paste ANY direct YouTube video link to add it instantly!",
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(searchResults) { video ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BackgroundCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    shadowElevation = 1.dp,
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AsyncImage(
                                model = video.thumbnailUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(width = 110.dp, height = 66.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = video.channelName,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ThematicPill(
                                        icon = Icons.Rounded.VerifiedUser,
                                        label = "${video.overallScore ?: 95}% Safe",
                                        tint = BrandSuccess,
                                        background = BrandSuccessContainer,
                                    )
                                    val mins = (video.durationSeconds ?: 0) / 60
                                    Text(
                                        text = "${mins}m",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = { onAddVideo(video) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add to $selectedChildName's Playlist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ─── TAB 3: CHILD PROFILES MANAGEMENT ─────────────────────────────────────

@Composable
fun ChildProfilesTab(
    children: List<ChildManageItem>,
    onEditChild: (ChildManageItem) -> Unit,
    onDeleteChild: (ChildManageItem) -> Unit,
    onAddNewChild: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Child Profiles",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Edit names, ages, avatars & login PINs",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                }

                Button(
                    onClick = onAddNewChild,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Child", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(children) { child ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BackgroundCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        ThematicAvatar(name = child.name, avatar = child.avatar, size = 48.dp, iconSize = 24.dp)
                        Column {
                            Text(
                                text = child.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Age: ${child.age} yrs",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                )
                                Text("•", fontSize = 12.sp, color = TextSecondary)
                                var showPin by remember { mutableStateOf(false) }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { showPin = !showPin }
                                ) {
                                    Text(
                                        text = if (showPin) "PIN: ${child.pin}" else "PIN: ••••",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary,
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        if (showPin) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (showPin) "Hide PIN" else "Show PIN",
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onEditChild(child) },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit",
                                tint = BrandPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Edit", fontSize = 12.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { onDeleteChild(child) },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete",
                                tint = BrandCoral,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── TAB 4: SECURITY & PREFERENCES ───────────────────────────────────────

@Composable
fun SecurityTab(
    parentPrefs: ParentPreferences,
    onPinUpdated: () -> Unit = {},
    onThemeChanged: (String) -> Unit = {},
) {
    var selectedTheme by remember { mutableStateOf(parentPrefs.appTheme) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Parent Guide & FAQs Card
        var showGuideDetails by remember { mutableStateOf(false) }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGuideDetails = !showGuideDetails },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Rounded.HelpOutline, contentDescription = null, tint = BrandPrimary)
                        Text("Parent Guide: How It Works", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        if (showGuideDetails) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary,
                    )
                }

                if (showGuideDetails) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = SurfaceCardBorder)
                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("1.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Child Profiles: Go to 'Profiles' tab to add or edit children, change their character avatar, and view or change their 4-digit PIN.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("2.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Add YouTube Videos: Go to 'Search' tab to find safe educational videos or paste ANY YouTube link to add it directly.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("3.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Manage Playlist: Go to 'Videos' tab to reorder videos with Up/Down buttons or delete unwanted videos.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("4.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Parent Master PIN: Default is '1234'. Use it anytime for quick offline Parent Zone access or to bypass a forgotten child PIN.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // App Theme Selector Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = BrandPrimary)
                    Text("App Theme", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Choose Light Theme, Dark Theme, or follow your device System setting.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("light" to "Light", "dark" to "Dark", "system" to "System").forEach { (mode, label) ->
                        val isSelected = selectedTheme == mode
                        OutlinedButton(
                            onClick = {
                                selectedTheme = mode
                                parentPrefs.appTheme = mode
                                onThemeChanged(mode)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else SurfaceCardBorder,
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary,
                            )
                        }
                    }
                }
            }
        }

        // Parent Cloud Account Card
        val scope = rememberCoroutineScope()
        var isCreateAccountMode by remember { mutableStateOf(false) }
        var parentNameInput by remember { mutableStateOf("") }
        var parentEmailInput by remember { mutableStateOf(parentPrefs.parentEmail) }
        var parentPasswordInput by remember { mutableStateOf("") }
        var isCloudPasswordVisible by remember { mutableStateOf(false) }
        var accountStatusMessage by remember { mutableStateOf<String?>(null) }
        var isAccountLoading by remember { mutableStateOf(false) }
        var currentToken by remember { mutableStateOf(parentPrefs.parentAuthToken) }

        var showForgotPasswordDialog by remember { mutableStateOf(false) }
        var forgotEmailInput by remember { mutableStateOf(parentPrefs.parentEmail) }
        var forgotCodeInput by remember { mutableStateOf("") }
        var forgotNewPasswordInput by remember { mutableStateOf("") }
        var forgotStage by remember { mutableStateOf(1) } // 1 = request, 2 = reset
        var forgotStatusMessage by remember { mutableStateOf<String?>(null) }
        var forgotErrorMessage by remember { mutableStateOf<String?>(null) }
        var isForgotLoading by remember { mutableStateOf(false) }

        var showDeleteAccountDialog by remember { mutableStateOf(false) }
        var isDeletingAccount by remember { mutableStateOf(false) }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.CloudSync, contentDescription = null, tint = BrandPrimary)
                    Text("Parent Cloud Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Sign in to synchronize child profiles, playlists, and analytics with the cloud server.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(14.dp))

                if (currentToken.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Signed in as:",
                                        fontSize = 12.sp,
                                        color = BrandPrimary,
                                    )
                                    Text(
                                        text = parentPrefs.parentEmail.ifBlank { "Parent Account" },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary,
                                    )
                                }
                                Button(
                                    onClick = {
                                        parentPrefs.parentAuthToken = ""
                                        parentPrefs.parentEmail = ""
                                        currentToken = ""
                                        com.kidcurate.app.data.ApiClient.authToken = ""
                                        accountStatusMessage = "Signed out of cloud account."
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text("Sign Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = BrandPrimary.copy(alpha = 0.2f))
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Delete account & cloud data",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(
                                    onClick = { showDeleteAccountDialog = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = BrandCoral),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text("Delete Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Mode toggle tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(3.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isCreateAccountMode) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    isCreateAccountMode = false
                                    accountStatusMessage = null
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Sign In",
                                fontSize = 12.sp,
                                fontWeight = if (!isCreateAccountMode) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isCreateAccountMode) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCreateAccountMode) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    isCreateAccountMode = true
                                    accountStatusMessage = null
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Create Account",
                                fontSize = 12.sp,
                                fontWeight = if (isCreateAccountMode) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCreateAccountMode) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (isCreateAccountMode) {
                        OutlinedTextField(
                            value = parentNameInput,
                            onValueChange = { parentNameInput = it },
                            label = { Text("Parent Name") },
                            placeholder = { Text("e.g. Sriram") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = parentEmailInput,
                        onValueChange = { parentEmailInput = it },
                        label = { Text("Parent Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = parentPasswordInput,
                        onValueChange = { parentPasswordInput = it },
                        label = { Text(if (isCreateAccountMode) "Create Password" else "Password") },
                        placeholder = { Text(if (isCreateAccountMode) "At least 6 characters" else "••••••••") },
                        singleLine = true,
                        visualTransformation = if (isCloudPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isCloudPasswordVisible = !isCloudPasswordVisible }) {
                                Icon(
                                    if (isCloudPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (isCloudPasswordVisible) "Hide Password" else "Show Password",
                                    tint = TextSecondary,
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )

                    if (!isCreateAccountMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(
                                onClick = {
                                    forgotEmailInput = parentEmailInput.trim()
                                    forgotStage = 1
                                    forgotCodeInput = ""
                                    forgotNewPasswordInput = ""
                                    forgotStatusMessage = null
                                    forgotErrorMessage = null
                                    showForgotPasswordDialog = true
                                }
                            ) {
                                Text(
                                    "Forgot Password?",
                                    fontSize = 12.sp,
                                    color = BrandPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(if (!isCreateAccountMode) 4.dp else 12.dp))

                    Button(
                        onClick = {
                            if (isCreateAccountMode && parentNameInput.isBlank()) {
                                accountStatusMessage = "Please enter your name."
                                return@Button
                            }
                            if (isCreateAccountMode && parentPasswordInput.length < 6) {
                                accountStatusMessage = "Password must be at least 6 characters."
                                return@Button
                            }
                            isAccountLoading = true
                            accountStatusMessage = null
                            scope.launch {
                                try {
                                    val resp = if (isCreateAccountMode) {
                                        com.kidcurate.app.data.ApiClient.api.registerParent(
                                            mapOf(
                                                "name" to parentNameInput.trim(),
                                                "email" to parentEmailInput.trim(),
                                                "password" to parentPasswordInput.trim()
                                            )
                                        )
                                    } else {
                                        com.kidcurate.app.data.ApiClient.api.loginParent(
                                            mapOf(
                                                "email" to parentEmailInput.trim(),
                                                "password" to parentPasswordInput.trim()
                                            )
                                        )
                                    }
                                    val token = resp["access_token"] as? String ?: ""
                                    if (token.isNotBlank()) {
                                        parentPrefs.parentAuthToken = token
                                        parentPrefs.parentEmail = parentEmailInput.trim()
                                        currentToken = token
                                        com.kidcurate.app.data.ApiClient.authToken = token
                                        accountStatusMessage = if (isCreateAccountMode) "Account created successfully!" else "Successfully connected to cloud!"
                                    } else {
                                        accountStatusMessage = "Response missing authorization token."
                                    }
                                } catch (e: Exception) {
                                    accountStatusMessage = "Operation failed: ${e.message}"
                                } finally {
                                    isAccountLoading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAccountLoading && parentEmailInput.isNotBlank() && parentPasswordInput.isNotBlank() && (!isCreateAccountMode || parentNameInput.isNotBlank()),
                    ) {
                        if (isAccountLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        } else {
                            Text(if (isCreateAccountMode) "Create Parent Account" else "Sign In to Cloud Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (accountStatusMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        accountStatusMessage!!,
                        color = if (currentToken.isNotBlank()) BrandSuccess else BrandCoral,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Forgot / Reset Password Dialog
        if (showForgotPasswordDialog) {
            Dialog(onDismissRequest = { showForgotPasswordDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Rounded.Key, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (forgotStage == 1) "Forgot Password" else "Enter Verification Code",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (forgotStage == 1)
                                "Enter your parent email to generate a password reset code."
                            else
                                "Enter the 6-digit code and choose a new password.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(14.dp))

                        if (forgotStage == 1) {
                            OutlinedTextField(
                                value = forgotEmailInput,
                                onValueChange = { forgotEmailInput = it },
                                label = { Text("Parent Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            OutlinedTextField(
                                value = forgotCodeInput,
                                onValueChange = { if (it.length <= 6) forgotCodeInput = it },
                                label = { Text("6-Digit Code") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = forgotNewPasswordInput,
                                onValueChange = { forgotNewPasswordInput = it },
                                label = { Text("New Password (min 6 chars)") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        if (forgotErrorMessage != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(forgotErrorMessage!!, color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (forgotStatusMessage != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(forgotStatusMessage!!, color = BrandSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = { showForgotPasswordDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    if (forgotStage == 1) {
                                        if (forgotEmailInput.isBlank()) {
                                            forgotErrorMessage = "Please enter your email."
                                            return@Button
                                        }
                                        isForgotLoading = true
                                        forgotErrorMessage = null
                                        forgotStatusMessage = null
                                        scope.launch {
                                            try {
                                                val res = ApiClient.api.forgotPassword(mapOf("email" to forgotEmailInput.trim()))
                                                val code = res["reset_code"] as? String ?: ""
                                                if (code.isNotBlank()) {
                                                    forgotCodeInput = code
                                                }
                                                forgotStatusMessage = "Code generated! Enter code and new password."
                                                forgotStage = 2
                                            } catch (e: Exception) {
                                                forgotErrorMessage = "Account not found or error connecting."
                                            } finally {
                                                isForgotLoading = false
                                            }
                                        }
                                    } else {
                                        if (forgotCodeInput.length != 6) {
                                            forgotErrorMessage = "Code must be 6 digits."
                                            return@Button
                                        }
                                        if (forgotNewPasswordInput.length < 6) {
                                            forgotErrorMessage = "Password must be at least 6 characters."
                                            return@Button
                                        }
                                        isForgotLoading = true
                                        forgotErrorMessage = null
                                        scope.launch {
                                            try {
                                                ApiClient.api.resetPassword(
                                                    mapOf(
                                                        "email" to forgotEmailInput.trim(),
                                                        "code" to forgotCodeInput.trim(),
                                                        "new_password" to forgotNewPasswordInput.trim()
                                                    )
                                                )
                                                parentPasswordInput = forgotNewPasswordInput
                                                accountStatusMessage = "Password reset! You can now log in."
                                                showForgotPasswordDialog = false
                                            } catch (e: Exception) {
                                                forgotErrorMessage = "Invalid code or failed to reset."
                                            } finally {
                                                isForgotLoading = false
                                            }
                                        }
                                    }
                                },
                                enabled = !isForgotLoading,
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            ) {
                                Text(if (isForgotLoading) "Please wait..." else if (forgotStage == 1) "Get Code" else "Reset Password")
                            }
                        }
                    }
                }
            }
        }

        // Delete Parent Account Confirmation Dialog
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { if (!isDeletingAccount) showDeleteAccountDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = BrandCoral)
                        Text("Delete Account & All Data?", fontWeight = FontWeight.Bold, color = BrandCoral)
                    }
                },
                text = {
                    Text(
                        "Are you sure you want to permanently delete your parent account? This will erase all child profiles, playlists, watch history, and cloud data. This action cannot be undone.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                isDeletingAccount = true
                                try {
                                    com.kidcurate.app.data.ApiClient.api.deleteAccount()
                                    parentPrefs.parentAuthToken = ""
                                    parentPrefs.parentEmail = ""
                                    currentToken = ""
                                    com.kidcurate.app.data.ApiClient.authToken = ""
                                    accountStatusMessage = "Account and all data deleted permanently."
                                    showDeleteAccountDialog = false
                                } catch (e: Exception) {
                                    accountStatusMessage = "Failed to delete account: ${e.message}"
                                    showDeleteAccountDialog = false
                                } finally {
                                    isDeletingAccount = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isDeletingAccount,
                    ) {
                        Text(if (isDeletingAccount) "Deleting..." else "Yes, Delete Everything", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteAccountDialog = false },
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isDeletingAccount,
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Parent Security & PIN Control Card
        var showChangeParentPinDialog by remember { mutableStateOf(false) }
        var newPinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var pinErrorMessage by remember { mutableStateOf<String?>(null) }
        var pinSuccessMessage by remember { mutableStateOf<String?>(null) }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = BrandPrimary)
                    Text("Security & PIN Control", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "KidCurate provides dual-layer security for parent controls and individual child profiles:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(12.dp))

                // Child Profile PIN
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Rounded.Face, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Child Profile PINs", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "To change a child's PIN, go to the 'Profiles' tab and tap 'Edit' on the child.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Parent Master PIN
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Parent Master PIN", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Used for quick offline Parent Zone access: ••••",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                newPinInput = ""
                                confirmPinInput = ""
                                pinErrorMessage = null
                                pinSuccessMessage = null
                                showChangeParentPinDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text("Change PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (pinSuccessMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(pinSuccessMessage!!, color = BrandSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Change Parent PIN Dialog
        if (showChangeParentPinDialog) {
            Dialog(onDismissRequest = { showChangeParentPinDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Rounded.LockReset, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Change Parent Master PIN",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Set a 4-digit PIN for quick Parent Zone entry.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(14.dp))

                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) newPinInput = it },
                            label = { Text("New 4-digit PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) confirmPinInput = it },
                            label = { Text("Confirm New PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (pinErrorMessage != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(pinErrorMessage!!, color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showChangeParentPinDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    if (newPinInput.length != 4) {
                                        pinErrorMessage = "PIN must be exactly 4 digits."
                                        return@Button
                                    }
                                    if (newPinInput != confirmPinInput) {
                                        pinErrorMessage = "PINs do not match."
                                        return@Button
                                    }
                                    parentPrefs.parentPin = newPinInput
                                    pinSuccessMessage = "Parent Master PIN changed successfully!"
                                    showChangeParentPinDialog = false
                                    onPinUpdated()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            ) {
                                Text("Save PIN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
