package com.example.newsapp.api

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.databinding.ArticleCardEditorBinding

class ArticlesAdapter(
    private val articles: List<Article>,
    private val onItemClick: (Article) -> Unit
) : RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(private val binding: ArticleCardEditorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.tvTopic.text = article.title

            val base64String = article.img
            if (base64String.isNotEmpty()) {
                // Remove any data prefix if exists and trim whitespace/newlines
                val cleanBase64 = if (base64String.startsWith("data:")) {
                    base64String.substringAfter(",").trim()
                } else {
                    base64String.trim()
                }

                Log.d("ArticlesAdapter", "Clean Base64 string length: ${cleanBase64.length}")

                try {
                    // Try decoding using NO_WRAP (if your string has no newlines)
                    val imageBytes = Base64.decode(cleanBase64, Base64.NO_WRAP)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    if (decodedImage != null) {
                        binding.ivTopic.setImageBitmap(decodedImage)
                    } else {
                        Log.e("ArticlesAdapter", "Decoded image is null")
                        binding.ivTopic.setImageResource(R.drawable.error_img)
                    }
                } catch (e: Exception) {
                    Log.e("ArticlesAdapter", "Error decoding image: ${e.localizedMessage}")
                    binding.ivTopic.setImageResource(R.drawable.error_img)
                }
            } else {
                binding.ivTopic.setImageResource(R.drawable.error_img)
            }

            binding.root.setOnClickListener {
                onItemClick(article)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ArticleCardEditorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount() = articles.size
}