package com.example.newsapp.api

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.databinding.ArticleTagBinding


class TagsAdapter(private val tags: List<String>) :
    RecyclerView.Adapter<TagsAdapter.TagViewHolder>() {

    inner class TagViewHolder(private val binding: ArticleTagBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tag: String) {
            binding.tagText.text = tag
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ArticleTagBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tags[position])
    }

    override fun getItemCount(): Int = tags.size
}