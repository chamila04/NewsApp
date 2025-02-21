package com.example.newsapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.newsapp.api.ApiClient
import com.example.newsapp.api.Article
import com.example.newsapp.api.ArticleRequest
import com.example.newsapp.api.ArticleResult
import com.example.newsapp.databinding.ActivityArticleWriteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


class ArticleWriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArticleWriteBinding
    // Holds the Base64 encoded image string after the user selects an image.
    private var base64Image: String? = null

    // In a real app, you might get the username from the logged-in user.
    private val username: String by lazy { intent.getStringExtra("username") ?: "defaultUser" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if an article is passed for editing.
        val article = intent.getParcelableExtra<Article>("article")
        if (article != null) {
            // Pre-populate fields.
            binding.topicInput.setText(article.title)
            base64Image = article.img
            val bitmap = decodeBase64ToBitmap(article.img)
            if (bitmap != null) {
                binding.imagePreview.setImageBitmap(bitmap)
            } else {
                binding.imagePreview.setImageResource(R.drawable.error_img)
            }
            // Set tag checkboxes.
            binding.checkboxLocal.isChecked = article.tags.contains("Local")
            binding.checkboxBusiness.isChecked = article.tags.contains("Business")
            binding.checkboxGlobal.isChecked = article.tags.contains("Global")
            binding.checkboxTechnology.isChecked = article.tags.contains("Technology")
            binding.checkboxEntertainment.isChecked = article.tags.contains("Entertainment")
            binding.checkboxPolitics.isChecked = article.tags.contains("Politics")
            binding.checkboxSports.isChecked = article.tags.contains("Sports")
            binding.articleInput.setText(article.article)
        }

        // Set up the Upload button.
        binding.uploadButton.setOnClickListener {
            pickImageFromGallery()
        }

        // Cancel and Back buttons simply finish the activity.
        binding.cancelButton.setOnClickListener { finish() }
        binding.backButton.setOnClickListener { finish() }

        // Publish button – validate fields and send article to API.
        binding.publishButton.setOnClickListener {
            publishArticle()
        }
    }

    // Register for the image picker result.
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                // Show a temporary preview.
                binding.imagePreview.setImageURI(uri)
                // Convert the image URI to a Bitmap and then to a Base64 string.
                lifecycleScope.launch {
                    val encodedImage = convertUriToBase64(uri)
                    if (encodedImage != null) {
                        base64Image = encodedImage
                    } else {
                        // If conversion fails (for example, due to file size), show an error image.
                        binding.imagePreview.setImageResource(R.drawable.error_img)
                    }
                }
            }
        }

    private fun pickImageFromGallery() {
        // Open the image picker (images only).
        imagePickerLauncher.launch("image/*")
    }

    /**
     * Converts an image URI to a Base64 string.
     * First, it checks the file size using AssetFileDescriptor.
     * If the image file size is more than 50 KB, it shows an error and returns null.
     */
    private suspend fun convertUriToBase64(uri: android.net.Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Use AssetFileDescriptor to check the file size.
                val afd = contentResolver.openAssetFileDescriptor(uri, "r")
                val fileSize = afd?.length ?: -1L
                afd?.close()
                if (fileSize > 100 * 1024) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ArticleWriteActivity,
                            "Image file size must be less than 50 KB",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@withContext null
                }

                // Now decode the image.
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap == null) return@withContext null

                // Compress the bitmap to JPEG.
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val byteArray = outputStream.toByteArray()

                // Double-check the compressed image size.
                if (byteArray.size > 100 * 1024) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ArticleWriteActivity,
                            "Compressed image size must be less than 50 KB",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@withContext null
                }
                return@withContext Base64.encodeToString(byteArray, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    // Converts a Base64 string back to a Bitmap.
    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Validates fields and calls the API to create the article.
    private fun publishArticle() {
        val title = binding.topicInput.text.toString().trim()
        val articleContent = binding.articleInput.text.toString().trim()

        // Gather selected tags.
        val selectedTags = mutableListOf<String>()
        if (binding.checkboxLocal.isChecked) selectedTags.add("Local")
        if (binding.checkboxBusiness.isChecked) selectedTags.add("Business")
        if (binding.checkboxGlobal.isChecked) selectedTags.add("Global")
        if (binding.checkboxTechnology.isChecked) selectedTags.add("Technology")
        if (binding.checkboxEntertainment.isChecked) selectedTags.add("Entertainment")
        if (binding.checkboxPolitics.isChecked) selectedTags.add("Politics")
        if (binding.checkboxSports.isChecked) selectedTags.add("Sports")

        // Validate that none of the fields are empty.
        if (title.isEmpty() ||
            articleContent.isEmpty() ||
            base64Image.isNullOrEmpty() ||
            selectedTags.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Build the article request.
        val articleRequest = ArticleRequest(
            username = username,
            title = title,
            tags = selectedTags,
            img = base64Image!!,
            article = articleContent
        )

        // Call the backend API to create the article.
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.createArticle(articleRequest)
                }
                if (response.isSuccessful) {
                    val result: ArticleResult? = response.body()
                    val message = result?.message ?: "Article published successfully!"
                    Toast.makeText(this@ArticleWriteActivity, message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@ArticleWriteActivity,
                        "Error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ArticleWriteActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}