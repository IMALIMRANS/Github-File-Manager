package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.GitHubUser
import com.example.data.model.SavedAccount
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("github_file_manager_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(GitHubUser::class.java)
    private val accountsListAdapter = moshi.adapter<List<SavedAccount>>(
        Types.newParameterizedType(List::class.java, SavedAccount::class.java)
    )

    companion object {
        private const val KEY_AUTH_TOKEN = "key_github_token"
        private const val KEY_SAVED_USER = "key_saved_user"
        private const val KEY_COMMITTER_NAME = "key_committer_name"
        private const val KEY_COMMITTER_EMAIL = "key_committer_email"
        private const val KEY_PINNED_REPOS = "key_pinned_repos"
        private const val KEY_ACCOUNTS_JSON = "key_accounts_json"
        private const val KEY_ACTIVE_ACCOUNT_ID = "key_active_account_id"
    }

    init {
        migrateLegacyAccountIfNeeded()
    }

    private fun migrateLegacyAccountIfNeeded() {
        val legacyToken = prefs.getString(KEY_AUTH_TOKEN, null)?.trim()
        val legacyUserJson = prefs.getString(KEY_SAVED_USER, null)
        val existingAccountsJson = prefs.getString(KEY_ACCOUNTS_JSON, null)

        if (!legacyToken.isNullOrEmpty() && existingAccountsJson.isNullOrEmpty() && !legacyUserJson.isNullOrEmpty()) {
            try {
                val legacyUser = userAdapter.fromJson(legacyUserJson)
                if (legacyUser != null) {
                    val account = SavedAccount(
                        user = legacyUser,
                        token = legacyToken,
                        addedAt = System.currentTimeMillis(),
                        committerName = prefs.getString(KEY_COMMITTER_NAME, null),
                        committerEmail = prefs.getString(KEY_COMMITTER_EMAIL, null)
                    )
                    saveAccountsList(listOf(account))
                    setActiveAccountId(legacyUser.id)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun getSavedAccounts(): List<SavedAccount> {
        val json = prefs.getString(KEY_ACCOUNTS_JSON, null) ?: return emptyList()
        return try {
            accountsListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveAccountsList(accounts: List<SavedAccount>) {
        val json = accountsListAdapter.toJson(accounts)
        prefs.edit().putString(KEY_ACCOUNTS_JSON, json).apply()
    }

    fun isAccountAlreadyAdded(login: String, id: Long): Boolean {
        val accounts = getSavedAccounts()
        return accounts.any {
            it.user.id == id || it.user.login.equals(login.trim(), ignoreCase = true)
        }
    }

    fun addAccount(account: SavedAccount): Boolean {
        if (isAccountAlreadyAdded(account.user.login, account.user.id)) {
            return false // Duplicate account rejected
        }
        val currentAccounts = getSavedAccounts().toMutableList()
        currentAccounts.add(0, account)
        saveAccountsList(currentAccounts)
        setActiveAccountId(account.user.id)
        // Also keep legacy keys in sync for compatibility
        saveLegacyActiveAccount(account)
        return true
    }

    fun getActiveAccountId(): Long? {
        val id = prefs.getLong(KEY_ACTIVE_ACCOUNT_ID, -1L)
        return if (id != -1L) id else null
    }

    fun setActiveAccountId(userId: Long) {
        prefs.edit().putLong(KEY_ACTIVE_ACCOUNT_ID, userId).apply()
        val account = getSavedAccounts().find { it.user.id == userId }
        if (account != null) {
            saveLegacyActiveAccount(account)
        }
    }

    private fun saveLegacyActiveAccount(account: SavedAccount) {
        prefs.edit()
            .putString(KEY_AUTH_TOKEN, account.token)
            .putString(KEY_SAVED_USER, userAdapter.toJson(account.user))
            .apply()
        if (!account.committerName.isNullOrBlank()) {
            prefs.edit().putString(KEY_COMMITTER_NAME, account.committerName).apply()
        }
        if (!account.committerEmail.isNullOrBlank()) {
            prefs.edit().putString(KEY_COMMITTER_EMAIL, account.committerEmail).apply()
        }
    }

    fun getActiveAccount(): SavedAccount? {
        val accounts = getSavedAccounts()
        if (accounts.isEmpty()) return null
        val activeId = getActiveAccountId()
        return accounts.find { it.user.id == activeId } ?: accounts.firstOrNull()?.also {
            setActiveAccountId(it.user.id)
        }
    }

    fun switchAccount(userId: Long): Boolean {
        val accounts = getSavedAccounts()
        val target = accounts.find { it.user.id == userId } ?: return false
        setActiveAccountId(target.user.id)
        return true
    }

    fun removeAccount(userId: Long): Boolean {
        val currentAccounts = getSavedAccounts().toMutableList()
        val removed = currentAccounts.removeAll { it.user.id == userId }
        if (removed) {
            saveAccountsList(currentAccounts)
            val currentActiveId = getActiveAccountId()
            if (currentActiveId == userId) {
                if (currentAccounts.isNotEmpty()) {
                    val nextAccount = currentAccounts.first()
                    setActiveAccountId(nextAccount.user.id)
                } else {
                    clearActiveAccount()
                }
            }
        }
        return removed
    }

    fun clearActiveAccount() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_SAVED_USER)
            .remove(KEY_ACTIVE_ACCOUNT_ID)
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token.trim()).apply()
    }

    fun getToken(): String? {
        val active = getActiveAccount()
        if (active != null && active.token.isNotBlank()) {
            return active.token
        }
        val legacy = prefs.getString(KEY_AUTH_TOKEN, null)?.trim()
        return if (!legacy.isNullOrEmpty()) legacy else null
    }

    fun hasToken(): Boolean = !getToken().isNullOrEmpty()

    fun clearToken() {
        val activeAccount = getActiveAccount()
        if (activeAccount != null) {
            removeAccount(activeAccount.user.id)
        } else {
            clearActiveAccount()
        }
    }

    fun saveUser(user: GitHubUser) {
        val json = userAdapter.toJson(user)
        prefs.edit().putString(KEY_SAVED_USER, json).apply()
        // Update user inside accounts list if present
        val accounts = getSavedAccounts().toMutableList()
        val idx = accounts.indexOfFirst { it.user.id == user.id }
        if (idx != -1) {
            accounts[idx] = accounts[idx].copy(user = user)
            saveAccountsList(accounts)
        }
    }

    fun getSavedUser(): GitHubUser? {
        val active = getActiveAccount()
        if (active != null) return active.user

        val json = prefs.getString(KEY_SAVED_USER, null) ?: return null
        return try {
            userAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun setCommitterInfo(name: String, email: String) {
        prefs.edit()
            .putString(KEY_COMMITTER_NAME, name)
            .putString(KEY_COMMITTER_EMAIL, email)
            .apply()

        // Also update for current active account
        val activeId = getActiveAccountId()
        if (activeId != null) {
            val accounts = getSavedAccounts().toMutableList()
            val idx = accounts.indexOfFirst { it.user.id == activeId }
            if (idx != -1) {
                accounts[idx] = accounts[idx].copy(committerName = name, committerEmail = email)
                saveAccountsList(accounts)
            }
        }
    }

    fun getCommitterName(): String? = prefs.getString(KEY_COMMITTER_NAME, null)
    fun getCommitterEmail(): String? = prefs.getString(KEY_COMMITTER_EMAIL, null)

    fun getPinnedRepoIds(): Set<String> {
        return prefs.getStringSet(KEY_PINNED_REPOS, emptySet()) ?: emptySet()
    }

    fun togglePinRepo(repoFullName: String) {
        val current = getPinnedRepoIds().toMutableSet()
        if (current.contains(repoFullName)) {
            current.remove(repoFullName)
        } else {
            current.add(repoFullName)
        }
        prefs.edit().putStringSet(KEY_PINNED_REPOS, current).apply()
    }
}
