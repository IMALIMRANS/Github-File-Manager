package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileTreeNode
import com.example.data.model.GitHubContentItem
import com.example.ui.theme.CleanBluePrimary
import com.example.ui.theme.CleanBorderLight
import com.example.ui.theme.CleanFolderBg
import com.example.ui.theme.CleanFolderIcon
import com.example.ui.theme.CleanPillBackground
import com.example.ui.theme.CleanPillText

/**
 * Represents a flattened row item in the virtualized recursive tree.
 */
data class FlattenedTreeRow(
    val node: FileTreeNode,
    val depth: Int,
    val isExpanded: Boolean,
    val hasChildren: Boolean,
    val isCurrentActiveDir: Boolean,
    val isAncestorOfActiveDir: Boolean
)

@Composable
fun RecursiveFileTreeComponent(
    rootNodes: List<FileTreeNode>,
    expandedPaths: Set<String>,
    currentPath: String,
    isLoading: Boolean,
    errorMessage: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleFolder: (String) -> Unit,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit,
    onNavigateToDirectory: (String) -> Unit,
    onOpenFile: (FileTreeNode) -> Unit,
    onItemMenuClick: (GitHubContentItem) -> Unit,
    onRefreshTree: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanCurrentPath = remember(currentPath) {
        currentPath.trim().removePrefix("/").removeSuffix("/")
    }

    // Flatten tree nodes for high-performance virtualization
    val visibleRows = remember(rootNodes, expandedPaths, cleanCurrentPath, searchQuery) {
        flattenTree(
            nodes = rootNodes,
            expandedPaths = expandedPaths,
            currentPath = cleanCurrentPath,
            searchQuery = searchQuery.trim()
        )
    }

    val totalFiles = remember(rootNodes) {
        rootNodes.sumOf { it.countTotalFiles() }
    }
    val totalFolders = remember(rootNodes) {
        rootNodes.sumOf { it.countTotalFolders() }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tree Header & Controls Toolbar
            TreeControlHeader(
                totalFiles = totalFiles,
                totalFolders = totalFolders,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onExpandAll = onExpandAll,
                onCollapseAll = onCollapseAll,
                onRefreshTree = onRefreshTree
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // Content Area: Loading, Error, Empty, or Tree List
            when {
                isLoading && rootNodes.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = CleanBluePrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Loading recursive repository tree...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                errorMessage != null && rootNodes.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountTree,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Unable to load tree",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onRefreshTree,
                                modifier = Modifier.testTag("retry_tree_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry Loading Tree")
                            }
                        }
                    }
                }

                visibleRows.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AccountTree,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No files match '$searchQuery'" else "Empty Repository Tree",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Try a different search query." else "This repository currently has no files.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("recursive_file_tree_list"),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp, start = 8.dp, end = 8.dp)
                    ) {
                        items(
                            items = visibleRows,
                            key = { it.node.path.ifEmpty { "root_${it.node.name}" } }
                        ) { row ->
                            TreeRowItem(
                                row = row,
                                searchQuery = searchQuery,
                                onToggleFolder = { onToggleFolder(row.node.path) },
                                onNavigateToDirectory = { onNavigateToDirectory(row.node.path) },
                                onOpenFile = { onOpenFile(row.node) },
                                onItemMenuClick = { onItemMenuClick(row.node.toContentItem()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreeControlHeader(
    totalFiles: Int,
    totalFolders: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit,
    onRefreshTree: () -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Stats badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CleanPillBackground)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = CleanPillText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$totalFolders dirs • $totalFiles files",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleanPillText
                        )
                    }
                }
            }

            // Quick Toolbar Actions: Search toggle, Expand All, Collapse All, Refresh
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) onSearchQueryChange("")
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("tree_search_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Tree",
                        tint = if (isSearchExpanded || searchQuery.isNotEmpty()) CleanBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onExpandAll,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("tree_expand_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = "Expand All",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onCollapseAll,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("tree_collapse_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.UnfoldLess,
                        contentDescription = "Collapse All",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRefreshTree,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("tree_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Tree",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Animated Search Input
        AnimatedVisibility(visible = isSearchExpanded || searchQuery.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Filter tree files & folders...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = CleanBluePrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .testTag("tree_search_input")
            )
        }
    }
}

