package com.example.newsapp.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    // Register Users
    @POST("/api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): RegisterResult

    // Login Users
    @POST("/api/auth/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): LoginResult

    // Get All Pending Articles for Editor
    @GET("/api/articles/pending")
    suspend fun getArticles(): Response<ArticleResult>

    // Update Article Status using PATCH
    @PATCH("/api/articles/{id}/status")
    suspend fun updateArticleStatus(
        @Path("id") articleId: String,
        @Body request: UpdateStatusRequest
    ): Response<UpdateStatusResponse>

    // Get all articles for a specific reporter
    @GET("/api/articles/user/{username}")
    suspend fun getReporterAllArticles(
        @Path("username") username: String
    ): Response<ArticleResult>

    // Get accepted articles for a specific reporter
    @GET("/api/articles/accepted/user/{username}")
    suspend fun getReporterAcceptArticles(
        @Path("username") username: String
    ): Response<ArticleResult>

    // Get pending articles for a specific reporter
    @GET("/api/articles/pending/user/{username}")
    suspend fun getReporterPendingArticles(
        @Path("username") username: String
    ): Response<ArticleResult>

    // Get rejected articles for a specific reporter
    @GET("/api/articles/rejected/user/{username}")
    suspend fun getReporterRejectArticles(
        @Path("username") username: String
    ): Response<ArticleResult>

    // Create an Article
    @POST("/api/articles/create")
    suspend fun createArticle(
        @Body request: ArticleRequest
    ): Response<ArticleResult>

    // Delete an Article by ID
    @DELETE("/api/articles/{id}")
    suspend fun deleteArticle(
        @Path("id") articleId: String
    ): Response<ArticleResult>

    // Update an Article by ID
    @PUT("/api/articles/{id}")
    suspend fun updateArticle(
        @Path("id") articleId: String,
        @Body request: ArticleRequest
    ): Response<ArticleResult>

    // Get accepted articles by tag
    @GET("/api/articles/tag/{tag}")
    suspend fun getArticlesByTag(
        @Path("tag") tag: String
    ): Response<ArticleResult>

    // Get All Accepted Articles
    @GET("/api/articles/all")
    suspend fun getAllArticles(): Response<ArticleResult>

    // Search articles topic
    @GET("/api/articles/search/{query}")
    suspend fun searchArticles(@Path("query") query: String): Response<ArticleResult>
}