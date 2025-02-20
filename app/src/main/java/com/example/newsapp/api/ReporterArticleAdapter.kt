package com.example.newsapp.api

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.databinding.ArticleCardRepoterBinding

class ReporterArticlesAdapter(
    private val articles: List<Article>,
    private val onEditClick: (Article) -> Unit,
    private val onStatusClick: (Article) -> Unit
) : RecyclerView.Adapter<ReporterArticlesAdapter.ReporterArticleViewHolder>() {

    inner class ReporterArticleViewHolder(val binding: ArticleCardRepoterBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.tvTopic.text = article.title
            binding.btnStatus.text = article.status

            // Set button background color based on the status text.
            when (article.status.lowercase()) {
                "accept" -> binding.btnStatus.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.light_green)
                )
                "pending" -> binding.btnStatus.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.light_yellow)
                )
                "reject" -> binding.btnStatus.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.light_red)
                )
            }

            binding.btnStatus.setOnClickListener { onStatusClick(article) }
            binding.btnEdit.setOnClickListener { onEditClick(article) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReporterArticleViewHolder {
        val binding = ArticleCardRepoterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReporterArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReporterArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount() = articles.size
}
