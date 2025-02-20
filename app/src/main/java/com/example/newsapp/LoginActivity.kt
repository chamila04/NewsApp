package com.example.newsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.api.ApiClient
import com.example.newsapp.api.LoginRequest
import com.example.newsapp.api.SessionManager
import com.example.newsapp.api.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var btnLogin: Button
    private lateinit var registerLink: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // If already logged in, redirect to HomeActivity immediately.
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        usernameEditText = findViewById(R.id.usernameInput)
        passwordEditText = findViewById(R.id.passwordInput)
        btnLogin = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.registerLink)

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Perform the login API call using coroutines.
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val loginResult = ApiClient.apiService.loginUser(LoginRequest(username, password))
                    withContext(Dispatchers.Main) {
                        if (loginResult.success) {
                            // Build full name from first and last names.
                            val fullName = "${loginResult.user.firstName} ${loginResult.user.lastName}"

                            // Save session details persistently.
                            sessionManager.saveSession(loginResult.user.username, fullName, loginResult.user.type)

                            // Start HomeActivity.
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java).apply {
                                putExtra("userType", loginResult.user.type)
                                putExtra("username", loginResult.user.username)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, loginResult.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Login error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}