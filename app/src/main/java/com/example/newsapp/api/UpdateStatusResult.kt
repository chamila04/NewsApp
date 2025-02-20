package com.example.newsapp.api

data class UpdateStatusResponse(
    val message: String,
    val article: ArticleStatus
)

data class ArticleStatus(
    val _id: String,
    val title: String,
    val status: String,
    val feedback: String?
)