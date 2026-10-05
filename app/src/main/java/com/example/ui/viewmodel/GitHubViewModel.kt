package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FileTreeNode
import com.example.data.model.GitHubBranch
import com.example.data.model.GitHubContentItem
import com.example.data.model.GitHubRepo
import com.example.data.model.GitHubUser
import com.example.data.repository.GitHubRepository
import com.example.data.repository.Resource
import com.example.data.repository.ZipExtractedItem
import com.example.data.storage.TokenManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

sealed interface ScreenDestination {
    data object Auth : ScreenDestination
    data object RepoList : ScreenDestination
    data class FileManager(val repo: GitHubRepo) : ScreenDestination
    data class FileEditor(val repo: GitHubRepo, val item: GitHubContentItem, val initialContent: String = "") : ScreenDestination
    data object Settings : ScreenDestination
}

enum class RepoSortOption {
    UPDATED,
    STARS,
    NAME,
    SIZE
}

enum class RepoFilterOption {
    ALL,
    PUBLIC,
    PRIVATE,
    FORKS
}

enum class ExplorerViewMode {
    LIST,
    GRID,
    TREE
}

data class Breadcrumb(
    val title: String,
    val fullPath: String
)

data class UploadProgress(
    val isUploading: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val currentFileName: String = "",
    val errorMessage: String? = null,
    val isCompleted: Boolean = false
)

data class DeleteProgress(
    val isDeleting: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val currentItemName: String = "",
    val errorMessage: String? = null,
    val isCompleted: Boolean = false
)

data class TokenExpiredState(
    val isExpired: Boolean = true,
    val user: GitHubUser? = null,
    val accountId: Long? = null,
    val message: String? = null
)

class GitHubViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GitHubRepository(application)
    private val tokenManager = TokenManager(application)

    // Token Expired / Session Alert State
    private val _tokenExpiredState = MutableStateFlow<TokenExpiredState?>(null)
    val tokenExpiredState: StateFlow<TokenExpiredState?> = _tokenExpiredState.asStateFlow()

    // Navigation State
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Auth)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Global Notifications / Toast / Snackbars
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Multi-Account State
    private val _savedAccounts = MutableStateFlow<List<com.example.data.model.SavedAccount>>(emptyList())
    val savedAccounts: StateFlow<List<com.example.data.model.SavedAccount>> = _savedAccounts.asStateFlow()

    private val _activeAccountId = MutableStateFlow<Long?>(null)
    val activeAccountId: StateFlow<Long?> = _activeAccountId.asStateFlow()

    // Auth State
    private val _currentUser = MutableStateFlow<GitHubUser?>(null)
    val currentUser: StateFlow<GitHubUser?> = _currentUser.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Repositories State
    private val _repositories = MutableStateFlow<List<GitHubRepo>>(emptyList())
    val repositories: StateFlow<List<GitHubRepo>> = _repositories.asStateFlow()

    private val _isRepoListLoading = MutableStateFlow(false)
    val isRepoListLoading: StateFlow<Boolean> = _isRepoListLoading.asStateFlow()

    private val _repoListError = MutableStateFlow<String?>(null)
    val repoListError: StateFlow<String?> = _repoListError.asStateFlow()

    private val _repoSearchQuery = MutableStateFlow("")
    val repoSearchQuery: StateFlow<String> = _repoSearchQuery.asStateFlow()

    private val _repoSort = MutableStateFlow(RepoSortOption.UPDATED)
    val repoSort: StateFlow<RepoSortOption> = _repoSort.asStateFlow()

    private val _repoFilter = MutableStateFlow(RepoFilterOption.ALL)
    val repoFilter: StateFlow<RepoFilterOption> = _repoFilter.asStateFlow()

    private val _pinnedRepos = MutableStateFlow<Set<String>>(emptySet())
    val pinnedRepos: StateFlow<Set<String>> = _pinnedRepos.asStateFlow()

    // File Manager State
    private val _activeRepo = MutableStateFlow<GitHubRepo?>(null)
    val activeRepo: StateFlow<GitHubRepo?> = _activeRepo.asStateFlow()

    private val _currentPath = MutableStateFlow("")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _activeBranch = MutableStateFlow("main")
    val activeBranch: StateFlow<String> = _activeBranch.asStateFlow()

    private val _availableBranches = MutableStateFlow<List<GitHubBranch>>(emptyList())
    val availableBranches: StateFlow<List<GitHubBranch>> = _availableBranches.asStateFlow()

    private val _currentContents = MutableStateFlow<List<GitHubContentItem>>(emptyList())
    val currentContents: StateFlow<List<GitHubContentItem>> = _currentContents.asStateFlow()

    private val _isFileExplorerLoading = MutableStateFlow(false)
    val isFileExplorerLoading: StateFlow<Boolean> = _isFileExplorerLoading.asStateFlow()

    private val _fileExplorerError = MutableStateFlow<String?>(null)
    val fileExplorerError: StateFlow<String?> = _fileExplorerError.asStateFlow()

    private val _fileSearchQuery = MutableStateFlow("")
    val fileSearchQuery: StateFlow<String> = _fileSearchQuery.asStateFlow()

    private val _viewMode = MutableStateFlow(ExplorerViewMode.LIST)
    val viewMode: StateFlow<ExplorerViewMode> = _viewMode.asStateFlow()

    // Recursive File Tree State
    private val _treeRootNodes = MutableStateFlow<List<FileTreeNode>>(emptyList())
    val treeRootNodes: StateFlow<List<FileTreeNode>> = _treeRootNodes.asStateFlow()

    private val _expandedFolderPaths = MutableStateFlow<Set<String>>(emptySet())
    val expandedFolderPaths: StateFlow<Set<String>> = _expandedFolderPaths.asStateFlow()

    private val _isTreeLoading = MutableStateFlow(false)
    val isTreeLoading: StateFlow<Boolean> = _isTreeLoading.asStateFlow()

    private val _treeError = MutableStateFlow<String?>(null)
    val treeError: StateFlow<String?> = _treeError.asStateFlow()

    private val _treeSearchQuery = MutableStateFlow("")
    val treeSearchQuery: StateFlow<String> = _treeSearchQuery.asStateFlow()

    // File Editor State
    private val _activeFile = MutableStateFlow<GitHubContentItem?>(null)
    val activeFile: StateFlow<GitHubContentItem?> = _activeFile.asStateFlow()

    private val _fileEditorContent = MutableStateFlow("")
    val fileEditorContent: StateFlow<String> = _fileEditorContent.asStateFlow()

    private val _originalFileContent = MutableStateFlow("")
    val originalFileContent: StateFlow<String> = _originalFileContent.asStateFlow()

    private val _isFileEditorLoading = MutableStateFlow(false)
    val isFileEditorLoading: StateFlow<Boolean> = _isFileEditorLoading.asStateFlow()

    private val _isFileSaving = MutableStateFlow(false)
    val isFileSaving: StateFlow<Boolean> = _isFileSaving.asStateFlow()

    // Upload & Zip Processing
    private val _uploadProgress = MutableStateFlow(UploadProgress())
    val uploadProgress: StateFlow<UploadProgress> = _uploadProgress.asStateFlow()

    private val _extractedZipItems = MutableStateFlow<List<ZipExtractedItem>>(emptyList())
    val extractedZipItems: StateFlow<List<ZipExtractedItem>> = _extractedZipItems.asStateFlow()

    private var activeUploadJob: Job? = null

    // Delete Progress
    private val _deleteProgress = MutableStateFlow(DeleteProgress())
    val deleteProgress: StateFlow<DeleteProgress> = _deleteProgress.asStateFlow()

    init {
        loadAccounts()
        checkSavedTokenAndInitialize()
        loadPinnedRepos()
    }

    fun loadAccounts() {
        _savedAccounts.value = tokenManager.getSavedAccounts()
        _activeAccountId.value = tokenManager.getActiveAccountId()
    }

    fun switchAccount(userId: Long) {
        if (tokenManager.switchAccount(userId)) {
            loadAccounts()
            _currentUser.value = tokenManager.getSavedUser()
            _activeRepo.value = null
            _currentPath.value = ""
            _currentContents.value = emptyList()
            _treeRootNodes.value = emptyList()
            _expandedFolderPaths.value = emptySet()
            _currentScreen.value = ScreenDestination.RepoList
            loadPinnedRepos()
            loadRepositories()
            viewModelScope.launch {
                _toastEvent.emit("Switched to @${_currentUser.value?.login ?: "account"}")
            }
        }
    }

    fun addNewAccount(token: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            when (val result = repository.validateAndSaveToken(token, preventDuplicate = true)) {
                is Resource.Success -> {
                    _isAuthLoading.value = false
                    loadAccounts()
                    _currentUser.value = result.data
                    _currentScreen.value = ScreenDestination.RepoList
                    _toastEvent.emit("Account @${result.data.login} added successfully!")
                    loadRepositories()
                    onDone(true)
                }
                is Resource.Error -> {
                    _isAuthLoading.value = false
                    _authError.value = result.message
                    _toastEvent.emit(result.message)
                    onDone(false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun removeAccount(userId: Long) {
        val wasActive = tokenManager.getActiveAccountId() == userId
        tokenManager.removeAccount(userId)
        loadAccounts()
        if (tokenManager.getActiveAccount() == null) {
            logout()
        } else if (wasActive) {
            _currentUser.value = tokenManager.getSavedUser()
            _activeRepo.value = null
            _currentPath.value = ""
            _currentContents.value = emptyList()
            _treeRootNodes.value = emptyList()
            _currentScreen.value = ScreenDestination.RepoList
            loadRepositories()
            viewModelScope.launch {
                _toastEvent.emit("Switched to active account @${_currentUser.value?.login}")
            }
        } else {
            viewModelScope.launch {
                _toastEvent.emit("Account removed.")
            }
        }
    }

    private fun loadPinnedRepos() {
        _pinnedRepos.value = tokenManager.getPinnedRepoIds()
    }

    fun togglePinRepo(repoFullName: String) {
        tokenManager.togglePinRepo(repoFullName)
        loadPinnedRepos()
    }

    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
    }

    private fun checkSavedTokenAndInitialize() {
        val token = tokenManager.getToken()
        val user = tokenManager.getSavedUser()
        if (!token.isNullOrEmpty()) {
            if (user != null) {
                _currentUser.value = user
                _currentScreen.value = ScreenDestination.RepoList
                loadRepositories()
            } else {
                validateToken(token)
            }
        } else {
            _currentScreen.value = ScreenDestination.Auth
        }
    }

    fun validateToken(token: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            when (val result = repository.validateAndSaveToken(token, preventDuplicate = false)) {
                is Resource.Success -> {
                    _currentUser.value = result.data
                    _isAuthLoading.value = false
                    loadAccounts()
                    _currentScreen.value = ScreenDestination.RepoList
                    _toastEvent.emit("Welcome, ${result.data.login}!")
                    loadRepositories()
                }
                is Resource.Error -> {
                    _isAuthLoading.value = false
                    _authError.value = result.message
                    if (_currentScreen.value !is ScreenDestination.Auth) {
                        checkForTokenExpiration(result)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun checkForTokenExpiration(error: Resource.Error) {
        val msg = error.message
        if (error.statusCode == 401 ||
            msg.contains("Bad credentials", ignoreCase = true) ||
            msg.contains("Requires authentication", ignoreCase = true) ||
            msg.contains("token has expired", ignoreCase = true) ||
            msg.contains("expired", ignoreCase = true)
        ) {
            val user = _currentUser.value
            _tokenExpiredState.value = TokenExpiredState(
                isExpired = true,
                user = user,
                accountId = _activeAccountId.value,
                message = "Your GitHub Personal Access Token for @${user?.login ?: "this account"} has expired or is invalid. Please update the token to continue accessing repositories."
            )
        }
    }

    fun dismissTokenExpired() {
        _tokenExpiredState.value = null
    }

    fun updateExpiredToken(newToken: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            when (val res = repository.validateAndSaveToken(newToken, preventDuplicate = false)) {
                is Resource.Success -> {
                    _currentUser.value = res.data
                    _tokenExpiredState.value = null
                    _isAuthLoading.value = false
                    loadAccounts()
                    _toastEvent.emit("Token updated successfully for @${res.data.login}!")
                    if (_currentScreen.value is ScreenDestination.FileManager) {
                        loadDirectoryContents()
                        loadRepositoryTree(force = true)
                    } else {
                        loadRepositories()
                    }
                    onDone(true)
                }
                is Resource.Error -> {
                    _isAuthLoading.value = false
                    _toastEvent.emit("Failed to update token: ${res.message}")
                    onDone(false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        tokenManager.clearToken()
        loadAccounts()
        val nextAccount = tokenManager.getActiveAccount()
        if (nextAccount != null) {
            _currentUser.value = nextAccount.user
            _activeRepo.value = null
            _currentPath.value = ""
            _currentContents.value = emptyList()
            _treeRootNodes.value = emptyList()
            _currentScreen.value = ScreenDestination.RepoList
            loadRepositories()
            viewModelScope.launch {
                _toastEvent.emit("Signed out. Switched to @${nextAccount.user.login}")
            }
        } else {
            _currentUser.value = null
            _repositories.value = emptyList()
            _activeRepo.value = null
            _currentPath.value = ""
            _currentContents.value = emptyList()
            _currentScreen.value = ScreenDestination.Auth
        }
    }

    fun setCommitterDetails(name: String, email: String) {
        tokenManager.setCommitterInfo(name, email)
        viewModelScope.launch {
            _toastEvent.emit("Committer settings updated.")
        }
    }

    fun getCommitterName(): String = tokenManager.getCommitterName() ?: _currentUser.value?.name ?: _currentUser.value?.login ?: ""
    fun getCommitterEmail(): String = tokenManager.getCommitterEmail() ?: _currentUser.value?.email ?: ""
    fun getCurrentToken(): String = tokenManager.getToken() ?: ""

    fun setRepoSearchQuery(query: String) {
        _repoSearchQuery.value = query
    }

    fun setRepoSort(sort: RepoSortOption) {
        _repoSort.value = sort
    }

    fun setRepoFilter(filter: RepoFilterOption) {
        _repoFilter.value = filter
    }

    fun setFileSearchQuery(query: String) {
        _fileSearchQuery.value = query
    }

    fun setTreeSearchQuery(query: String) {
        _treeSearchQuery.value = query
    }

    fun setViewMode(mode: ExplorerViewMode) {
        _viewMode.value = mode
        if (mode == ExplorerViewMode.TREE && _treeRootNodes.value.isEmpty() && !_isTreeLoading.value) {
            loadRepositoryTree()
        }
    }

    fun toggleViewMode() {
        val nextMode = when (_viewMode.value) {
            ExplorerViewMode.LIST -> ExplorerViewMode.GRID
            ExplorerViewMode.GRID -> ExplorerViewMode.TREE
            ExplorerViewMode.TREE -> ExplorerViewMode.LIST
        }
        setViewMode(nextMode)
    }

    fun toggleFolderExpansion(path: String) {
        val cleanPath = path.trim().removePrefix("/").removeSuffix("/")
        val currentSet = _expandedFolderPaths.value.toMutableSet()
        if (currentSet.contains(cleanPath)) {
            currentSet.remove(cleanPath)
        } else {
            currentSet.add(cleanPath)
        }
        _expandedFolderPaths.value = currentSet
    }

    fun expandFolder(path: String) {
        val cleanPath = path.trim().removePrefix("/").removeSuffix("/")
        val currentSet = _expandedFolderPaths.value.toMutableSet()
        currentSet.add(cleanPath)
        // Also expand all ancestors
        if (cleanPath.contains('/')) {
            val parts = cleanPath.split('/')
            var acc = ""
            for (i in 0 until parts.size - 1) {
                acc = if (acc.isEmpty()) parts[i] else "$acc/${parts[i]}"
                currentSet.add(acc)
            }
        }
        _expandedFolderPaths.value = currentSet
    }

    fun collapseFolder(path: String) {
        val cleanPath = path.trim().removePrefix("/").removeSuffix("/")
        val currentSet = _expandedFolderPaths.value.toMutableSet()
        currentSet.remove(cleanPath)
        _expandedFolderPaths.value = currentSet
    }

    fun expandAllFolders() {
        val allDirPaths = mutableSetOf<String>()
        fun collectDirs(nodes: List<FileTreeNode>) {
            for (node in nodes) {
                if (node.isDirectory) {
                    allDirPaths.add(node.path)
                    collectDirs(node.children)
                }
            }
        }
        collectDirs(_treeRootNodes.value)
        _expandedFolderPaths.value = allDirPaths
    }

    fun collapseAllFolders() {
        _expandedFolderPaths.value = emptySet()
    }

    fun loadRepositoryTree(force: Boolean = false) {
        val repo = _activeRepo.value ?: return
        val owner = repo.owner?.login ?: _currentUser.value?.login ?: return
        val branch = _activeBranch.value.ifBlank { repo.defaultBranch.ifEmpty { "main" } }

        if (!force && _treeRootNodes.value.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _isTreeLoading.value = true
            _treeError.value = null
            when (val res = repository.fetchRecursiveGitTree(owner, repo.name, branch)) {
                is Resource.Success -> {
                    val entries = res.data.tree
                    val builtTree = FileTreeNode.buildTreeFromEntries(entries)
                    _treeRootNodes.value = builtTree
                    _isTreeLoading.value = false

                    // Auto-expand current active path in the tree if present
                    if (_currentPath.value.isNotEmpty()) {
                        expandFolder(_currentPath.value)
                    }
                }
                is Resource.Error -> {
                    _isTreeLoading.value = false
                    _treeError.value = res.message
                    checkForTokenExpiration(res)
                    // Fallback to building tree from current directory if recursive git tree failed
                    if (_currentContents.value.isNotEmpty()) {
                        _treeRootNodes.value = FileTreeNode.fromContentItems(_currentContents.value)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadRepositories() {
        viewModelScope.launch {
            _isRepoListLoading.value = true
            _repoListError.value = null
            when (val result = repository.fetchRepositories()) {
                is Resource.Success -> {
                    _repositories.value = result.data
                    _isRepoListLoading.value = false
                }
                is Resource.Error -> {
                    _isRepoListLoading.value = false
                    _repoListError.value = result.message
                    checkForTokenExpiration(result)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun createRepository(name: String, description: String?, isPrivate: Boolean, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isRepoListLoading.value = true
            when (val result = repository.createRepository(name, description, isPrivate)) {
                is Resource.Success -> {
                    _isRepoListLoading.value = false
                    _toastEvent.emit("Repository '${result.data.name}' created!")
                    loadRepositories()
                    onDone(true)
                }
                is Resource.Error -> {
                    _isRepoListLoading.value = false
                    _toastEvent.emit("Error: ${result.message}")
                    onDone(false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun openRepository(repo: GitHubRepo, initialPath: String = "", branch: String? = null) {
        _activeRepo.value = repo
        _currentPath.value = initialPath
        _activeBranch.value = branch ?: repo.defaultBranch.ifEmpty { "main" }
        _currentScreen.value = ScreenDestination.FileManager(repo)
        _treeRootNodes.value = emptyList()
        _expandedFolderPaths.value = emptySet()
        loadBranches(repo.owner?.login ?: _currentUser.value?.login ?: "", repo.name)
        loadDirectoryContents()
        loadRepositoryTree(force = true)
    }

    fun loadBranches(owner: String, repo: String) {
        viewModelScope.launch {
            when (val res = repository.fetchBranches(owner, repo)) {
                is Resource.Success -> {
                    _availableBranches.value = res.data
                }
                else -> {}
            }
        }
    }

    fun switchBranch(branchName: String) {
        _activeBranch.value = branchName
        loadDirectoryContents()
        loadRepositoryTree(force = true)
    }

    fun navigateToDirectory(path: String) {
        val clean = path.trim().removePrefix("/").removeSuffix("/")
        _currentPath.value = clean
        _fileSearchQuery.value = ""
        if (clean.isNotEmpty()) {
            expandFolder(clean)
        }
        loadDirectoryContents()
    }

    fun navigateUp() {
        val path = _currentPath.value.trim().removePrefix("/").removeSuffix("/")
        if (path.isEmpty()) return
        val parentPath = if (path.contains("/")) path.substringBeforeLast("/") else ""
        navigateToDirectory(parentPath)
    }

    fun getBreadcrumbs(): List<Breadcrumb> {
        val repo = _activeRepo.value ?: return emptyList()
        val list = mutableListOf(Breadcrumb(title = repo.name, fullPath = ""))
        val path = _currentPath.value.trim().removePrefix("/").removeSuffix("/")
        if (path.isNotEmpty()) {
            val parts = path.split("/")
            var accumulated = ""
            for (part in parts) {
                accumulated = if (accumulated.isEmpty()) part else "$accumulated/$part"
                list.add(Breadcrumb(title = part, fullPath = accumulated))
            }
        }
        return list
    }

    fun loadDirectoryContents() {
        val repo = _activeRepo.value ?: return
        val owner = repo.owner?.login ?: _currentUser.value?.login ?: return
        viewModelScope.launch {
            _isFileExplorerLoading.value = true
            _fileExplorerError.value = null
            when (val res = repository.fetchDirectoryContents(owner, repo.name, _currentPath.value, _activeBranch.value)) {
                is Resource.Success -> {
                    _currentContents.value = res.data
                    _isFileExplorerLoading.value = false
                }
                is Resource.Error -> {
                    _isFileExplorerLoading.value = false
                    _fileExplorerError.value = res.message
                    checkForTokenExpiration(res)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun openFile(item: GitHubContentItem) {
        val repo = _activeRepo.value ?: return
        val owner = repo.owner?.login ?: _currentUser.value?.login ?: return
        _activeFile.value = item
        _currentScreen.value = ScreenDestination.FileEditor(repo, item)

        viewModelScope.launch {
            _isFileEditorLoading.value = true
            // If item already has content, use it, otherwise fetch
            if (!item.content.isNullOrEmpty()) {
                val decoded = repository.decodeBase64Content(item)
                _fileEditorContent.value = decoded
                _originalFileContent.value = decoded
                _isFileEditorLoading.value = false
            } else if (item.downloadUrl != null) {
                when (val rawRes = repository.fetchRawText(item.downloadUrl)) {
                    is Resource.Success -> {
                        _fileEditorContent.value = rawRes.data
                        _originalFileContent.value = rawRes.data
                        _isFileEditorLoading.value = false
                    }
                    is Resource.Error -> {
                        // Fallback to fetch single content item API
                        when (val fileRes = repository.fetchFileContent(owner, repo.name, item.path, _activeBranch.value)) {
                            is Resource.Success -> {
                                val text = repository.decodeBase64Content(fileRes.data)
                                _fileEditorContent.value = text
                                _originalFileContent.value = text
                                _activeFile.value = fileRes.data
                                _isFileEditorLoading.value = false
                            }
                            is Resource.Error -> {
                                _isFileEditorLoading.value = false
                                _toastEvent.emit("Error loading file: ${fileRes.message}")
                            }
                            is Resource.Loading -> {}
                        }
                    }
                    is Resource.Loading -> {}
                }
            } else {
                when (val fileRes = repository.fetchFileContent(owner, repo.name, item.path, _activeBranch.value)) {
                    is Resource.Success -> {
                        val text = repository.decodeBase64Content(fileRes.data)
                        _fileEditorContent.value = text
                        _originalFileContent.value = text
                        _activeFile.value = fileRes.data
                        _isFileEditorLoading.value = false
                    }
                    is Resource.Error -> {
                        _isFileEditorLoading.value = false
                        _toastEvent.emit("Error loading file: ${fileRes.message}")
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun updateEditorContent(newContent: String) {
        _fileEditorContent.value = newContent
    }

    fun commitFileChanges(
        commitMessage: String,
        targetBranch: String? = null,
        onSuccess: () -> Unit
    ) {
        val repo = _activeRepo.value ?: return
        val file = _activeFile.value ?: return
        val owner = repo.owner?.login ?: _currentUser.value?.login ?: return
        val branch = targetBranch ?: _activeBranch.value

        viewModelScope.launch {
            _isFileSaving.value = true
            val bytes = _fileEditorContent.value.toByteArray(StandardCharsets.UTF_8)
            val msg = commitMessage.ifBlank { "Update ${file.name}" }

            when (val res = repository.updateFile(
                owner = owner,
                repo = repo.name,
                path = file.path,
                contentBytes = bytes,
                commitMessage = msg,
                branch = branch
            )) {
                is Resource.Success -> {
                    _isFileSaving.value = false
                    _originalFileContent.value = _fileEditorContent.value
                    if (res.data.content != null) {
                        _activeFile.value = res.data.content
                    }
                    _toastEvent.emit("Changes committed successfully!")
                    loadDirectoryContents()
                    loadRepositoryTree(force = true)
                    onSuccess()
                }
                is Resource.Error -> {
                    _isFileSaving.value = false
                    _toastEvent.emit("Failed to update file: ${res.message}")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun createNewFile(
        fileName: String,
        content: String,
        commitMessage: String,
        onDone: (Boolean) -> Unit
    ) {
        val repo = _activeRepo.value ?: return
        val owner = repo.owner?.login ?: _currentUser.value?.login ?: return
        val currentDir = _currentPath.value.trim().removePrefix("/").removeSuffix("/")
        val fullPath = if (currentDir.isEmpty()) fileName.trim() else "$currentDir/${fileName.trim()}"

        viewModelScope.launch {
            _isFileExplorerLoading.value = true
            val bytes = content.toByteArray(StandardCharsets.UTF_8)
            val msg = commitMessage.ifBlank { "Create $fileName" }

            when (val res = repository.createOrUpdateFile(
                owner = owner,
                repo = repo.name,
                path = fullPath,
                contentBytes = bytes,
                commitMessage = msg,
                existingSha = null,
                branch = _activeBranch.value
            )) {
                is Resource.Success -> {
                    _isFileExplorerLoading.value = false
                    _toastEvent.emit("File '$fileName' created!")
                    loadDirectoryContents()
                    onDone(true)
                }
                is Resource.Error -> {
                    _isFileExplorerLoading.value = false
                    _toastEvent.emit("Failed to create file: ${res.message}")
                    onDone(false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun createFolder(folderName: String, onDone: (Boolean) -> Unit) {
        val repo = _activeRepo.value ?: return
        val owner = repo.owner?.login ?: _currentUser.value?.login ?: return
        val currentDir = _currentPath.value.trim().removePrefix("/").removeSuffix("/")
        val cleanName = folderName.trim().removePrefix("/").removeSuffix("/")
        val fullFolderPath = if (currentDir.isEmpty()) cleanName else "$currentDir/$cleanName"

        viewModelScope.launch {
            _isFileExplorerLoading.value = true
            when (val res = repository.createFolder(owner, repo.name, fullFolderPath, _activeBranch.value)) {
                is Resource.Success -> {
                    _isFileExplorerLoading.value = false
                    _toastEvent.emit("Folder '$cleanName' created!")
                    loadDirectoryContents()
                    onDone(true)
                }
                is Resource.Error -> {
                    _isFileExplorerLoading.value = false
                    _toastEvent.emit("Failed to create folder: ${res.message}")
                    onDone(false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteFile(item: GitHubContentItem, commitMessage: String, onDone: (Boolean) -> Unit) {
        val repo = _activeRepo.value ?: return
        val owner = repo.owner?.login ?: _currentUser.value?.login ?: return

        viewModelScope.launch {
            _isFileExplorerLoading.value = true
            val msg = commitMessage.ifBlank { "Delete ${item.name}" }
            when (val res = repository.deleteFile(
                owner = owner,
                repo = repo.name,
                path = item.path,
                sha = item.sha,
                commitMessage = msg,
                branch = _activeBranch.value
            )) {
                is Resource.Success -> {
                    _isFileExplorerLoading.value = false
                    _toastEvent.emit("File '${item.name}' deleted.")
                    loadDirectoryContents()
                    loadRepositoryTree(force = true)
                    onDone(true)
                }
                is Resource.Error -> {
                    _isFileExplorerLoading.value = false
                    _toastEvent.emit("Delete error: ${res.message}")
                    onDone(false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteFolder(folderItem: GitHubContentItem, commitMessage: String, onDone: (Boolean) -> Unit) {
        val repo = _activeRepo.value ?: return
        val owner = repo.owner?.login ?: _currentUser.value?.login ?: return

        viewModelScope.launch {
            _deleteProgress.value = DeleteProgress(
                isDeleting = true,
                current = 0,
                total = 1,
                currentItemName = "Resolving files in ${folderItem.name}...",
                isCompleted = false
            )

            val msg = commitMessage.ifBlank { "Delete folder ${folderItem.name}" }
            when (val res = repository.deleteFolderRecursively(
                owner = owner,
                repo = repo.name,
                folderPath = folderItem.path,
                commitMessage = msg,
                branch = _activeBranch.value,
                onProgress = { cur, tot, file ->
                    _deleteProgress.value = DeleteProgress(
                        isDeleting = true,
                        current = cur,
                        total = tot,
                        currentItemName = file.substringAfterLast('/'),
                        isCompleted = false
                    )
                }
            )) {
                is Resource.Success -> {
                    val count = res.data
                    _deleteProgress.value = DeleteProgress(
                        isDeleting = false,
                        current = count,
                        total = count,
                        currentItemName = "Completed",
                        isCompleted = true
                    )
                    _toastEvent.emit("Folder '${folderItem.name}' deleted ($count files removed).")
                    loadDirectoryContents()
                    loadRepositoryTree(force = true)
                    onDone(true)
                }
                is Resource.Error -> {
                    _deleteProgress.value = DeleteProgress(
                        isDeleting = false,
                        errorMessage = res.message
                    )
                    _toastEvent.emit("Folder delete error: ${res.message}")
                    onDone(false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteMultipleItems(items: List<GitHubContentItem>, commitMessage: String, onDone: (Boolean) -> Unit) {
        val repo = _activeRepo.value ?: return
        val owner = repo.owner?.login ?: _currentUser.value?.login ?: return

        if (items.isEmpty()) {
            onDone(true)
            return
        }

        viewModelScope.launch {
            _deleteProgress.value = DeleteProgress(
                isDeleting = true,
                current = 0,
                total = items.size,
                currentItemName = "Resolving items to delete...",
                isCompleted = false
            )

            val msg = commitMessage.ifBlank { "Batch delete ${items.size} items" }
            when (val res = repository.deleteMultipleItems(
                owner = owner,
                repo = repo.name,
                items = items,
                commitMessage = msg,
                branch = _activeBranch.value,
                onProgress = { cur, tot, file ->
                    _deleteProgress.value = DeleteProgress(
                        isDeleting = true,
                        current = cur,
                        total = tot,
                        currentItemName = file.substringAfterLast('/'),
                        isCompleted = false
                    )
                }
            )) {
                is Resource.Success -> {
                    val count = res.data
                    _deleteProgress.value = DeleteProgress(
                        isDeleting = false,
                        current = count,
                        total = count,
                        currentItemName = "Completed",
                        isCompleted = true
                    )
                    _toastEvent.emit("Successfully deleted $count files/folders.")
                    loadDirectoryContents()
                    loadRepositoryTree(force = true)
                    onDone(true)
                }
                is Resource.Error -> {
                    _deleteProgress.value = DeleteProgress(
                        isDeleting = false,
                        errorMessage = res.message
                    )
                    _toastEvent.emit("Batch delete error: ${res.message}")
                    onDone(false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun dismissDeleteProgress() {
        _deleteProgress.value = DeleteProgress()
    }

    fun uploadSingleFile(uri: Uri, targetFileName: String, commitMessage: String, onDone: (Boolean) -> Unit) {
        val repo = _activeRepo.value ?: return
        val owner = repo.owner?.login ?: _currentUser.value?.login ?: return
        val currentDir = _currentPath.value.trim().removePrefix("/").removeSuffix("/")
        val fullPath = if (currentDir.isEmpty()) targetFileName else "$currentDir/$targetFileName"

        viewModelScope.launch {
            _uploadProgress.value = UploadProgress(
                isUploading = true,
                current = 1,
                total = 1,
                currentFileName = targetFileName,
                isCompleted = false
            )

            when (val readRes = repository.readFileBytesFromUri(uri)) {
                is Resource.Success -> {
                    val bytes = readRes.data
                    val msg = commitMessage.ifBlank { "Upload $targetFileName" }
                    when (val commitRes = repository.createOrUpdateFile(
                        owner = owner,
                        repo = repo.name,
                        path = fullPath,
                        contentBytes = bytes,
                        commitMessage = msg,
                        existingSha = null,
                        branch = _activeBranch.value
                    )) {
                        is Resource.Success -> {
                            _uploadProgress.value = UploadProgress(
                                isUploading = false,
                                current = 1,
                                total = 1,
                                currentFileName = targetFileName,
                                isCompleted = true
                            )
                            _toastEvent.emit("File '$targetFileName' uploaded successfully!")
                            loadDirectoryContents()
                            onDone(true)
                        }
                        is Resource.Error -> {
                            _uploadProgress.value = UploadProgress(
                                isUploading = false,
                                errorMessage = commitRes.message
                            )
                            _toastEvent.emit("Upload failed: ${commitRes.message}")
                            onDone(false)
                        }
                        is Resource.Loading -> {}
                    }
                }
                is Resource.Error -> {
                    _uploadProgress.value = UploadProgress(
                        isUploading = false,
                        errorMessage = readRes.message
                    )
                    _toastEvent.emit("Error reading file: ${readRes.message}")
                    onDone(false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun parseZip(uri: Uri, onParsed: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isFileExplorerLoading.value = true
            when (val res = repository.parseZipFile(uri)) {
                is Resource.Success -> {
                    _extractedZipItems.value = res.data
                    _isFileExplorerLoading.value = false
                    onParsed(true)
                }
                is Resource.Error -> {
                    _isFileExplorerLoading.value = false
                    _toastEvent.emit("ZIP error: ${res.message}")
                    onParsed(false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun uploadExtractedZip(
        items: List<ZipExtractedItem>,
        commitMessage: String,
        onDone: (Boolean) -> Unit
    ) {
        val repo = _activeRepo.value ?: return
        val owner = repo.owner?.login ?: _currentUser.value?.login ?: return
        val currentDir = _currentPath.value.trim().removePrefix("/").removeSuffix("/")

        val filesToUpload = items.filter { !it.isDirectory && it.bytes != null }
        if (filesToUpload.isEmpty()) {
            viewModelScope.launch {
                _toastEvent.emit("No files to upload from ZIP.")
                onDone(false)
            }
            return
        }

        activeUploadJob?.cancel()
        activeUploadJob = viewModelScope.launch {
            _uploadProgress.value = UploadProgress(
                isUploading = true,
                current = 0,
                total = filesToUpload.size,
                currentFileName = "Starting batch upload...",
                isCompleted = false
            )

            var successCount = 0
            for ((index, item) in filesToUpload.withIndex()) {
                val filePath = item.relativePath.trim().removePrefix("/")
                val fullPath = if (currentDir.isEmpty()) filePath else "$currentDir/$filePath"

                _uploadProgress.value = UploadProgress(
                    isUploading = true,
                    current = index + 1,
                    total = filesToUpload.size,
                    currentFileName = item.relativePath,
                    isCompleted = false
                )

                val msg = commitMessage.ifBlank { "Upload ${item.relativePath} from ZIP" }
                val res = repository.createOrUpdateFile(
                    owner = owner,
                    repo = repo.name,
                    path = fullPath,
                    contentBytes = item.bytes!!,
                    commitMessage = msg,
                    existingSha = null,
                    branch = _activeBranch.value
                )

                if (res is Resource.Success) {
                    successCount++
                }
            }

            _uploadProgress.value = UploadProgress(
                isUploading = false,
                current = successCount,
                total = filesToUpload.size,
                currentFileName = "Completed ($successCount/${filesToUpload.size} files)",
                isCompleted = true
            )

            _toastEvent.emit("Uploaded $successCount files to ${repo.name}!")
            loadDirectoryContents()
            onDone(true)
        }
    }

    fun cancelUpload() {
        activeUploadJob?.cancel()
        _uploadProgress.value = UploadProgress(isUploading = false, errorMessage = "Upload cancelled.")
    }

    fun dismissUploadProgress() {
        _uploadProgress.value = UploadProgress()
    }
}
