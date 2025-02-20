package com.example.newsapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Build
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.api.ApiClient
import com.example.newsapp.api.Article
import com.example.newsapp.api.TagsAdapter
import com.example.newsapp.api.UpdateStatusRequest
import com.example.newsapp.databinding.ActivityEditorReviewBinding
import kotlinx.coroutines.launch

class EditorReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the article passed via intent.
        val article: Article? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("article", Article::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("article")
        }

        if (article == null) {
            Toast.makeText(this, "Article data not available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set article details.
        binding.articleTopic.text = article.title
        binding.writerName.text = article.username
        binding.articleContent.text = article.article

        // Decode and display the Base64 image.
        try {
            var base64String = article.img
            if (base64String.startsWith("data:")) {
                base64String = base64String.substringAfter(",")
            }
            Log.d("EditorReviewActivity", "Base64 string length: ${base64String.length}")
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            if (bitmap != null) {
                binding.articleImage.setImageBitmap(bitmap)
            } else {
                Log.e("EditorReviewActivity", "Bitmap is null after decoding.")
                binding.articleImage.setImageResource(R.drawable.error_img)
            }
        } catch (e: Exception) {
            Log.e("EditorReviewActivity", "Error decoding image: ${e.message}")
            binding.articleImage.setImageResource(R.drawable.error_img)
        }

        // Set up the tags RecyclerView.
        val tagsAdapter = TagsAdapter(article.tags)
        binding.tagsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.tagsRecyclerView.adapter = tagsAdapter

        // Back button to close the activity.
        binding.backButton.setOnClickListener {
            finish()
        }

        // Accept button: update article status to "accept"
        binding.acceptButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val request = UpdateStatusRequest(status = "accept", feedback = "")
                    val response = ApiClient.apiService.updateArticleStatus(article._id, request)
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EditorReviewActivity,
                            "Article accepted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                        // Optionally finish or refresh the UI here.
                    } else {
                        Toast.makeText(
                            this@EditorReviewActivity,
                            "Failed to update status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@EditorReviewActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Reject button: open EditorFeedbackActivity and pass the article ID.
        binding.rejectButton.setOnClickListener {
            val intent = Intent(this, EditorFeedbackActivity::class.java)
            intent.putExtra("articleId", article._id)
            startActivity(intent)
        }
    }
}