@Composable
private fun TreeRowItem(
    row: FlattenedTreeRow,
    searchQuery: String,
    onToggleFolder: () -> Unit,
    onNavigateToDirectory: () -> Unit,
    onOpenFile: () -> Unit,
    onItemMenuClick: () -> Unit
) {
    val node = row.node
    val isDir = node.isDirectory
    val depth = row.depth
    val guideColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val activeBorderColor = CleanBluePrimary.copy(alpha = 0.5f)

    val chevronRotation by animateFloatAsState(
        targetValue = if (row.isExpanded) 90f else 0f,
        label = "chevron_rotate"
    )

    // Indentation guideline drawing
    val indentWidth = 20.dp
    val startPadding = (depth * 20).dp

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = when {
            row.isCurrentActiveDir -> CleanBluePrimary.copy(alpha = 0.08f)
            else -> Color.Transparent
        },
        border = if (row.isCurrentActiveDir) {
            androidx.compose.foundation.BorderStroke(1.dp, activeBorderColor)
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                if (isDir) {
                    onToggleFolder()
                } else {
                    onOpenFile()
                }
            }
            .testTag("tree_node_${node.path}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Draw tree guidelines for nested depths
                    for (i in 0 until depth) {
                        val x = (i * 20.dp.toPx()) + 14.dp.toPx()
                        drawLine(
                            color = guideColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }
                }
                .padding(start = startPadding + 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand/Collapse Chevron for directories or spacer for files
            if (isDir) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleFolder)
                        .testTag("tree_chevron_${node.path}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = if (row.isExpanded) "Collapse" else "Expand",
                        tint = CleanBluePrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(chevronRotation)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(28.dp))
            }

            // Type Icon: Folder vs File visual
            if (isDir) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CleanFolderBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (row.isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = null,
                        tint = CleanFolderIcon,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                val visual = remember(node.name) {
                    FileIconHelper.getVisualForFile(node.toContentItem())
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(visual.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = null,
                        tint = visual.color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // File / Folder Name & Stats
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = node.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isDir || row.isCurrentActiveDir) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            row.isCurrentActiveDir -> CleanBluePrimary
                            isDir -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // If matches search query, add a match indicator
                    if (searchQuery.isNotEmpty() && node.name.contains(searchQuery, ignoreCase = true)) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CleanBluePrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "MATCH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CleanBluePrimary
                            )
                        }
                    }

                    // Active directory badge
                    if (row.isCurrentActiveDir && isDir) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CleanBluePrimary)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Subtitle: size for files or item count for folders
                if (isDir) {
                    val childCount = node.children.size
                    if (childCount > 0) {
                        Text(
                            text = "$childCount item${if (childCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                } else if (node.size > 0) {
                    Text(
                        text = FileIconHelper.formatFileSize(node.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // Trailing Actions:
            // For folders: Quick "Jump to directory" button
            // For files: Context menu button
            if (isDir) {
                IconButton(
                    onClick = onNavigateToDirectory,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("tree_nav_dir_${node.path}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Navigate into folder",
                        tint = CleanBluePrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onItemMenuClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("tree_file_menu_${node.path}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "File Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Recursively flattens the tree hierarchy based on currently expanded paths and search query.
 */
private fun flattenTree(
    nodes: List<FileTreeNode>,
    expandedPaths: Set<String>,
    currentPath: String,
    searchQuery: String
): List<FlattenedTreeRow> {
    val result = mutableListOf<FlattenedTreeRow>()

    fun traverse(nodeList: List<FileTreeNode>, depth: Int) {
        for (node in nodeList) {
            val isExpanded = expandedPaths.contains(node.path) || (searchQuery.isNotEmpty() && hasMatchingDescendant(node, searchQuery))
            val isCurrentActive = node.isDirectory && node.path == currentPath
            val isAncestor = node.isDirectory && currentPath.isNotEmpty() && currentPath.startsWith("${node.path}/")

            // If searching, check if node matches or has matching descendants
            val matchesSearch = searchQuery.isEmpty() || node.name.contains(searchQuery, ignoreCase = true) || hasMatchingDescendant(node, searchQuery)

            if (matchesSearch) {
                result.add(
                    FlattenedTreeRow(
                        node = node,
                        depth = depth,
                        isExpanded = isExpanded,
                        hasChildren = node.children.isNotEmpty(),
                        isCurrentActiveDir = isCurrentActive,
                        isAncestorOfActiveDir = isAncestor
                    )
                )

                if (node.isDirectory && isExpanded && node.children.isNotEmpty()) {
                    traverse(node.children, depth + 1)
                }
            }
        }
    }

    traverse(nodes, 0)
    return result
}

private fun hasMatchingDescendant(node: FileTreeNode, query: String): Boolean {
    if (query.isEmpty()) return false
    for (child in node.children) {
        if (child.name.contains(query, ignoreCase = true)) return true
        if (child.isDirectory && hasMatchingDescendant(child, query)) return true
    }
    return false
}
