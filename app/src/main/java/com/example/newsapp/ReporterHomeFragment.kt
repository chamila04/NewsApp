package com.example.newsapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.api.ApiClient
import com.example.newsapp.api.ArticleResult
import com.example.newsapp.api.ReporterArticlesAdapter
import com.example.newsapp.api.UserSession
import com.example.newsapp.databinding.FragmentReporterHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReporterHomeFragment : Fragment() {

    private var _binding: FragmentReporterHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var reporterUsername: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReporterHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Instead of reading from requireActivity().intent, read from UserSession
        reporterUsername = UserSession.username ?: "null"

        binding.rvArticles.layoutManager = LinearLayoutManager(requireContext())

        // Set initial style: "All" filter is active
        updateButtonStyles("all")
        loadArticles("all")

        // Set click listeners for filter buttons
        binding.btnAll.setOnClickListener {
            updateButtonStyles("all")
            loadArticles("all")
        }

        binding.btnAccept.setOnClickListener {
            updateButtonStyles("accept")
            loadArticles("accept")
        }

        binding.btnPending.setOnClickListener {
            updateButtonStyles("pending")
            loadArticles("pending")
        }

        binding.btnReject.setOnClickListener {
            updateButtonStyles("reject")
            loadArticles("reject")
        }

        // Floating Action Button launches ArticleWriteActivity for new articles.
        binding.fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), ArticleWriteActivity::class.java)
            // Pass the username along from UserSession or reporterUsername
            intent.putExtra("username", reporterUsername)
            startActivity(intent)
        }
    }

    /**
     * Loads articles based on the provided filter.
     * "all" loads all articles; "accept", "pending" or "reject" call their specific endpoints.
     */
    private fun loadArticles(filter: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    when (filter) {
                        "accept" -> ApiClient.apiService.getReporterAcceptArticles(reporterUsername)
                        "pending" -> ApiClient.apiService.getReporterPendingArticles(reporterUsername)
                        "reject" -> ApiClient.apiService.getReporterRejectArticles(reporterUsername)
                        else -> ApiClient.apiService.getReporterAllArticles(reporterUsername)
                    }
                }
                if (response.isSuccessful) {
                    val result: ArticleResult? = response.body()
                    if (result != null && result.count > 0) {
                        val adapter = ReporterArticlesAdapter(
                            articles = result.articles,
                            onEditClick = { article ->
                                // Launch ArticleWriteActivity for editing
                                val intent = Intent(requireContext(), ArticleWriteActivity::class.java)
                                intent.putExtra("article", article)
                                startActivity(intent)
                            },
                            onStatusClick = { article ->
                                // Launch ViewFeedbackActivity with feedback details
                                val intent = Intent(requireContext(), ViewFeedbackActivity::class.java)
                                intent.putExtra("topic", article.title)
                                intent.putExtra("feedback", article.feedback ?: "No feedback available")
                                intent.putExtra("article", article)
                                startActivity(intent)
                            }
                        )
                        binding.rvArticles.adapter = adapter
                    } else {
                        binding.rvArticles.adapter = ReporterArticlesAdapter(
                            articles = emptyList(),
                            onEditClick = {},
                            onStatusClick = {}
                        )
                        Toast.makeText(requireContext(), "No articles found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Updates the style of the filter buttons.
     * The selected button gets the active style, while the others are set inactive.
     */
    private fun updateButtonStyles(selected: String) {
        when (selected) {
            "all" -> {
                setActive(binding.btnAll)
                setInactive(binding.btnAccept)
                setInactive(binding.btnPending)
                setInactive(binding.btnReject)
            }
            "accept" -> {
                setActive(binding.btnAccept)
                setInactive(binding.btnAll)
                setInactive(binding.btnPending)
                setInactive(binding.btnReject)
            }
            "pending" -> {
                setActive(binding.btnPending)
                setInactive(binding.btnAll)
                setInactive(binding.btnAccept)
                setInactive(binding.btnReject)
            }
            "reject" -> {
                setActive(binding.btnReject)
                setInactive(binding.btnAll)
                setInactive(binding.btnAccept)
                setInactive(binding.btnPending)
            }
        }
    }

    /**
     * Applies the active style to a button.
     */
    private fun setActive(button: Button) {
        button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.activeColor)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.activeTextColor))
    }

    /**
     * Applies the inactive style to a button.
     */
    private fun setInactive(button: Button) {
        button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.inactiveColor)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactiveTextColor))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}