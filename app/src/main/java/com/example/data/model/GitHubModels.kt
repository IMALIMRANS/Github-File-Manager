package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubUser(
    val login: String = "",
    val id: Long = 0L,
    @Json(name = "avatar_url") val avatarUrl: String = "",
    val name: String? = null,
    val bio: String? = null,
    @Json(name = "public_repos") val publicRepos: Int = 0,
    @Json(name = "total_private_repos") val totalPrivateRepos: Int = 0,
    val email: String? = null,
    @Json(name = "html_url") val htmlUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubRepo(
    val id: Long = 0L,
    val name: String = "",
    @Json(name = "full_name") val fullName: String = "",
    val private: Boolean = false,
    val description: String? = null,
    val fork: Boolean = false,
    @Json(name = "default_branch") val defaultBranch: String = "main",
    @Json(name = "stargazers_count") val stargazersCount: Int = 0,
    @Json(name = "forks_count") val forksCount: Int = 0,
    @Json(name = "updated_at") val updatedAt: String? = null,
    val size: Long = 0L,
    val language: String? = null,
    val owner: GitHubRepoOwner? = null,
    @Json(name = "html_url") val htmlUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubRepoOwner(
    val login: String = "",
    @Json(name = "avatar_url") val avatarUrl: String = "",
    @Json(name = "html_url") val htmlUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubContentItem(
    val name: String = "",
    val path: String = "",
    val sha: String = "",
    val size: Long = 0L,
    val url: String = "",
    @Json(name = "html_url") val htmlUrl: String = "",
    @Json(name = "download_url") val downloadUrl: String? = null,
    val type: String = "file", // "file", "dir", "symlink", "submodule"
    val content: String? = null,
    val encoding: String? = null
) {
    val isDirectory: Boolean get() = type == "dir"
    val isFile: Boolean get() = type == "file"
}

@JsonClass(generateAdapter = true)
data class GitHubFileCommitRequest(
    val message: String,
    val content: String, // base64 encoded content
    val sha: String? = null, // required when updating an existing file
    val branch: String? = null,
    val committer: GitHubCommitter? = null,
    val author: GitHubCommitter? = null
)

@JsonClass(generateAdapter = true)
data class GitHubDeleteFileRequest(
    val message: String,
    val sha: String, // required
    val branch: String? = null,
    val committer: GitHubCommitter? = null
)

@JsonClass(generateAdapter = true)
data class GitHubCommitter(
    val name: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class GitHubCommitResponse(
    val content: GitHubContentItem? = null,
    val commit: GitHubCommitInfo? = null
)

@JsonClass(generateAdapter = true)
data class GitHubCommitInfo(
    val sha: String = "",
    val message: String = "",
    @Json(name = "html_url") val htmlUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubBranch(
    val name: String = "",
    val commit: GitHubBranchCommit? = null,
    val `protected`: Boolean = false
)

@JsonClass(generateAdapter = true)
data class GitHubBranchCommit(
    val sha: String = "",
    val url: String = ""
)

@JsonClass(generateAdapter = true)
data class CreateRepoRequest(
    val name: String,
    val description: String? = null,
    val private: Boolean = false,
    @Json(name = "auto_init") val autoInit: Boolean = true
)

@JsonClass(generateAdapter = true)
data class GitHubBlob(
    val sha: String = "",
    val size: Long = 0L,
    val content: String = "",
    val encoding: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubGitTreeResponse(
    val sha: String = "",
    val url: String = "",
    val tree: List<GitHubTreeEntry> = emptyList(),
    val truncated: Boolean = false
)

@JsonClass(generateAdapter = true)
data class GitHubTreeEntry(
    val path: String = "",
    val mode: String = "",
    val type: String = "", // "tree" (dir) or "blob" (file)
    val sha: String = "",
    val size: Long = 0L,
    val url: String = ""
) {
    val isDirectory: Boolean get() = type == "tree"
    val isFile: Boolean get() = type == "blob"
    val name: String get() = path.substringAfterLast('/')
}

@JsonClass(generateAdapter = true)
data class SavedAccount(
    val user: GitHubUser,
    val token: String,
    val addedAt: Long = System.currentTimeMillis(),
    val committerName: String? = null,
    val committerEmail: String? = null
)
