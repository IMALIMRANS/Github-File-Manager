package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.data.api.GitHubApiClient
import com.example.data.api.GitHubApiService
import com.example.data.model.CreateRepoRequest
import com.example.data.model.GitHubBranch
import com.example.data.model.GitHubCommitResponse
import com.example.data.model.GitHubCommitter
import com.example.data.model.GitHubContentItem
import com.example.data.model.GitHubDeleteFileRequest
import com.example.data.model.GitHubFileCommitRequest
import com.example.data.model.GitHubRepo
import com.example.data.model.GitHubUser
import com.example.data.storage.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val statusCode: Int? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

data class ZipExtractedItem(
    val relativePath: String,
    val size: Long,
    val isDirectory: Boolean,
    val bytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ZipExtractedItem
        return relativePath == other.relativePath
    }

    override fun hashCode(): Int = relativePath.hashCode()
}

class GitHubRepository(
    private val context: Context,
    private val tokenManager: TokenManager = TokenManager(context),
    private val api: GitHubApiService = GitHubApiClient.service
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listAdapter = moshi.adapter<List<GitHubContentItem>>(
        Types.newParameterizedType(List::class.java, GitHubContentItem::class.java)
    )
    private val singleAdapter = moshi.adapter(GitHubContentItem::class.java)

    private fun getAuthHeader(): String {
        val token = tokenManager.getToken() ?: ""
        return if (token.startsWith("ghp_") || token.startsWith("github_pat_") || token.startsWith("gho_") || token.startsWith("Bearer ") || token.startsWith("token ")) {
            if (token.startsWith("Bearer ") || token.startsWith("token ")) token else "Bearer $token"
        } else {
            "Bearer $token"
        }
    }

    private fun getAuthHeaderForToken(token: String): String {
        val cleanToken = token.trim()
        return if (cleanToken.startsWith("ghp_") || cleanToken.startsWith("github_pat_") || cleanToken.startsWith("gho_") || cleanToken.startsWith("Bearer ") || cleanToken.startsWith("token ")) {
            if (cleanToken.startsWith("Bearer ") || cleanToken.startsWith("token ")) cleanToken else "Bearer $cleanToken"
        } else {
            "Bearer $cleanToken"
        }
    }

    suspend fun validateAndSaveToken(token: String, preventDuplicate: Boolean = true): Resource<GitHubUser> = withContext(Dispatchers.IO) {
        try {
            val auth = getAuthHeaderForToken(token)
            val response = api.getAuthenticatedUser(auth)
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!

                if (preventDuplicate && tokenManager.isAccountAlreadyAdded(user.login, user.id)) {
                    return@withContext Resource.Error(
                        "This GitHub account (@${user.login}) is already added. You cannot add the same account multiple times.",
                        409
                    )
                }

                val committerName = user.name ?: user.login
                val committerEmail = user.email ?: "${user.login}@users.noreply.github.com"
                val newAccount = com.example.data.model.SavedAccount(
                    user = user,
                    token = token.trim(),
                    addedAt = System.currentTimeMillis(),
                    committerName = committerName,
                    committerEmail = committerEmail
                )

                tokenManager.addAccount(newAccount)
                Resource.Success(user)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Invalid Personal Access Token. Please verify your token permissions."
                    403 -> "API Rate limit exceeded or insufficient token scopes."
                    else -> "Authentication failed (${response.code()}): ${response.message()}"
                }
                Resource.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error. Please check your connection.")
        }
    }

    suspend fun fetchCurrentUser(): Resource<GitHubUser> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAuthenticatedUser(getAuthHeader())
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                tokenManager.saveUser(user)
                Resource.Success(user)
            } else {
                Resource.Error("Failed to fetch user (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun fetchRepositories(): Resource<List<GitHubRepo>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserRepositories(
                authHeader = getAuthHeader(),
                visibility = "all",
                affiliation = "owner,collaborator,organization_member",
                sort = "updated",
                direction = "desc",
                perPage = 100
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load repositories (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to connect to GitHub")
        }
    }

    suspend fun createRepository(name: String, description: String?, isPrivate: Boolean): Resource<GitHubRepo> = withContext(Dispatchers.IO) {
        try {
            val req = CreateRepoRequest(name = name, description = description, private = isPrivate, autoInit = true)
            val response = api.createRepository(getAuthHeader(), req)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to create repository (${response.code()}): ${response.message()}", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error creating repository")
        }
    }

    suspend fun fetchBranches(owner: String, repo: String): Resource<List<GitHubBranch>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBranches(getAuthHeader(), owner, repo)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load branches (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error fetching branches")
        }
    }

    suspend fun fetchRecursiveGitTree(
        owner: String,
        repo: String,
        treeShaOrBranch: String = "main"
    ): Resource<com.example.data.model.GitHubGitTreeResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGitTree(getAuthHeader(), owner, repo, treeShaOrBranch, recursive = 1)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load repository tree (${response.code()}): ${response.message()}", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error fetching recursive tree")
        }
    }

    suspend fun fetchDirectoryContents(
        owner: String,
        repo: String,
        path: String = "",
        branch: String? = null
    ): Resource<List<GitHubContentItem>> = withContext(Dispatchers.IO) {
        try {
            val cleanPath = path.trim().removePrefix("/").removeSuffix("/")
            val response = if (cleanPath.isEmpty()) {
                api.getRootContents(getAuthHeader(), owner, repo, branch)
            } else {
                api.getContents(getAuthHeader(), owner, repo, cleanPath, branch)
            }

            if (response.isSuccessful && response.body() != null) {
                val rawString = response.body()!!.string()
                // Check if response is an array (directory) or a single object (file)
                if (rawString.trimStart().startsWith("[")) {
                    val list = listAdapter.fromJson(rawString) ?: emptyList()
                    val sortedList = list.sortedWith(compareBy<GitHubContentItem> { !it.isDirectory }.thenBy { it.name.lowercase() })
                    Resource.Success(sortedList)
                } else {
                    val single = singleAdapter.fromJson(rawString)
                    if (single != null) {
                        Resource.Success(listOf(single))
                    } else {
                        Resource.Success(emptyList())
                    }
                }
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Directory or repository not found or repository is empty."
                    401 -> "Unauthorized. Please check token permissions."
                    else -> "Failed to load directory (${response.code()})"
                }
                Resource.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load contents")
        }
    }

    suspend fun fetchFileContent(
        owner: String,
        repo: String,
        path: String,
        branch: String? = null
    ): Resource<GitHubContentItem> = withContext(Dispatchers.IO) {
        try {
            val cleanPath = path.trim().removePrefix("/")
            val response = api.getContents(getAuthHeader(), owner, repo, cleanPath, branch)
            if (response.isSuccessful && response.body() != null) {
                val rawString = response.body()!!.string()
                val item = singleAdapter.fromJson(rawString)
                if (item != null) {
                    Resource.Success(item)
                } else {
                    Resource.Error("Unable to parse file response")
                }
            } else {
                Resource.Error("Failed to fetch file (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error loading file")
        }
    }

    suspend fun fetchRawText(url: String): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.downloadRaw(getAuthHeader(), url)
            if (response.isSuccessful && response.body() != null) {
                val text = response.body()!!.string()
                Resource.Success(text)
            } else {
                Resource.Error("Failed to download raw file (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error downloading raw text")
        }
    }

    fun decodeBase64Content(item: GitHubContentItem): String {
        return try {
            val raw = item.content ?: ""
            val cleanBase64 = raw.replace("\n", "").replace("\r", "")
            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            String(bytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "Unable to decode file content as UTF-8 text."
        }
    }

    suspend fun fetchLatestFileSha(
        owner: String,
        repo: String,
        path: String,
        branch: String? = null
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val cleanPath = path.trim().removePrefix("/")
            val response = api.getContents(getAuthHeader(), owner, repo, cleanPath, branch)
            if (response.isSuccessful && response.body() != null) {
                val rawString = response.body()!!.string()
                val item = singleAdapter.fromJson(rawString)
                if (item != null && item.sha.isNotBlank()) {
                    Resource.Success(item.sha)
                } else {
                    Resource.Error("Could not retrieve file SHA from GitHub repository metadata")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                Resource.Error("Could not find file on GitHub (${response.code()}): ${response.message()} $errorBody", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to retrieve latest file SHA")
        }
    }

    /**
     * Updates an existing file using GitHub Contents API:
     * 1. First fetches the latest file SHA from GitHub.
     * 2. Encodes the new content in Base64 (NO_WRAP).
     * 3. Sends a PUT request with the updated Base64-encoded content, commit message, branch, and latest SHA.
     * 4. Returns Resource.Success only when the API returns a successful response.
     */
    suspend fun updateFile(
        owner: String,
        repo: String,
        path: String,
        contentBytes: ByteArray,
        commitMessage: String,
        branch: String? = null
    ): Resource<GitHubCommitResponse> = withContext(Dispatchers.IO) {
        try {
            val cleanPath = path.trim().removePrefix("/")

            // 1. Fetch latest SHA from GitHub Contents API
            val latestSha = when (val shaRes = fetchLatestFileSha(owner, repo, cleanPath, branch)) {
                is Resource.Success -> shaRes.data
                is Resource.Error -> return@withContext Resource.Error(shaRes.message, shaRes.statusCode)
                is Resource.Loading -> return@withContext Resource.Error("Fetching SHA...")
            }

            // 2. Base64-encode updated content
            val base64Content = Base64.encodeToString(contentBytes, Base64.NO_WRAP)
            val committerName = tokenManager.getCommitterName() ?: "GitHub File Manager"
            val committerEmail = tokenManager.getCommitterEmail() ?: "filemanager@example.com"
            val committer = GitHubCommitter(name = committerName, email = committerEmail)

            // 3. Build PUT commit request with the latest SHA
            val request = GitHubFileCommitRequest(
                message = commitMessage,
                content = base64Content,
                sha = latestSha,
                branch = branch,
                committer = committer,
                author = committer
            )

            // 4. Send PUT request
            val response = api.createOrUpdateFile(getAuthHeader(), owner, repo, cleanPath, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                Resource.Error("File update failed (${response.code()}): ${response.message()} $errorBody", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error updating file in repository")
        }
    }

    suspend fun createOrUpdateFile(
        owner: String,
        repo: String,
        path: String,
        contentBytes: ByteArray,
        commitMessage: String,
        existingSha: String? = null,
        branch: String? = null
    ): Resource<GitHubCommitResponse> = withContext(Dispatchers.IO) {
        try {
            val cleanPath = path.trim().removePrefix("/")
            val base64Content = Base64.encodeToString(contentBytes, Base64.NO_WRAP)
            val committerName = tokenManager.getCommitterName() ?: "GitHub File Manager"
            val committerEmail = tokenManager.getCommitterEmail() ?: "filemanager@example.com"
            val committer = GitHubCommitter(name = committerName, email = committerEmail)

            // If existingSha is not provided, attempt to check if the file already exists to obtain sha
            var finalSha = existingSha
            if (finalSha == null) {
                try {
                    val checkRes = api.getContents(getAuthHeader(), owner, repo, cleanPath, branch)
                    if (checkRes.isSuccessful && checkRes.body() != null) {
                        val body = checkRes.body()!!.string()
                        val existingItem = singleAdapter.fromJson(body)
                        finalSha = existingItem?.sha
                    }
                } catch (_: Exception) {
                    // Ignore 404 - new file
                }
            }

            val request = GitHubFileCommitRequest(
                message = commitMessage,
                content = base64Content,
                sha = finalSha,
                branch = branch,
                committer = committer,
                author = committer
            )

            val response = api.createOrUpdateFile(getAuthHeader(), owner, repo, cleanPath, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                Resource.Error("Commit failed (${response.code()}): ${response.message()} $errorBody", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error committing file to repository")
        }
    }

    suspend fun createFolder(
        owner: String,
        repo: String,
        folderPath: String,
        branch: String? = null
    ): Resource<GitHubCommitResponse> = withContext(Dispatchers.IO) {
        val cleanFolder = folderPath.trim().removePrefix("/").removeSuffix("/")
        val gitKeepPath = "$cleanFolder/.gitkeep"
        val emptyBytes = "".toByteArray(StandardCharsets.UTF_8)
        createOrUpdateFile(
            owner = owner,
            repo = repo,
            path = gitKeepPath,
            contentBytes = emptyBytes,
            commitMessage = "Create directory $cleanFolder",
            existingSha = null,
            branch = branch
        )
    }

    suspend fun deleteFile(
        owner: String,
        repo: String,
        path: String,
        sha: String,
        commitMessage: String,
        branch: String? = null
    ): Resource<GitHubCommitResponse> = withContext(Dispatchers.IO) {
        try {
            val cleanPath = path.trim().removePrefix("/")
            val committerName = tokenManager.getCommitterName() ?: "GitHub File Manager"
            val committerEmail = tokenManager.getCommitterEmail() ?: "filemanager@example.com"
            val committer = GitHubCommitter(name = committerName, email = committerEmail)

            val req = GitHubDeleteFileRequest(
                message = commitMessage,
                sha = sha,
                branch = branch,
                committer = committer
            )

            val response = api.deleteFile(getAuthHeader(), owner, repo, cleanPath, req)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Delete failed (${response.code()}): ${response.message()}", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error deleting file")
        }
    }

    suspend fun resolveFilesInDirectory(
        owner: String,
        repo: String,
        folderPath: String,
        branch: String? = null
    ): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val cleanFolder = folderPath.trim().removePrefix("/").removeSuffix("/")
        val result = mutableListOf<Pair<String, String>>()

        // Try Git Tree API first for fastest recursive resolution
        val treeBranch = branch ?: "HEAD"
        try {
            val treeRes = api.getGitTree(getAuthHeader(), owner, repo, treeBranch, recursive = 1)
            if (treeRes.isSuccessful && treeRes.body() != null) {
                val entries = treeRes.body()!!.tree
                val prefix = "$cleanFolder/"
                val matches = entries.filter { it.isFile && (it.path == cleanFolder || it.path.startsWith(prefix)) }
                if (matches.isNotEmpty()) {
                    return@withContext matches.map { Pair(it.path, it.sha) }
                }
            }
        } catch (_: Exception) {
        }

        // Fallback: Recursive directory crawl via Contents API
        suspend fun crawl(subPath: String) {
            when (val contentsRes = fetchDirectoryContents(owner, repo, subPath, branch)) {
                is Resource.Success -> {
                    for (item in contentsRes.data) {
                        if (item.isDirectory) {
                            crawl(item.path)
                        } else if (item.isFile) {
                            result.add(Pair(item.path, item.sha))
                        }
                    }
                }
                else -> {}
            }
        }

        crawl(cleanFolder)
        result
    }

    suspend fun deleteFolderRecursively(
        owner: String,
        repo: String,
        folderPath: String,
        commitMessage: String,
        branch: String? = null,
        onProgress: (current: Int, total: Int, currentFile: String) -> Unit = { _, _, _ -> }
    ): Resource<Int> = withContext(Dispatchers.IO) {
        val cleanFolder = folderPath.trim().removePrefix("/").removeSuffix("/")
        val files = resolveFilesInDirectory(owner, repo, cleanFolder, branch)

        if (files.isEmpty()) {
            // Attempt to delete .gitkeep if it exists
            val gitKeepPath = "$cleanFolder/.gitkeep"
            return@withContext when (val shaRes = fetchLatestFileSha(owner, repo, gitKeepPath, branch)) {
                is Resource.Success -> {
                    when (val del = deleteFile(owner, repo, gitKeepPath, shaRes.data, commitMessage, branch)) {
                        is Resource.Success -> Resource.Success(1)
                        is Resource.Error -> Resource.Error(del.message)
                        is Resource.Loading -> Resource.Error("Deleting...")
                    }
                }
                else -> Resource.Success(0)
            }
        }

        var deletedCount = 0
        val total = files.size
        for ((index, file) in files.withIndex()) {
            val (path, originalSha) = file
            onProgress(index + 1, total, path)

            var shaToUse = originalSha
            if (shaToUse.isBlank()) {
                val latestShaRes = fetchLatestFileSha(owner, repo, path, branch)
                if (latestShaRes is Resource.Success) {
                    shaToUse = latestShaRes.data
                }
            }

            val msg = commitMessage.ifBlank { "Delete $cleanFolder (file ${index + 1}/$total)" }
            val delRes = deleteFile(owner, repo, path, shaToUse, msg, branch)
            if (delRes is Resource.Success) {
                deletedCount++
            } else {
                // Try once more with refreshed SHA
                val latestShaRes = fetchLatestFileSha(owner, repo, path, branch)
                if (latestShaRes is Resource.Success) {
                    val retry = deleteFile(owner, repo, path, latestShaRes.data, msg, branch)
                    if (retry is Resource.Success) {
                        deletedCount++
                    }
                }
            }
        }

        Resource.Success(deletedCount)
    }

    suspend fun deleteMultipleItems(
        owner: String,
        repo: String,
        items: List<GitHubContentItem>,
        commitMessage: String,
        branch: String? = null,
        onProgress: (current: Int, total: Int, currentFile: String) -> Unit = { _, _, _ -> }
    ): Resource<Int> = withContext(Dispatchers.IO) {
        val filesToDelete = mutableListOf<Pair<String, String>>()

        for (item in items) {
            if (item.isDirectory) {
                val subFiles = resolveFilesInDirectory(owner, repo, item.path, branch)
                filesToDelete.addAll(subFiles)
            } else {
                filesToDelete.add(Pair(item.path, item.sha))
            }
        }

        val distinctFiles = filesToDelete.distinctBy { it.first }
        if (distinctFiles.isEmpty()) {
            return@withContext Resource.Success(0)
        }

        var deletedCount = 0
        val total = distinctFiles.size
        for ((index, file) in distinctFiles.withIndex()) {
            val (path, originalSha) = file
            onProgress(index + 1, total, path)

            var shaToUse = originalSha
            if (shaToUse.isBlank()) {
                val latestShaRes = fetchLatestFileSha(owner, repo, path, branch)
                if (latestShaRes is Resource.Success) {
                    shaToUse = latestShaRes.data
                }
            }

            val msg = commitMessage.ifBlank { "Delete ${path.substringAfterLast('/')}" }
            val delRes = deleteFile(owner, repo, path, shaToUse, msg, branch)
            if (delRes is Resource.Success) {
                deletedCount++
            } else {
                val latestShaRes = fetchLatestFileSha(owner, repo, path, branch)
                if (latestShaRes is Resource.Success) {
                    val retry = deleteFile(owner, repo, path, latestShaRes.data, msg, branch)
                    if (retry is Resource.Success) {
                        deletedCount++
                    }
                }
            }
        }

        Resource.Success(deletedCount)
    }

    // ZIP Extraction helper
    suspend fun parseZipFile(uri: Uri): Resource<List<ZipExtractedItem>> = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Resource.Error("Cannot open selected ZIP file.")

            val zipStream = ZipInputStream(inputStream)
            val items = mutableListOf<ZipExtractedItem>()
            var entry: ZipEntry? = zipStream.nextEntry

            while (entry != null) {
                val entryName = entry.name
                // Skip Mac OS __MACOSX metadata or .DS_Store
                if (!entryName.startsWith("__MACOSX") && !entryName.endsWith(".DS_Store")) {
                    if (entry.isDirectory) {
                        items.add(
                            ZipExtractedItem(
                                relativePath = entryName.removeSuffix("/"),
                                size = 0,
                                isDirectory = true,
                                bytes = null
                            )
                        )
                    } else {
                        val buffer = ByteArrayOutputStream()
                        val data = ByteArray(4096)
                        var count: Int
                        while (zipStream.read(data, 0, data.size).also { count = it } != -1) {
                            buffer.write(data, 0, count)
                        }
                        val bytes = buffer.toByteArray()
                        items.add(
                            ZipExtractedItem(
                                relativePath = entryName,
                                size = bytes.size.toLong(),
                                isDirectory = false,
                                bytes = bytes
                            )
                        )
                    }
                }
                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }
            zipStream.close()
            inputStream.close()

            if (items.isEmpty()) {
                Resource.Error("The selected ZIP file is empty or contains only unsupported metadata.")
            } else {
                Resource.Success(items)
            }
        } catch (e: Exception) {
            Resource.Error("Failed to extract ZIP: ${e.localizedMessage}")
        }
    }

    // Read byte array from URI
    suspend fun readFileBytesFromUri(uri: Uri): Resource<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Resource.Error("Unable to open selected file.")
            val bytes = inputStream.use { it.readBytes() }
            Resource.Success(bytes)
        } catch (e: Exception) {
            Resource.Error("Error reading file: ${e.localizedMessage}")
        }
    }
}
