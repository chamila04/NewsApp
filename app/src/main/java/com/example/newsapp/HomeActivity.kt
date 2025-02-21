package com.example.newsapp

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.api.SessionManager
import com.example.newsapp.api.UserSession
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // First, try to get username and user type from UserSession.
        var username = UserSession.username
        var userType = UserSession.userType

        // If either value is empty, retrieve from SessionManager and assign to UserSession.
        if (username.isNullOrEmpty() || userType.isNullOrEmpty()) {
            val sessionManager = SessionManager(this)
            username = sessionManager.getUsername() ?: "defaultUser"
            userType = sessionManager.getUserType() ?: "reporter"
            UserSession.username = username
            UserSession.userType = userType
        }

        // Now set up the navigation using the correct userType.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Inflate the navigation graph and choose the start destination based on userType.
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        val startDestinationId = if (userType.equals("editor", ignoreCase = true))
            R.id.editorHomeFragment
        else
            R.id.reporterHomeFragment
        navGraph.setStartDestination(startDestinationId)
        navController.graph = navGraph

        // Initialize BottomNavigationView.
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Remove the placeholder home menu item and add a new one with the proper destination ID.
        val menu = bottomNavigationView.menu
        menu.removeItem(R.id.navigation_home)
        menu.add(Menu.NONE, startDestinationId, Menu.NONE, getString(R.string.home))
            .setIcon(R.drawable.home_icon)

        // Bind BottomNavigationView to the NavController.
        bottomNavigationView.setupWithNavController(navController)
    }
}