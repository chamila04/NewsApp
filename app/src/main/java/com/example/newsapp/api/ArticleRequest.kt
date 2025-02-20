package com.example.newsapp.api

data class ArticleRequest(
    val username: String,
    val title: String,
    val tags: List<String>,
    val img: String,       // Base64 encoded image string
    val article: String
)