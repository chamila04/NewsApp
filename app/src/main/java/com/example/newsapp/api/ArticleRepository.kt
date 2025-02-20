package com.example.newsapp.api

object ArticleRepository {
    // In-memory storage for Articles.
    private val articles = mutableMapOf<String, Article>()

    fun addArticle(article: Article) {
        articles[article._id] = article
    }

    fun getArticle(id: String): Article? {
        return articles[id]
    }
}