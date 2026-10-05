package com.example.data.api

import com.example.data.model.CreateRepoRequest
import com.example.data.model.GitHubBranch
import com.example.data.model.GitHubCommitResponse
import com.example.data.model.GitHubDeleteFileRequest
import com.example.data.model.GitHubFileCommitRequest
import com.example.data.model.GitHubRepo
import com.example.data.model.GitHubUser
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface GitHubApiService {

    @GET("user")
    suspend fun getAuthenticatedUser(
        @Header("Authorization") authHeader: String
    ): Response<GitHubUser>

    @GET("user/repos")
    suspend fun getUserRepositories(
        @Header("Authorization") authHeader: String,
        @Query("visibility") visibility: String? = "all",
        @Query("affiliation") affiliation: String? = "owner,collaborator,organization_member",
        @Query("sort") sort: String? = "updated",
        @Query("direction") direction: String? = "desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1
    ): Response<List<GitHubRepo>>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getContents(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Query("ref") ref: String? = null
    ): Response<ResponseBody>

    @GET("repos/{owner}/{repo}/contents")
    suspend fun getRootContents(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("ref") ref: String? = null
    ): Response<ResponseBody>

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun createOrUpdateFile(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Body request: GitHubFileCommitRequest
    ): Response<GitHubCommitResponse>

    @HTTP(method = "DELETE", path = "repos/{owner}/{repo}/contents/{path}", hasBody = true)
    suspend fun deleteFile(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Body request: GitHubDeleteFileRequest
    ): Response<GitHubCommitResponse>

    @GET("repos/{owner}/{repo}/branches")
    suspend fun getBranches(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 100
    ): Response<List<GitHubBranch>>

    @POST("user/repos")
    suspend fun createRepository(
        @Header("Authorization") authHeader: String,
        @Body request: CreateRepoRequest
    ): Response<GitHubRepo>

    @GET("repos/{owner}/{repo}/git/trees/{tree_sha}")
    suspend fun getGitTree(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("tree_sha") treeSha: String,
        @Query("recursive") recursive: Int? = 1
    ): Response<com.example.data.model.GitHubGitTreeResponse>

    @GET
    suspend fun downloadRaw(
        @Header("Authorization") authHeader: String,
        @Url url: String
    ): Response<ResponseBody>
}

object GitHubApiClient {
    private const val BASE_URL = "https://api.github.com/"

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("User-Agent", "GitHubFileManager-Android")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val service: GitHubApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GitHubApiService::class.java)
    }
}
