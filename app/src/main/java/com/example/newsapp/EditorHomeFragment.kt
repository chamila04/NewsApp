package com.example.newsapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.api.ApiClient
import com.example.newsapp.api.ArticlesAdapter
import com.example.newsapp.databinding.FragmentEditorHomeBinding
import kotlinx.coroutines.launch

class EditorHomeFragment : Fragment() {
    private var _binding: FragmentEditorHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var articlesAdapter: ArticlesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvArticles.layoutManager = LinearLayoutManager(requireContext())

        // Set an empty adapter first if needed
        articlesAdapter = ArticlesAdapter(listOf()) { article ->
            // Handle click, for example:
            val intent = Intent(requireContext(), EditorReviewActivity::class.java)
            intent.putExtra("article", article)
            startActivity(intent)
        }
        binding.rvArticles.adapter = articlesAdapter

        // Use a coroutine to fetch articles
        lifecycleScope.launch {
            try {
                // Call the suspend function from ApiClient.apiService
                val response = ApiClient.apiService.getArticles()
                if (response.isSuccessful && response.body() != null) {
                    val articles = response.body()!!.articles
                    // Update the adapter with the fetched articles
                    articlesAdapter = ArticlesAdapter(articles) { article ->
                        val intent = Intent(requireContext(), EditorReviewActivity::class.java)
                        intent.putExtra("article", article)
                        startActivity(intent)
                    }
                    binding.rvArticles.adapter = articlesAdapter
                } else {
                    Log.e("EditorHomeFragment", "Error: Response not successful ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("EditorHomeFragment", "Exception: Failed to fetch articles: ${e.localizedMessage}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}