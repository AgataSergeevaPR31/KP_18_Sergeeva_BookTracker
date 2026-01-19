package com.example.libro.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.libro.R
import com.example.libro.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            binding.bottomNavigation.setupWithNavController(navController)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        handleAchievementsIntent()
    }

    private fun handleAchievementsIntent() {
        intent?.let { intent ->
            val showDialog = intent.getBooleanExtra("NEW_ACHIEVEMENTS", false)
            val achievementNames = intent.getStringArrayExtra("ACHIEVEMENT_NAMES")

            if (showDialog && !achievementNames.isNullOrEmpty()) {
                val bundle = Bundle().apply {
                    putBoolean("show_achievement_dialog", true)
                    putStringArray("achievement_names", achievementNames)
                }

                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                navHostFragment?.childFragmentManager?.fragments?.forEach { fragment ->
                    if (fragment is com.example.libro.ui.home.HomeFragment) {
                        fragment.arguments = bundle
                    }
                }
            }

            val fragmentToLoad = intent.getStringExtra("FRAGMENT_TO_LOAD")
            if (fragmentToLoad == "home") {
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                navHostFragment?.navController?.navigate(R.id.homeFragment)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAchievementsIntent()
    }
}