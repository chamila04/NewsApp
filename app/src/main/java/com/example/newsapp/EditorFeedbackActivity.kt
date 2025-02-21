package com.example.newsapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.newsapp.api.ApiClient
import com.example.newsapp.api.UpdateStatusRequest
import com.example.newsapp.databinding.ActivityEditorFeedbackBinding
import kotlinx.coroutines.launch

class EditorFeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the article ID and rating passed from EditorReviewActivity.
        val articleId = intent.getStringExtra("articleId")
        val passedRating = intent.getFloatExtra("currentRating", 0f)

        if (articleId == null) {
            Toast.makeText(this, "Article ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set the rating bar with the passed rating and make it non-interactive.
        binding.ratingBar.rating = passedRating
        binding.ratingValue.text = "$passedRating/5.0"
        binding.ratingBar.setIsIndicator(true)

        // Send button: update article status to "reject" with feedback and the passed rating.
        binding.sendButton.setOnClickListener {
            val feedbackText = binding.feedbackInput.text.toString().trim()
            if (feedbackText.isEmpty()) {
                Toast.makeText(this, "Feedback is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                try {
                    val request = UpdateStatusRequest(
                        status = "reject",
                        feedback = feedbackText,
                        rating = passedRating
                    )
                    val response = ApiClient.apiService.updateArticleStatus(articleId, request)
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EditorFeedbackActivity,
                            "Article rejected successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()  // Return to the previous screen.
                    } else {
                        Toast.makeText(
                            this@EditorFeedbackActivity,
                            "Failed to update status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@EditorFeedbackActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Cancel button: close the feedback screen and return to EditorReviewActivity.
        binding.cancelButton.setOnClickListener {
            finish()
        }

        // Back button to close the activity.
        binding.backButton.setOnClickListener {
            finish()
        }
    }
}