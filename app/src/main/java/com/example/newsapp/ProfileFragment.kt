package com.example.newsapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.newsapp.api.SessionManager
import com.example.newsapp.api.UserSession
import com.example.newsapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    // _binding is nullable and only valid between onCreateView and onDestroyView.
    private var _binding: FragmentProfileBinding? = null
    // This property is valid only between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display the saved session details.
        binding.fullNameText.text = sessionManager.getFullName() ?: "Guest"
        binding.typeText.text = sessionManager.getUserType() ?: "Unknown"

        // When the logout button is clicked, clear both the UserSession and the SessionManager.
        binding.logoutButton.setOnClickListener {
            // Clear the in-memory UserSession values.
            UserSession.username = null
            UserSession.userType = null
            UserSession.fullName = null

            // Clear the persistent session details.
            sessionManager.clearSession()

            // Redirect to WelcomeActivity.
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // When the settings button is clicked, go to the SettingsActivity.
        binding.settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}