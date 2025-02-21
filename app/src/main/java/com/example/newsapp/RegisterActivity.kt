package com.example.newsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.api.ApiClient
import com.example.newsapp.api.RegisterRequest
import com.example.newsapp.api.RegisterResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val firstNameEdit = findViewById<EditText>(R.id.firstNameInput)
        val lastNameEdit = findViewById<EditText>(R.id.lastNameInput)
        val usernameEdit = findViewById<EditText>(R.id.usernameInput)
        val emailEdit = findViewById<EditText>(R.id.emailInput)
        val passwordEdit = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordEdit = findViewById<EditText>(R.id.confirmPasswordInput)
        val typeRadioGroup = findViewById<RadioGroup>(R.id.roleRadioGroup)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginLink = findViewById<TextView>(R.id.loginLink)

        // Redirect to LoginActivity
        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Handle registration
        registerButton.setOnClickListener {
            val firstName = firstNameEdit.text.toString()
            val lastName = lastNameEdit.text.toString()
            val username = usernameEdit.text.toString()
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()
            val confirmPassword = confirmPasswordEdit.text.toString()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && username.isNotEmpty() &&
                email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {

                // Check if passwords match
                if (password != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    passwordEdit.text.clear()
                    confirmPasswordEdit.text.clear()
                    return@setOnClickListener
                }

                // Get selected user type
                val selectedTypeId = typeRadioGroup.checkedRadioButtonId
                val selectedType = when (selectedTypeId) {
                    R.id.reporterRadio -> "reporter"
                    R.id.editorRadio -> "editor"
                    else -> null
                }

                if (selectedType != null) {
                    registerUser(firstName, lastName, username, email, selectedType, password)
                } else {
                    Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        type: String,
        password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.registerUser(
                    RegisterRequest(firstName, lastName, username, email, type, password)
                )
                withContext(Dispatchers.Main) {
                    handleRegisterResponse(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleRegisterResponse(response: RegisterResult) {
        if (response.success) {
            Toast.makeText(this, "Success: ${response.message}", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Error: ${response.message}", Toast.LENGTH_LONG).show()
        }
    }
}