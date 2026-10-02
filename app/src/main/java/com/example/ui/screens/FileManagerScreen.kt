package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FileTreeNode
import com.example.data.model.GitHubBranch
import com.example.data.model.GitHubContentItem
import com.example.data.model.GitHubRepo
import com.example.data.repository.ZipExtractedItem
import com.example.ui.components.BulkDeleteDialog
import com.example.ui.components.CreateFileDialog
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.DeleteFileDialog
import com.example.ui.components.DeleteFolderDialog
import com.example.ui.components.DeleteProgressDialog
import com.example.ui.components.FileDetailsBottomSheet
import com.example.ui.components.FileGridItem
import com.example.ui.components.FileListItem
import com.example.ui.components.FolderDetailsBottomSheet
import com.example.ui.components.RecursiveFileTreeComponent
import com.example.ui.components.TopAppBarWithBreadcrumbs
import com.example.ui.components.UploadProgressDialog
import com.example.ui.viewmodel.Breadcrumb
import com.example.ui.viewmodel.DeleteProgress
import com.example.ui.viewmodel.ExplorerViewMode
import com.example.ui.viewmodel.UploadProgress

@Composable
fun FileManagerScreen(
    repo: GitHubRepo,
    currentPath: String,
    breadcrumbs: List<Breadcrumb>,
    activeBranch: String,
    availableBranches: List<GitHubBranch>,
    contents: List<GitHubContentItem>,
    isLoading: Boolean,
    errorMessage: String?,
    searchQuery: String,
    viewMode: ExplorerViewMode,
    treeRootNodes: List<FileTreeNode> = emptyList(),
    expandedPaths: Set<String> = emptySet(),
    isTreeLoading: Boolean = false,
    treeErrorMessage: String? = null,
    treeSearchQuery: String = "",
    uploadProgress: UploadProgress,
    extractedZipItems: List<ZipExtractedItem>,
    deleteProgress: DeleteProgress = DeleteProgress(),
    onNavigateBack: () -> Unit,
    onBreadcrumbClick: (String) -> Unit,
    onBranchSelected: (String) -> Unit,
    onToggleViewMode: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTreeSearchQueryChange: (String) -> Unit = {},
    onToggleFolder: (String) -> Unit = {},
    onExpandAllFolders: () -> Unit = {},
    onCollapseAllFolders: () -> Unit = {},
    onRefreshTree: () -> Unit = {},
    onNavigateToDirectory: (String) -> Unit = {},
    onOpenItem: (GitHubContentItem) -> Unit,
    onCreateFile: (name: String, content: String, message: String) -> Unit,
    onCreateFolder: (name: String) -> Unit,
    onDeleteFile: (item: GitHubContentItem, message: String) -> Unit,
    onDeleteFolder: (item: GitHubContentItem, message: String) -> Unit = { _, _ -> },
    onDeleteMultiple: (items: List<GitHubContentItem>, message: String) -> Unit = { _, _ -> },
    onDismissDeleteProgress: () -> Unit = {},
    onUploadSingleFile: (uri: Uri, targetName: String, message: String) -> Unit,
    onParseZip: (uri: Uri) -> Unit,
    onUploadZipItems: (items: List<ZipExtractedItem>, message: String) -> Unit,
    onCancelUpload: () -> Unit,
    onDismissUploadProgress: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchOpen by remember { mutableStateOf(false) }
    var isSpeedDialOpen by remember { mutableStateOf(false) }

    // Multi-Selection Mode
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<GitHubContentItem>()) }

    // Dialog & Sheet States
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var selectedItemForDetails by remember { mutableStateOf<GitHubContentItem?>(null) }
    var selectedItemForDelete by remember { mutableStateOf<GitHubContentItem?>(null) }
    var selectedFolderForDetails by remember { mutableStateOf<GitHubContentItem?>(null) }
    var selectedFolderForDelete by remember { mutableStateOf<GitHubContentItem?>(null) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }
    var showZipDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // File Pickers
    val singleFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileNameFromUri(context, uri) ?: "uploaded_file"
            onUploadSingleFile(uri, fileName, "Upload $fileName")
        }
    }

    val replaceFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val targetItem = selectedItemForDetails
        if (uri != null && targetItem != null) {
            onUploadSingleFile(uri, targetItem.name, "Replace ${targetItem.name}")
            selectedItemForDetails = null
        }
    }

    val zipPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onParseZip(uri)
            showZipDialog = true
        }
    }

    val filteredContents = remember(contents, searchQuery) {
        if (searchQuery.isBlank()) {
            contents
        } else {
            contents.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBarWithBreadcrumbs(
                repoName = repo.name,
                breadcrumbs = breadcrumbs,
                activeBranch = activeBranch,
                availableBranches = availableBranches,
                viewMode = viewMode,
                isSearchOpen = isSearchOpen,
                isSelectionMode = isSelectionMode,
                selectedCount = selectedItems.size,
                onNavigateBack = onNavigateBack,
                onBreadcrumbClick = onBreadcrumbClick,
                onBranchSelected = onBranchSelected,
                onToggleViewMode = onToggleViewMode,
                onToggleSearch = {
                    isSearchOpen = !isSearchOpen
                    if (!isSearchOpen) onSearchQueryChange("")
                },
                onToggleSelectionMode = {
                    isSelectionMode = !isSelectionMode
                    if (!isSelectionMode) selectedItems = emptySet()
                },
                onExitSelectionMode = {
                    isSelectionMode = false
                    selectedItems = emptySet()
                },
                onSelectAll = {
                    if (selectedItems.size == filteredContents.size) {
                        selectedItems = emptySet()
                    } else {
                        selectedItems = filteredContents.toSet()
                    }
                },
                onDeleteSelected = {
                    if (selectedItems.isNotEmpty()) {
                        showBulkDeleteDialog = true
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Speed Dial Sub-actions
                    AnimatedVisibility(visible = isSpeedDialOpen) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Extract & Upload ZIP
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "Extract & Upload ZIP",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                SmallFloatingActionButton(
                                    onClick = {
                                        isSpeedDialOpen = false
                                        zipPicker.launch("application/zip")
                                    },
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.testTag("upload_zip_action")
                                ) {
                                    Icon(imageVector = Icons.Default.Archive, contentDescription = "ZIP Upload")
                                }
                            }

                            // Upload Single File
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "Upload File from Phone",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                SmallFloatingActionButton(
                                    onClick = {
                                        isSpeedDialOpen = false
                                        singleFilePicker.launch("*/*")
                                    },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.testTag("upload_file_action")
                                ) {
                                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = "Upload File")
                                }
                            }

                            // New Folder
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "New Folder",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                SmallFloatingActionButton(
                                    onClick = {
                                        isSpeedDialOpen = false
                                        showCreateFolderDialog = true
                                    },
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.testTag("new_folder_action")
                                ) {
                                    Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "New Folder")
                                }
                            }

                            // New File
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "New File",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                SmallFloatingActionButton(
                                    onClick = {
                                        isSpeedDialOpen = false
                                        showCreateFileDialog = true
                                    },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.testTag("new_file_action")
                                ) {
                                    Icon(imageVector = Icons.Default.NoteAdd, contentDescription = "New File")
                                }
                            }
                        }
                    }

                    // Main Speed Dial FAB
                    FloatingActionButton(
                        onClick = { isSpeedDialOpen = !isSpeedDialOpen },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier.testTag("file_manager_fab")
                    ) {
                        Icon(
                            imageVector = if (isSpeedDialOpen) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Actions"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Optional Search Bar
            AnimatedVisibility(visible = isSearchOpen) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Filter files in this folder...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("filter_files_input")
                    )
                }
            }

            // Contents: Tree, List, or Grid
            if (viewMode == ExplorerViewMode.TREE) {
                RecursiveFileTreeComponent(
                    rootNodes = treeRootNodes,
                    expandedPaths = expandedPaths,
                    currentPath = currentPath,
                    isLoading = isTreeLoading,
                    errorMessage = treeErrorMessage,
                    searchQuery = treeSearchQuery,
                    onSearchQueryChange = onTreeSearchQueryChange,
                    onToggleFolder = onToggleFolder,
                    onExpandAll = onExpandAllFolders,
                    onCollapseAll = onCollapseAllFolders,
                    onNavigateToDirectory = onNavigateToDirectory,
                    onOpenFile = { node -> onOpenItem(node.toContentItem()) },
                    onItemMenuClick = { item ->
                        if (item.isDirectory) {
                            selectedFolderForDetails = item
                        } else {
                            selectedItemForDetails = item
                        }
                    },
                    onRefreshTree = onRefreshTree
                )
            } else if (isLoading && contents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Fetching repository files...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (filteredContents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching files" else "This directory is empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button to create files, folders or upload files from your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (viewMode == ExplorerViewMode.LIST) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredContents, key = { it.path }) { item ->
                            val isSelected = selectedItems.contains(item)
                            FileListItem(
                                item = item,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedItems = if (isSelected) selectedItems - item else selectedItems + item
                                    } else {
                                        onOpenItem(item)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedItems = setOf(item)
                                    } else {
                                        selectedItems = if (isSelected) selectedItems - item else selectedItems + item
                                    }
                                },
                                onMenuClick = {
                                    if (item.isDirectory) {
                                        selectedFolderForDetails = item
                                    } else {
                                        selectedItemForDetails = item
                                    }
                                }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 105.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredContents, key = { it.path }) { item ->
                            val isSelected = selectedItems.contains(item)
                            FileGridItem(
                                item = item,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedItems = if (isSelected) selectedItems - item else selectedItems + item
                                    } else {
                                        onOpenItem(item)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedItems = setOf(item)
                                    } else {
                                        selectedItems = if (isSelected) selectedItems - item else selectedItems + item
                                    }
                                },
                                onMenuClick = {
                                    if (item.isDirectory) {
                                        selectedFolderForDetails = item
                                    } else {
                                        selectedItemForDetails = item
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    if (showCreateFileDialog) {
        CreateFileDialog(
            onDismiss = { showCreateFileDialog = false },
            onConfirm = { name, content, msg ->
                showCreateFileDialog = false
                onCreateFile(name, content, msg)
            }
        )
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name ->
                showCreateFolderDialog = false
                onCreateFolder(name)
            }
        )
    }

    // File Details Bottom Sheet
    selectedItemForDetails?.let { item ->
        FileDetailsBottomSheet(
            item = item,
            onDismiss = { selectedItemForDetails = null },
            onOpenEdit = {
                selectedItemForDetails = null
                onOpenItem(item)
            },
            onReplaceFile = {
                replaceFilePicker.launch("*/*")
            },
            onDeleteRequest = {
                selectedItemForDetails = null
                selectedItemForDelete = item
            },
            onShare = {
                selectedItemForDetails = null
            }
        )
    }

    // Folder Details Bottom Sheet
    selectedFolderForDetails?.let { folder ->
        FolderDetailsBottomSheet(
            item = folder,
            onDismiss = { selectedFolderForDetails = null },
            onOpenFolder = {
                selectedFolderForDetails = null
                onOpenItem(folder)
            },
            onDeleteRequest = {
                selectedFolderForDetails = null
                selectedFolderForDelete = folder
            }
        )
    }

    // Single File Delete Dialog
    selectedItemForDelete?.let { item ->
        DeleteFileDialog(
            item = item,
            onDismiss = { selectedItemForDelete = null },
            onConfirm = { msg ->
                selectedItemForDelete = null
                onDeleteFile(item, msg)
            }
        )
    }

    // Folder Delete Dialog
    selectedFolderForDelete?.let { folder ->
        DeleteFolderDialog(
            item = folder,
            onDismiss = { selectedFolderForDelete = null },
            onConfirm = { msg ->
                selectedFolderForDelete = null
                onDeleteFolder(folder, msg)
            }
        )
    }

    // Bulk Delete Dialog
    if (showBulkDeleteDialog && selectedItems.isNotEmpty()) {
        BulkDeleteDialog(
            selectedItems = selectedItems.toList(),
            onDismiss = { showBulkDeleteDialog = false },
            onConfirm = { msg ->
                val itemsToDelete = selectedItems.toList()
                showBulkDeleteDialog = false
                isSelectionMode = false
                selectedItems = emptySet()
                onDeleteMultiple(itemsToDelete, msg)
            }
        )
    }

    // Delete Progress Dialog
    if (deleteProgress.isDeleting || deleteProgress.isCompleted || deleteProgress.errorMessage != null) {
        DeleteProgressDialog(
            progress = deleteProgress,
            onDismiss = onDismissDeleteProgress
        )
    }

    if (showZipDialog && extractedZipItems.isNotEmpty()) {
        ZipUploadDialog(
            items = extractedZipItems,
            currentRepoPath = currentPath,
            onDismiss = { showZipDialog = false },
            onConfirmUpload = { items, msg ->
                showZipDialog = false
                onUploadZipItems(items, msg)
            }
        )
    }

    if (uploadProgress.isUploading || uploadProgress.isCompleted || uploadProgress.errorMessage != null) {
        UploadProgressDialog(
            progress = uploadProgress,
            onCancel = onCancelUpload,
            onDismiss = onDismissUploadProgress
        )
    }
}

private fun getFileNameFromUri(context: android.content.Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    name = it.getString(index)
                }
            }
        }
    }
    return name ?: uri.path?.substringAfterLast('/')
}
