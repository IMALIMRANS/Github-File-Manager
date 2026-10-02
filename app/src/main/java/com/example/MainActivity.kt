package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.FileViewerEditorScreen
import com.example.ui.screens.RepoListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.components.TokenExpiredDialog
import com.example.ui.theme.GitHubFileManagerTheme
import com.example.ui.viewmodel.GitHubViewModel
import com.example.ui.viewmodel.ScreenDestination
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: GitHubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GitHubFileManagerTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    viewModel.toastEvent.collectLatest { message ->
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    GitHubFileManagerApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GitHubFileManagerApp(
    viewModel: GitHubViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val savedAccounts by viewModel.savedAccounts.collectAsStateWithLifecycle()
    val activeAccountId by viewModel.activeAccountId.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    val repositories by viewModel.repositories.collectAsStateWithLifecycle()
    val isRepoListLoading by viewModel.isRepoListLoading.collectAsStateWithLifecycle()
    val repoListError by viewModel.repoListError.collectAsStateWithLifecycle()
    val repoSearchQuery by viewModel.repoSearchQuery.collectAsStateWithLifecycle()
    val repoSort by viewModel.repoSort.collectAsStateWithLifecycle()
    val repoFilter by viewModel.repoFilter.collectAsStateWithLifecycle()
    val pinnedRepos by viewModel.pinnedRepos.collectAsStateWithLifecycle()

    val activeRepo by viewModel.activeRepo.collectAsStateWithLifecycle()
    val currentPath by viewModel.currentPath.collectAsStateWithLifecycle()
    val activeBranch by viewModel.activeBranch.collectAsStateWithLifecycle()
    val availableBranches by viewModel.availableBranches.collectAsStateWithLifecycle()
    val currentContents by viewModel.currentContents.collectAsStateWithLifecycle()
    val isFileExplorerLoading by viewModel.isFileExplorerLoading.collectAsStateWithLifecycle()
    val fileExplorerError by viewModel.fileExplorerError.collectAsStateWithLifecycle()
    val fileSearchQuery by viewModel.fileSearchQuery.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()

    val treeRootNodes by viewModel.treeRootNodes.collectAsStateWithLifecycle()
    val expandedFolderPaths by viewModel.expandedFolderPaths.collectAsStateWithLifecycle()
    val isTreeLoading by viewModel.isTreeLoading.collectAsStateWithLifecycle()
    val treeError by viewModel.treeError.collectAsStateWithLifecycle()
    val treeSearchQuery by viewModel.treeSearchQuery.collectAsStateWithLifecycle()

    val activeFile by viewModel.activeFile.collectAsStateWithLifecycle()
    val fileEditorContent by viewModel.fileEditorContent.collectAsStateWithLifecycle()
    val originalFileContent by viewModel.originalFileContent.collectAsStateWithLifecycle()
    val isFileEditorLoading by viewModel.isFileEditorLoading.collectAsStateWithLifecycle()
    val isFileSaving by viewModel.isFileSaving.collectAsStateWithLifecycle()

    val uploadProgress by viewModel.uploadProgress.collectAsStateWithLifecycle()
    val deleteProgress by viewModel.deleteProgress.collectAsStateWithLifecycle()
    val tokenExpiredState by viewModel.tokenExpiredState.collectAsStateWithLifecycle()
    val extractedZipItems by viewModel.extractedZipItems.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_navigation",
        modifier = modifier
    ) { screen ->
        when (screen) {
            is ScreenDestination.Auth -> {
                AuthScreen(
                    isLoading = isAuthLoading,
                    errorMessage = authError,
                    onAuthenticate = { token -> viewModel.validateToken(token) }
                )
            }

            is ScreenDestination.RepoList -> {
                RepoListScreen(
                    user = currentUser,
                    savedAccounts = savedAccounts,
                    activeAccountId = activeAccountId,
                    repositories = repositories,
                    isLoading = isRepoListLoading,
                    errorMessage = repoListError,
                    searchQuery = repoSearchQuery,
                    currentSort = repoSort,
                    currentFilter = repoFilter,
                    pinnedRepos = pinnedRepos,
                    onSearchQueryChange = { viewModel.setRepoSearchQuery(it) },
                    onSortChange = { viewModel.setRepoSort(it) },
                    onFilterChange = { viewModel.setRepoFilter(it) },
                    onTogglePin = { viewModel.togglePinRepo(it) },
                    onSwitchAccount = { viewModel.switchAccount(it) },
                    onAddNewAccount = { token -> viewModel.addNewAccount(token) {} },
                    onRemoveAccount = { viewModel.removeAccount(it) },
                    onOpenRepo = { repo -> viewModel.openRepository(repo) },
                    onCreateRepo = { name, desc, isPrivate ->
                        viewModel.createRepository(name, desc, isPrivate) {}
                    },
                    onRefresh = { viewModel.loadRepositories() },
                    onOpenSettings = { viewModel.navigateTo(ScreenDestination.Settings) }
                )
            }

            is ScreenDestination.FileManager -> {
                BackHandler {
                    if (currentPath.isNotEmpty()) {
                        viewModel.navigateUp()
                    } else {
                        viewModel.navigateTo(ScreenDestination.RepoList)
                    }
                }

                activeRepo?.let { repo ->
                    FileManagerScreen(
                        repo = repo,
                        currentPath = currentPath,
                        breadcrumbs = viewModel.getBreadcrumbs(),
                        activeBranch = activeBranch,
                        availableBranches = availableBranches,
                        contents = currentContents,
                        isLoading = isFileExplorerLoading,
                        errorMessage = fileExplorerError,
                        searchQuery = fileSearchQuery,
                        viewMode = viewMode,
                        treeRootNodes = treeRootNodes,
                        expandedPaths = expandedFolderPaths,
                        isTreeLoading = isTreeLoading,
                        treeErrorMessage = treeError,
                        treeSearchQuery = treeSearchQuery,
                        uploadProgress = uploadProgress,
                        deleteProgress = deleteProgress,
                        extractedZipItems = extractedZipItems,
                        onNavigateBack = {
                            if (currentPath.isNotEmpty()) {
                                viewModel.navigateUp()
                            } else {
                                viewModel.navigateTo(ScreenDestination.RepoList)
                            }
                        },
                        onBreadcrumbClick = { path -> viewModel.navigateToDirectory(path) },
                        onBranchSelected = { branch -> viewModel.switchBranch(branch) },
                        onToggleViewMode = { viewModel.toggleViewMode() },
                        onSearchQueryChange = { viewModel.setFileSearchQuery(it) },
                        onTreeSearchQueryChange = { viewModel.setTreeSearchQuery(it) },
                        onToggleFolder = { path -> viewModel.toggleFolderExpansion(path) },
                        onExpandAllFolders = { viewModel.expandAllFolders() },
                        onCollapseAllFolders = { viewModel.collapseAllFolders() },
                        onRefreshTree = { viewModel.loadRepositoryTree(force = true) },
                        onNavigateToDirectory = { path -> viewModel.navigateToDirectory(path) },
                        onOpenItem = { item ->
                            if (item.isDirectory) {
                                viewModel.navigateToDirectory(item.path)
                            } else {
                                viewModel.openFile(item)
                            }
                        },
                        onCreateFile = { name, content, msg ->
                            viewModel.createNewFile(name, content, msg) {}
                        },
                        onCreateFolder = { name ->
                            viewModel.createFolder(name) {}
                        },
                        onDeleteFile = { item, msg ->
                            viewModel.deleteFile(item, msg) {}
                        },
                        onDeleteFolder = { item, msg ->
                            viewModel.deleteFolder(item, msg) {}
                        },
                        onDeleteMultiple = { items, msg ->
                            viewModel.deleteMultipleItems(items, msg) {}
                        },
                        onDismissDeleteProgress = { viewModel.dismissDeleteProgress() },
                        onUploadSingleFile = { uri, name, msg ->
                            viewModel.uploadSingleFile(uri, name, msg) {}
                        },
                        onParseZip = { uri ->
                            viewModel.parseZip(uri) {}
                        },
                        onUploadZipItems = { items, msg ->
                            viewModel.uploadExtractedZip(items, msg) {}
                        },
                        onCancelUpload = { viewModel.cancelUpload() },
                        onDismissUploadProgress = { viewModel.dismissUploadProgress() },
                        onRefresh = { viewModel.loadDirectoryContents() }
                    )
                }
            }

            is ScreenDestination.FileEditor -> {
                BackHandler {
                    activeRepo?.let { repo ->
                        viewModel.navigateTo(ScreenDestination.FileManager(repo))
                    } ?: viewModel.navigateTo(ScreenDestination.RepoList)
                }

                val targetRepo = activeRepo ?: screen.repo
                val targetItem = activeFile ?: screen.item

                FileViewerEditorScreen(
                    repo = targetRepo,
                    item = targetItem,
                    editorContent = fileEditorContent,
                    originalContent = originalFileContent,
                    isLoading = isFileEditorLoading,
                    isSaving = isFileSaving,
                    onContentChange = { viewModel.updateEditorContent(it) },
                    onCommitChanges = { msg, onDone ->
                        viewModel.commitFileChanges(msg, onSuccess = onDone)
                    },
                    onNavigateBack = {
                        viewModel.navigateTo(ScreenDestination.FileManager(targetRepo))
                    }
                )
            }

            is ScreenDestination.Settings -> {
                BackHandler {
                    viewModel.navigateTo(ScreenDestination.RepoList)
                }

                SettingsScreen(
                    user = currentUser,
                    savedAccounts = savedAccounts,
                    activeAccountId = activeAccountId,
                    currentToken = viewModel.getCurrentToken(),
                    committerName = viewModel.getCommitterName(),
                    committerEmail = viewModel.getCommitterEmail(),
                    onSwitchAccount = { viewModel.switchAccount(it) },
                    onAddNewAccount = { token -> viewModel.addNewAccount(token) {} },
                    onRemoveAccount = { viewModel.removeAccount(it) },
                    onSaveCommitterInfo = { name, email -> viewModel.setCommitterDetails(name, email) },
                    onUpdateToken = { token -> viewModel.validateToken(token) },
                    onLogout = { viewModel.logout() },
                    onNavigateBack = { viewModel.navigateTo(ScreenDestination.RepoList) }
                )
            }
        }
    }

    // Global Token Expired Dialog
    tokenExpiredState?.let { expiredInfo ->
        TokenExpiredDialog(
            user = expiredInfo.user,
            message = expiredInfo.message,
            onDismiss = { viewModel.dismissTokenExpired() },
            onUpdateToken = { newToken ->
                viewModel.updateExpiredToken(newToken)
            },
            onSwitchAccount = {
                viewModel.dismissTokenExpired()
                viewModel.navigateTo(ScreenDestination.Settings)
            },
            onSignOut = {
                viewModel.dismissTokenExpired()
                viewModel.logout()
            }
        )
    }
}
