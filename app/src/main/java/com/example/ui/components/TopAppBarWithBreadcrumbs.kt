package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GitHubBranch
import com.example.ui.theme.CleanBluePrimary
import com.example.ui.theme.CleanPillBackground
import com.example.ui.theme.CleanPillText
import com.example.ui.theme.CleanRedAccent
import com.example.ui.viewmodel.Breadcrumb
import com.example.ui.viewmodel.ExplorerViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithBreadcrumbs(
    repoName: String,
    breadcrumbs: List<Breadcrumb>,
    activeBranch: String,
    availableBranches: List<GitHubBranch>,
    viewMode: ExplorerViewMode,
    isSearchOpen: Boolean,
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    onNavigateBack: () -> Unit,
    onBreadcrumbClick: (String) -> Unit,
    onBranchSelected: (String) -> Unit,
    onToggleViewMode: () -> Unit,
    onToggleSearch: () -> Unit,
    onToggleSelectionMode: () -> Unit = {},
    onExitSelectionMode: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var branchMenuExpanded by remember { mutableStateOf(false) }
    val breadcrumbScrollState = rememberScrollState()

    // Auto-scroll breadcrumbs to the far right on navigation
    LaunchedEffect(breadcrumbs.size) {
        breadcrumbScrollState.animateScrollTo(breadcrumbScrollState.maxValue)
    }

    Surface(
        color = if (isSelectionMode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            if (isSelectionMode) {
                // Multi-Selection Active Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onExitSelectionMode) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close selection")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$selectedCount selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onSelectAll) {
                            Icon(imageVector = Icons.Default.SelectAll, contentDescription = "Select All")
                        }

                        IconButton(
                            onClick = onDeleteSelected,
                            enabled = selectedCount > 0,
                            modifier = Modifier.testTag("bulk_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = if (selectedCount > 0) CleanRedAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            } else {
                // Standard Navigation Top Bar Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Button & Repo Information
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("top_bar_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // GitHub Avatar Icon Circle
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CleanBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = repoName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Branch Selector Pill
                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CleanPillBackground)
                                        .clickable { branchMenuExpanded = true }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("branch_selector_pill")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AltRoute,
                                        contentDescription = null,
                                        modifier = Modifier.size(11.dp),
                                        tint = CleanPillText
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = activeBranch.ifBlank { "main" },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CleanPillText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = CleanPillText
                                    )
                                }

                                DropdownMenu(
                                    expanded = branchMenuExpanded,
                                    onDismissRequest = { branchMenuExpanded = false }
                                ) {
                                    availableBranches.forEach { branch ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (branch.name == activeBranch) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .clip(CircleShape)
                                                                .background(MaterialTheme.colorScheme.primary)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    Text(
                                                        text = branch.name,
                                                        fontWeight = if (branch.name == activeBranch) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            },
                                            onClick = {
                                                branchMenuExpanded = false
                                                onBranchSelected(branch.name)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Actions: Search, View Mode, and Selection Mode Trigger
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleSearch,
                            modifier = Modifier.testTag("search_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isSearchOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onToggleSelectionMode,
                            modifier = Modifier.testTag("selection_mode_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Select items",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onToggleViewMode,
                            modifier = Modifier.testTag("view_mode_toggle_button")
                        ) {
                            val icon = when (viewMode) {
                                ExplorerViewMode.LIST -> Icons.Default.GridView
                                ExplorerViewMode.GRID -> Icons.Default.AccountTree
                                ExplorerViewMode.TREE -> Icons.Default.ViewList
                            }
                            val desc = when (viewMode) {
                                ExplorerViewMode.LIST -> "Switch to Grid View"
                                ExplorerViewMode.GRID -> "Switch to Tree View"
                                ExplorerViewMode.TREE -> "Switch to List View"
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = desc,
                                tint = if (viewMode == ExplorerViewMode.TREE) CleanBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Minimalist Breadcrumbs Scrollable Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(breadcrumbScrollState)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Root Home Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBreadcrumbClick("") }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("breadcrumb_root")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Repository Root",
                        modifier = Modifier.size(16.dp),
                        tint = CleanBluePrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = repoName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CleanBluePrimary
                    )
                }

                // Nested directories in breadcrumb path
                breadcrumbs.forEachIndexed { index, crumb ->
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(horizontal = 1.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )

                    val isLast = index == breadcrumbs.lastIndex
                    if (isLast) {
                        // Highlighted current active folder pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CleanPillBackground)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = crumb.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CleanPillText,
                                maxLines = 1
                            )
                        }
                    } else {
                        Text(
                            text = crumb.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = CleanBluePrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onBreadcrumbClick(crumb.fullPath) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        }
    }
}
