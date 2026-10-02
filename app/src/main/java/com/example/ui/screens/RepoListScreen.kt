package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.ForkRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.GitHubRepo
import com.example.data.model.GitHubUser
import com.example.ui.components.CreateRepoDialog
import com.example.ui.theme.CleanBluePrimary
import com.example.ui.theme.CleanPillBackground
import com.example.ui.theme.CleanPillText
import com.example.ui.viewmodel.RepoFilterOption
import com.example.ui.viewmodel.RepoSortOption
import java.util.Locale

import androidx.compose.material.icons.filled.SwapHoriz
import com.example.data.model.SavedAccount
import com.example.ui.components.AccountSwitcherBottomSheet
import com.example.ui.components.AddAccountDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoListScreen(
    user: GitHubUser?,
    savedAccounts: List<SavedAccount> = emptyList(),
    activeAccountId: Long? = null,
    repositories: List<GitHubRepo>,
    isLoading: Boolean,
    errorMessage: String?,
    searchQuery: String,
    currentSort: RepoSortOption,
    currentFilter: RepoFilterOption,
    pinnedRepos: Set<String>,
    onSearchQueryChange: (String) -> Unit,
    onSortChange: (RepoSortOption) -> Unit,
    onFilterChange: (RepoFilterOption) -> Unit,
    onTogglePin: (String) -> Unit,
    onSwitchAccount: (Long) -> Unit = {},
    onAddNewAccount: (String) -> Unit = {},
    onRemoveAccount: (Long) -> Unit = {},
    onOpenRepo: (GitHubRepo) -> Unit,
    onCreateRepo: (name: String, description: String?, isPrivate: Boolean) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateRepoDialog by remember { mutableStateOf(false) }
    var showAccountSwitcherSheet by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var isSortMenuOpen by remember { mutableStateOf(false) }

    // Filter and Sort calculation
    val filteredRepos = remember(repositories, searchQuery, currentFilter, currentSort, pinnedRepos) {
        repositories
            .filter { repo ->
                val matchesSearch = searchQuery.isBlank() ||
                        repo.name.contains(searchQuery, ignoreCase = true) ||
                        (repo.description?.contains(searchQuery, ignoreCase = true) == true)

                val matchesFilter = when (currentFilter) {
                    RepoFilterOption.ALL -> true
                    RepoFilterOption.PUBLIC -> !repo.private
                    RepoFilterOption.PRIVATE -> repo.private
                    RepoFilterOption.FORKS -> repo.fork
                }
                matchesSearch && matchesFilter
            }
            .sortedWith { r1, r2 ->
                val isPinned1 = pinnedRepos.contains(r1.fullName)
                val isPinned2 = pinnedRepos.contains(r2.fullName)
                if (isPinned1 != isPinned2) {
                    return@sortedWith if (isPinned1) -1 else 1
                }
                when (currentSort) {
                    RepoSortOption.UPDATED -> (r2.updatedAt ?: "").compareTo(r1.updatedAt ?: "")
                    RepoSortOption.STARS -> r2.stargazersCount.compareTo(r1.stargazersCount)
                    RepoSortOption.NAME -> r1.name.compareTo(r2.name, ignoreCase = true)
                    RepoSortOption.SIZE -> r2.size.compareTo(r1.size)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showAccountSwitcherSheet = true }
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .testTag("account_profile_button")
                    ) {
                        if (user?.avatarUrl?.isNotBlank() == true) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CleanBluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (user?.login?.take(1) ?: "G").uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = user?.name ?: user?.login ?: "Repositories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@${user?.login ?: "github"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (savedAccounts.size > 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CleanBluePrimary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "${savedAccounts.size} accounts",
                                            color = CleanBluePrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAccountSwitcherSheet = true },
                        modifier = Modifier.testTag("switch_accounts_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Account",
                            tint = CleanBluePrimary
                        )
                    }
                    IconButton(onClick = onRefresh, modifier = Modifier.testTag("refresh_repos_button")) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("settings_button")) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateRepoDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                modifier = Modifier.testTag("create_new_repo_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Repository")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search and Controls Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search repositories...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repo_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter chips and sort
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(RepoFilterOption.entries) { filter ->
                            val isSelected = currentFilter == filter
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) CleanBluePrimary else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onFilterChange(filter) }
                            ) {
                                Text(
                                    text = filter.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { isSortMenuOpen = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Sort repositories",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentSort.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isSortMenuOpen,
                            onDismissRequest = { isSortMenuOpen = false }
                        ) {
                            RepoSortOption.entries.forEach { sort ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Sort by: " + sort.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() },
                                            fontWeight = if (currentSort == sort) FontWeight.Bold else FontWeight.Normal,
                                            color = if (currentSort == sort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        isSortMenuOpen = false
                                        onSortChange(sort)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Repositories List
            if (isLoading && repositories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CleanBluePrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading your repositories...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (filteredRepos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = null,
                            tint = CleanBluePrimary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No repositories match '$searchQuery'" else "No repositories found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button below to create a new repository or refresh.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRepos, key = { it.id }) { repo ->
                        RepoCardItem(
                            repo = repo,
                            isPinned = pinnedRepos.contains(repo.fullName),
                            onTogglePin = { onTogglePin(repo.fullName) },
                            onClick = { onOpenRepo(repo) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateRepoDialog) {
        CreateRepoDialog(
            onDismiss = { showCreateRepoDialog = false },
            onConfirm = { name, desc, isPrivate ->
                showCreateRepoDialog = false
                onCreateRepo(name, desc, isPrivate)
            }
        )
    }

    if (showAccountSwitcherSheet) {
        AccountSwitcherBottomSheet(
            accounts = savedAccounts,
            activeAccountId = activeAccountId,
            onDismiss = { showAccountSwitcherSheet = false },
            onSelectAccount = { id ->
                showAccountSwitcherSheet = false
                onSwitchAccount(id)
            },
            onAddNewAccount = {
                showAccountSwitcherSheet = false
                showAddAccountDialog = true
            },
            onRemoveAccount = { id ->
                onRemoveAccount(id)
            }
        )
    }

    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { token ->
                showAddAccountDialog = false
                onAddNewAccount(token)
            }
        )
    }
}

@Composable
fun RepoCardItem(
    repo: GitHubRepo,
    isPinned: Boolean,
    onTogglePin: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("repo_card_${repo.name}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = repo.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (repo.private) CleanPillBackground else Color(0xFFE0F2FE)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (repo.private) Icons.Default.Lock else Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = if (repo.private) CleanPillText else CleanBluePrimary
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (repo.private) "Private" else "Public",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (repo.private) CleanPillText else CleanBluePrimary
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin repository",
                        tint = if (isPinned) CleanBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!repo.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!repo.language.isNullOrBlank()) {
                        val langColor = getLanguageColor(repo.language)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(langColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = repo.language,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    if (repo.stargazersCount > 0) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Stars",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = repo.stargazersCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    if (repo.forksCount > 0) {
                        Icon(
                            imageVector = Icons.Default.ForkRight,
                            contentDescription = "Forks",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = repo.forksCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Branch: ${repo.defaultBranch.ifEmpty { "main" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun getLanguageColor(language: String): Color {
    return when (language.lowercase(Locale.ROOT)) {
        "kotlin" -> Color(0xFFA97BFF)
        "java" -> Color(0xFFB07219)
        "javascript" -> Color(0xFFF1E05A)
        "typescript" -> Color(0xFF3178C6)
        "python" -> Color(0xFF3572A5)
        "html" -> Color(0xFFE34C26)
        "css" -> Color(0xFF563D7C)
        "c++" -> Color(0xFFF34B7D)
        "c#" -> Color(0xFF178600)
        "go" -> Color(0xFF00ADD8)
        "rust" -> Color(0xFFDEA584)
        "ruby" -> Color(0xFF701516)
        "swift" -> Color(0xFFF05138)
        "dart" -> Color(0xFF00B4AB)
        "php" -> Color(0xFF4F5D95)
        else -> Color(0xFF8B949E)
    }
}
