package com.example.newsapp.api

data class LoginResult(
    val success: Boolean,
    val message: String,
    val token: String,
    val user: User
)
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val type: String,
    val status: String?
)