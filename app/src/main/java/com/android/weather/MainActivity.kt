package com.android.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.android.weather.databinding.ActivityMainBinding
import com.android.weather.fragments.HomeFragment
import com.android.weather.module.NavigationListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class MainActivity : AppCompatActivity(), NavigationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainNavController: NavController

    // get current navigation fragment
    private val currentMainNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        mainNavController = navHostFragment.findNavController()
    }

    // Handle back press
    override fun onBackPressed() {
        binding.apply {
            if (currentMainNavigationFragment is HomeFragment) {
                finish()
            } else {
                onBackPressed()
            }
        }
    }
}