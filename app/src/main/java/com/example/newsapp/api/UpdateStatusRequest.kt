package com.example.newsapp.api

data class UpdateStatusRequest(
    val status: String,
    val feedback: String? = null,  // feedback is optional
    val rating: Float = 0f
)