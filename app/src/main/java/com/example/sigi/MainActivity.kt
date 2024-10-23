package com.example.sigi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.sigi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setBottomNavigationView()

        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        }
    }

    private fun setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, HomeFragment()).commit()
                    true
                }
                R.id.fragment_search -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, SearchFragment()).commit()
                    true
                }
                R.id.fragment_calendar -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, CalendarFragment()).commit()
                    true
                }
                R.id.fragment_settings -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, SettingsFragment()).commit()
                    true
                }
                else -> false
            }
        }
    }
}
