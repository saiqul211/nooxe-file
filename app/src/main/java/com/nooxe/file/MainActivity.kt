package com.nooxe.file

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nooxe.file.databinding.ActivityMainBinding
import com.nooxe.file.ui.HomeFragment
import com.nooxe.file.ui.BrowseFragment
import com.nooxe.file.ui.CleanFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            showFragment(HomeFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { showFragment(HomeFragment()); true }
                R.id.nav_browse -> { showFragment(BrowseFragment()); true }
                R.id.nav_clean -> { showFragment(CleanFragment()); true }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        val current = supportFragmentManager.findFragmentById(R.id.container)
        if (current is OnBackPressedListener && current.onBackPressed()) return
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}

interface OnBackPressedListener {
    fun onBackPressed(): Boolean
}
