package com.example.newsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.newsapp.api.ApiClient
import com.example.newsapp.api.Article
import com.example.newsapp.databinding.ActivityViewFeedbackBinding
import kotlinx.coroutines.launch

class ViewFeedbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve extras from the intent.
        val topic = intent.getStringExtra("topic")
        val feedback = intent.getStringExtra("feedback")
        val article = intent.getParcelableExtra<Article>("article")

        // Set the topic, feedback, and rating.
        binding.topicField.setText(topic)
        binding.feedbackField.setText(feedback)
        article?.let {
            binding.ratingBar.rating = it.rating
            binding.ratingValue.text = "${it.rating}/5.0"
        }

        // Back button: finish this activity.
        binding.backButton.setOnClickListener { finish() }

        // Edit button: Launch ArticleWriteActivity for editing.
        binding.editButton.setOnClickListener {
            article?.let {
                val intent = Intent(this, ArticleWriteActivity::class.java)
                intent.putExtra("article", it)
                startActivity(intent)
            }
        }

        // Delete button: call the delete API and redirect to HomeActivity.
        binding.deleteButton.setOnClickListener {
            article?.let { deleteArticle(it) } ?: run {
                Toast.makeText(this, "No article to delete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteArticle(article: Article) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.deleteArticle(article._id)
                if (response.isSuccessful) {
                    Toast.makeText(this@ViewFeedbackActivity, "Article deleted", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ViewFeedbackActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@ViewFeedbackActivity, "Failed to delete article", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ViewFeedbackActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}