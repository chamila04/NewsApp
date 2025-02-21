package com.example.newsapp.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class ArticleResult(
    val count: Int,
    val message: String? = null,
    val articles: List<Article>
)

@Parcelize
data class Article(
    val feedback:String? = null,
    val _id: String,
    val username: String,
    val title: String,
    val tags: List<String>,
    val img: String, // Base64 encoded image
    val article: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String? = null,  // Made nullable with a default value
    val rating: Float = 0f
): Parcelable