package com.example.newsapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.R
import com.example.newsapp.api.Article
import com.example.newsapp.api.ArticleRepository
import com.example.newsapp.api.TagsAdapter
import com.example.newsapp.api.UserSession
import com.example.newsapp.databinding.ActivityViewArticleBinding

class ViewArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewArticleBinding

    companion object {
        private const val EXTRA_ARTICLE_ID = "extra_article_id"

        // Instead of passing the full Article, we add it to the repository
        // and pass only its ID.
        fun newIntent(context: Context, article: Article): Intent {
            ArticleRepository.addArticle(article)
            return Intent(context, ViewArticleActivity::class.java).apply {
                putExtra(EXTRA_ARTICLE_ID, article._id)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the Article from the repository using its ID.
        val article = getArticleFromIntent()
        if (article == null) {
            Toast.makeText(this, "Article data not available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        populateArticle(article)

        // Back button click returns to the previous screen.
        binding.backButton.setOnClickListener { finish() }
    }

    private fun getArticleFromIntent(): Article? {
        val articleId = intent.getStringExtra(EXTRA_ARTICLE_ID)
        return if (articleId != null) {
            ArticleRepository.getArticle(articleId)
        } else {
            null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateArticle(article: Article) {
        binding.articleTopic.text = article.title

        // Use username from the article endpoint to display in writer name.
        binding.writerName.text = article.username

        // Only show the date.
        // Node.js sends the date as "2025-02-18T16:24:00.816Z", so we extract "2025-02-18"
        binding.updatedDate.text = if (article.updatedAt != null && article.updatedAt.length >= 10) {
            article.updatedAt.substring(0, 10)
        } else {
            "N/A"
        }

        binding.articleContent.text = article.article

        // Decode and set image from Base64 string.
        val bitmap = decodeBase64ToBitmap(article.img)
        if (bitmap != null) {
            binding.articleImage.setImageBitmap(bitmap)
        } else {
            binding.articleImage.setImageResource(R.drawable.error_img)
        }

        // Setup tags RecyclerView.
        binding.tagsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.tagsRecyclerView.adapter = TagsAdapter(article.tags)
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        if (base64String.isEmpty()) return null

        // Remove any data URL prefix if present.
        val cleanBase64 = if (base64String.startsWith("data:")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }
        return try {
            val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}