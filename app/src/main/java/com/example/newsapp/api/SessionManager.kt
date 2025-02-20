package com.example.newsapp.api

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "fullName"
        private const val KEY_USER_TYPE = "userType"
    }

    // Save login details when the user logs in successfully.
    fun saveSession(username: String, fullName: String, userType: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_USER_TYPE, userType)
            apply()
        }
    }

    // Check if the user is logged in.
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)
    fun getUserType(): String? = prefs.getString(KEY_USER_TYPE, null)

    // Clear session details when the user logs out.
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}