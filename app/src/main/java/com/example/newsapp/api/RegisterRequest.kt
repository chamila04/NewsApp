package com.example.newsapp.api

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val type: String,
    val password: String
)