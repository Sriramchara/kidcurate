package com.kidcurate.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kidcurate.app.data.*
import com.kidcurate.app.ui.screens.parent.*
import com.kidcurate.app.ui.components.GlassBackgroundAura
import com.kidcurate.app.ui.components.OnboardingTourDialog
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
    onAccountDeleted: () -> Unit = onBackToChildMode,
) {
    BackHandler(onBack = onBackToChildMode)

    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val parentPrefs = remember { ParentPreferences.get(appContext) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Videos", "Search", "History", "Profiles", "Settings")

    // Managed Children state
    var managedChildren by remember { mutableStateOf<List<ChildManageItem>>(emptyList()) }
    var selectedChildId by remember { mutableStateOf(profiles.firstOrNull()?.id ?: "") }
    var selectedChildName by remember { mutableStateOf(profiles.firstOrNull()?.name ?: "Child") }

    // Child's Playlist state
    var currentPlaylist by remember { mutableStateOf<PlaylistData?>(null) }
    var isLoadingPlaylist by remember { mutableStateOf(false) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<VideoSearchResultItem>>(emptyList()) }
    var searchSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
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
                if (list.isNotEmpty()) {
                    managedChildren = list
                    val cachedList = list.map { ChildProfileItem(id = it.id, name = it.name, avatar = it.avatar) }
                    parentPrefs.cachedProfilesJson = com.google.gson.Gson().toJson(cachedList)
                    if (selectedChildId.isEmpty()) {
                        selectedChildId = list.first().id
                        selectedChildName = list.first().name
                    }
                } else if (managedChildren.isEmpty() && parentPrefs.cachedProfilesJson.isNotBlank()) {
                    try {
                        val listType = object : com.google.gson.reflect.TypeToken<List<ChildProfileItem>>() {}.type
                        val cached: List<ChildProfileItem> = com.google.gson.Gson().fromJson(parentPrefs.cachedProfilesJson, listType)
                        if (cached.isNotEmpty()) {
                            managedChildren = cached.map {
                                ChildManageItem(
                                    id = it.id,
                                    name = it.name,
                                    age = 7,
                                    avatar = it.avatar,
                                    pin = "••••",
                                    parentId = null,
                                )
                            }
                            if (selectedChildId.isEmpty()) {
                                selectedChildId = cached.first().id
                                selectedChildName = cached.first().name
                            }
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {
                if (managedChildren.isEmpty() && parentPrefs.cachedProfilesJson.isNotBlank()) {
                    try {
                        val listType = object : com.google.gson.reflect.TypeToken<List<ChildProfileItem>>() {}.type
                        val cached: List<ChildProfileItem> = com.google.gson.Gson().fromJson(parentPrefs.cachedProfilesJson, listType)
                        if (cached.isNotEmpty()) {
                            managedChildren = cached.map {
                                ChildManageItem(
                                    id = it.id,
                                    name = it.name,
                                    age = 7,
                                    avatar = it.avatar,
                                    pin = "••••",
                                    parentId = null,
                                )
                            }
                            if (selectedChildId.isEmpty()) {
                                selectedChildId = cached.first().id
                                selectedChildName = cached.first().name
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
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
                val msg = e.message ?: ""
                if (!msg.contains("404")) {
                    statusMessage = "Could not load playlist: $msg"
                }
            } finally {
                isLoadingPlaylist = false
            }
        }
    }

    fun performSearch(query: String) {
        if (query.isBlank()) return
        isSearching = true
        searchSuggestions = emptyList()
        scope.launch {
            try {
                searchResults = ApiClient.api.searchVideos(query.trim(), maxResults = 30)
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
        if (searchQuery.isNotBlank() && searchResults.isEmpty()) {
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
        if (q.length >= 2) {
            try {
                searchSuggestions = ApiClient.api.getSearchSuggestions(q)
            } catch (_: Exception) {
                searchSuggestions = emptyList()
            }
        } else {
            searchSuggestions = emptyList()
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
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    text = "Manage playlists, videos & profiles",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
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
                                        2 -> Icons.Rounded.History
                                        3 -> Icons.Rounded.Face
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
                                    onClick = {
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

            val existingVideoIds: Set<String> = remember(currentPlaylist) {
                currentPlaylist?.items?.map { it.youtubeId }?.toSet() ?: emptySet()
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> ManagePlaylistTab(
                    childName = selectedChildName,
                    playlist = currentPlaylist,
                    isLoading = isLoadingPlaylist,
                    onDeleteVideo = { itemId ->
                        val current = currentPlaylist
                        if (current != null) {
                            val remaining = current.items.filter { it.itemId != itemId }
                            currentPlaylist = current.copy(items = remaining)
                        }
                        scope.launch {
                            try {
                                ApiClient.api.removeVideoFromChildPlaylist(selectedChildId, itemId)
                                statusMessage = "Video removed from playlist."
                            } catch (e: Exception) {
                                statusMessage = "Error removing video: ${e.message}"
                                loadChildPlaylist(selectedChildId)
                            }
                        }
                    },
                    onMoveUp = { index ->
                        val current = currentPlaylist ?: return@ManagePlaylistTab
                        val items = current.items.toMutableList()
                        if (index > 0) {
                            val temp = items[index]
                            items[index] = items[index - 1]
                            items[index - 1] = temp
                            currentPlaylist = current.copy(items = items)
                            val orderedIds = items.map { it.itemId }
                            scope.launch {
                                try {
                                    ApiClient.api.reorderChildPlaylist(selectedChildId, ReorderPlaylistRequest(orderedIds))
                                } catch (e: Exception) {
                                    statusMessage = "Error syncing playlist order: ${e.message}"
                                }
                            }
                        }
                    },
                    onMoveDown = { index ->
                        val current = currentPlaylist ?: return@ManagePlaylistTab
                        val items = current.items.toMutableList()
                        if (index < items.size - 1) {
                            val temp = items[index]
                            items[index] = items[index + 1]
                            items[index + 1] = temp
                            currentPlaylist = current.copy(items = items)
                            val orderedIds = items.map { it.itemId }
                            scope.launch {
                                try {
                                    ApiClient.api.reorderChildPlaylist(selectedChildId, ReorderPlaylistRequest(orderedIds))
                                } catch (e: Exception) {
                                    statusMessage = "Error syncing playlist order: ${e.message}"
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
                    searchSuggestions = searchSuggestions,
                    existingVideoIds = existingVideoIds,
                    onAddVideo = { video ->
                        scope.launch {
                            try {
                                val resp = ApiClient.api.addVideoToChildPlaylist(
                                    selectedChildId,
                                    AddVideoToChildRequest(
                                        youtubeId = video.youtubeId,
                                        title = video.title,
                                        channelName = video.channelName,
                                        thumbnailUrl = video.thumbnailUrl,
                                        durationSeconds = video.durationSeconds ?: 300,
                                    ),
                                )
                                if (resp["status"] == "already_added") {
                                    statusMessage = "\"${video.title}\" is already in $selectedChildName's playlist."
                                } else {
                                    statusMessage = "Added \"${video.title}\" to $selectedChildName's playlist!"
                                    loadChildPlaylist(selectedChildId)
                                }
                            } catch (e: Exception) {
                                statusMessage = "Error adding video: ${e.message}"
                            }
                        }
                    },
                )

                2 -> HistoryTabContent(
                    managedChildren = managedChildren,
                    onSelectSearchTerm = { term ->
                        searchQuery = term
                        selectedTabIndex = 1
                        scope.launch {
                            isSearching = true
                            try {
                                searchResults = ApiClient.api.searchVideos(term)
                            } catch (_: Exception) {
                                searchResults = emptyList()
                            } finally {
                                isSearching = false
                            }
                        }
                    },
                    onReAddVideo = { historyItem ->
                        scope.launch {
                            try {
                                val targetChild = if (historyItem.childId.isNotBlank()) historyItem.childId else selectedChildId
                                ApiClient.api.addVideoToChildPlaylist(
                                    targetChild,
                                    AddVideoToChildRequest(
                                        youtubeId = historyItem.youtubeId,
                                        title = historyItem.title,
                                        channelName = historyItem.channelName,
                                        thumbnailUrl = historyItem.thumbnailUrl,
                                        durationSeconds = historyItem.durationSeconds,
                                    )
                                )
                                statusMessage = "Re-added \"${historyItem.title}\" to playlist!"
                                loadChildPlaylist(selectedChildId)
                            } catch (e: Exception) {
                                statusMessage = "Could not re-add video: ${e.message}"
                            }
                        }
                    }
                )

                3 -> ChildProfilesTab(
                    children = managedChildren,
                    onEditChild = { child -> showEditChildDialog = child },
                    onDeleteChild = { child -> showDeleteConfirmDialog = child },
                    onAddNewChild = { showAddChildDialog = true },
                )

                4 -> SecurityTab(
                    parentPrefs = parentPrefs,
                    onThemeChanged = onThemeChanged,
                    onBackToChildMode = onBackToChildMode,
                    onAccountDeleted = onAccountDeleted,
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
        val rawPin = child.pin.orEmpty()
        val initialPin = if (rawPin.isNotBlank() && rawPin != "••••" && !rawPin.startsWith("$2") && rawPin.all { it.isDigit() }) rawPin else ""
        var pinInput by remember { mutableStateOf(initialPin) }
        var isPinVisible by remember { mutableStateOf(false) }
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
                        label = { Text("Child 4-digit PIN (Leave blank to keep current)") },
                        placeholder = { Text("••••") },
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    if (isPinVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (isPinVisible) "Hide PIN" else "Show PIN",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
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
                                if (ApiClient.authToken.isBlank()) {
                                    statusMessage = "Please sign in with Google under Settings to sync child profiles to the cloud."
                                    showEditChildDialog = null
                                    return@launch
                                }
                                val updatedPin = if (pinInput.isNotBlank() && pinInput.length == 4) pinInput.trim() else null
                                val updatedChild = ApiClient.api.updateChild(
                                    child.id,
                                    ChildUpdateRequest(
                                        name = nameInput.trim(),
                                        age = parsedAge,
                                        avatar = avatarInput,
                                        pin = updatedPin,
                                    ),
                                )
                                val updatedList = managedChildren.map { if (it.id == child.id) updatedChild else it }
                                managedChildren = updatedList
                                val cachedList = updatedList.map { ChildProfileItem(id = it.id, name = it.name, avatar = it.avatar) }
                                parentPrefs.cachedProfilesJson = com.google.gson.Gson().toJson(cachedList)
                                statusMessage = "Updated profile for ${nameInput.trim()}!"
                                refreshManagedChildren()
                            } catch (e: Exception) {
                                val msg = e.message ?: ""
                                statusMessage = if (msg.contains("401")) {
                                    "Cloud session expired. Please re-authenticate under Settings."
                                } else {
                                    "Error updating child: $msg"
                                }
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
        var ageInput by remember { mutableStateOf("") }
        var pinInput by remember { mutableStateOf("") }
        var isPinVisible by remember { mutableStateOf(true) }
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
                        placeholder = { Text("e.g. 7") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                        label = { Text("4-digit Login PIN") },
                        placeholder = { Text("Enter 4-digit PIN") },
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    if (isPinVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (isPinVisible) "Hide PIN" else "Show PIN",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
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
                                if (ApiClient.authToken.isBlank()) {
                                    statusMessage = "Please sign in with Google under Settings to sync child profiles to the cloud."
                                    showAddChildDialog = false
                                    return@launch
                                }
                                val created = ApiClient.api.createChild(
                                    ChildCreateRequest(
                                        name = nameInput.trim(),
                                        age = parsedAge,
                                        avatar = avatarInput,
                                        pin = pinInput.trim(),
                                    ),
                                )
                                val updated = managedChildren + created
                                managedChildren = updated
                                val cachedList = updated.map { ChildProfileItem(id = it.id, name = it.name, avatar = it.avatar) }
                                parentPrefs.cachedProfilesJson = com.google.gson.Gson().toJson(cachedList)
                                statusMessage = "Created profile for ${nameInput.trim()}!"
                                refreshManagedChildren()
                            } catch (e: Exception) {
                                val msg = e.message ?: ""
                                statusMessage = if (msg.contains("401")) {
                                    "Cloud session expired. Please re-authenticate under Settings."
                                } else {
                                    "Error adding child: $msg"
                                }
                            } finally {
                                showAddChildDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    enabled = nameInput.isNotBlank() && pinInput.length == 4 && ageInput.toIntOrNull() != null,
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
                                val updated = managedChildren.filter { it.id != child.id }
                                managedChildren = updated
                                val cachedList = updated.map { ChildProfileItem(id = it.id, name = it.name, avatar = it.avatar) }
                                parentPrefs.cachedProfilesJson = com.google.gson.Gson().toJson(cachedList)
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
