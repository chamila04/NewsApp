package com.example.newsapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.R
import com.example.newsapp.api.ApiClient
import com.example.newsapp.api.Article
import com.example.newsapp.api.ArticlesAdapter
import com.example.newsapp.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var articlesList: List<Article> = emptyList()
    private lateinit var articlesAdapter: ArticlesAdapter

    // List to keep track of the category buttons
    private lateinit var categoryButtons: List<View>

    // Variable to store the currently selected category tag.
    // It is null if no category is selected.
    private var currentSelectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Setup RecyclerView
        binding.articlesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        articlesAdapter = ArticlesAdapter(articlesList) { article ->
            // When an article card is clicked, open the ViewArticleActivity
            startActivity(ViewArticleActivity.newIntent(requireContext(), article))
        }
        binding.articlesRecyclerView.adapter = articlesAdapter

        // Initialize category buttons (including Business and Technology buttons)
        categoryButtons = listOf(
            binding.localButton,
            binding.globalButton,
            binding.politicsButton,
            binding.entertainmentButton,
            binding.sportsButton,
            binding.businessButton,
            binding.technologyButton
        )

        // Set click listeners for category buttons
        binding.localButton.setOnClickListener { onCategorySelected("Local", it) }
        binding.globalButton.setOnClickListener { onCategorySelected("Global", it) }
        binding.politicsButton.setOnClickListener { onCategorySelected("Politics", it) }
        binding.entertainmentButton.setOnClickListener { onCategorySelected("Entertainment", it) }
        binding.sportsButton.setOnClickListener { onCategorySelected("Sports", it) }
        binding.businessButton.setOnClickListener { onCategorySelected("Business", it) }
        binding.technologyButton.setOnClickListener { onCategorySelected("Technology", it) }

        // Set listener on the search EditText so that when the search action is pressed:
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchEditText.text.toString().trim()
                if (query.isEmpty()) {
                    fetchArticles() // Show all articles if search is empty
                } else {
                    searchArticles(query) // Call search API with the query
                }
                // Hide the keyboard:
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                true
            } else {
                false
            }
        }

        // Load all accepted articles on start
        fetchArticles()
    }

    /**
     * Called when a category button is clicked.
     * It highlights the selected button and fetches articles by the tag.
     * If the same category button is clicked again, it unselects that button and loads all articles.
     */
    private fun onCategorySelected(tag: String, selectedView: View) {
        // If the same category is clicked again, unselect it.
        if (currentSelectedCategory == tag) {
            currentSelectedCategory = null
            // Reset all buttons to inactive colors.
            for (button in categoryButtons) {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.inactiveColor))
                if (button is com.google.android.material.button.MaterialButton) {
                    button.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactiveTextColor))
                }
            }
            // Fetch all articles since no category is selected.
            fetchArticles()
            return
        }

        // Otherwise, select the new category.
        currentSelectedCategory = tag

        // Reset all buttons to inactive colors.
        for (button in categoryButtons) {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.inactiveColor))
            if (button is com.google.android.material.button.MaterialButton) {
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactiveTextColor))
            }
        }
        // Set the clicked button to active colors.
        selectedView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.activeColor))
        if (selectedView is com.google.android.material.button.MaterialButton) {
            selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.activeTextColor))
        }

        // Fetch articles that match the selected tag.
        fetchArticles(tag)
    }

    /**
     * Fetch articles.
     * If [tag] is provided, call the API to get articles by tag.
     * Otherwise, get all accepted articles.
     *
     * After receiving the results, we further filter the list on the client so that only
     * articles whose tags match (ignoring case) the provided tag are shown.
     * Then, we sort the articles so that the most recent article is at the top.
     */
    private fun fetchArticles(tag: String? = null) {
        lifecycleScope.launch {
            try {
                if (tag.isNullOrEmpty()) {
                    Log.d("SearchFragment", "Fetching all accepted articles")
                } else {
                    Log.d("SearchFragment", "Fetching articles for tag: $tag")
                }
                val response = if (tag.isNullOrEmpty()) {
                    ApiClient.apiService.getAllArticles()
                } else {
                    ApiClient.apiService.getArticlesByTag(tag)
                }
                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        Log.d("SearchFragment", "Received ${result.articles.size} articles")
                        // If a tag was provided, filter the articles on the client
                        // then sort them by createdAt (newest first).
                        articlesList = if (!tag.isNullOrEmpty()) {
                            result.articles.filter { article ->
                                article.tags.any { it.equals(tag, ignoreCase = true) }
                            }.sortedByDescending { it.createdAt }
                        } else {
                            result.articles.sortedByDescending { it.createdAt }
                        }
                        updateRecyclerView()
                    } ?: Log.e("SearchFragment", "Response body is null")
                } else {
                    Log.e("SearchFragment", "API Error: ${response.code()}")
                }
            } catch (e: IOException) {
                Log.e("SearchFragment", "Network error: ${e.localizedMessage}")
            } catch (e: HttpException) {
                Log.e("SearchFragment", "HTTP error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Search articles by the provided [query] using the searchArticles API endpoint.
     * After receiving the results, sort the articles so that the most recent article is at the top.
     */
    private fun searchArticles(query: String) {
        lifecycleScope.launch {
            try {
                Log.d("SearchFragment", "Searching articles for query: $query")
                val response = ApiClient.apiService.searchArticles(query)
                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        Log.d("SearchFragment", "Received ${result.articles.size} search results")
                        articlesList = result.articles.sortedByDescending { it.createdAt }
                        updateRecyclerView()
                    } ?: Log.e("SearchFragment", "Search response body is null")
                } else {
                    Log.e("SearchFragment", "Search API error: ${response.code()}")
                }
            } catch (e: IOException) {
                Log.e("SearchFragment", "Search network error: ${e.localizedMessage}")
            } catch (e: HttpException) {
                Log.e("SearchFragment", "Search HTTP error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Update the RecyclerView.
     * If articlesList is empty, we hide the RecyclerView (or you may choose to show a placeholder).
     * Otherwise, we update the adapter with the list.
     */
    private fun updateRecyclerView() {
        articlesAdapter = ArticlesAdapter(articlesList) { article ->
            startActivity(ViewArticleActivity.newIntent(requireContext(), article))
        }
        binding.articlesRecyclerView.adapter = articlesAdapter

        // Hide the RecyclerView if there are no articles.
        binding.articlesRecyclerView.visibility =
            if (articlesList.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}