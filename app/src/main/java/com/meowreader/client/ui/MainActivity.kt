package com.meowreader.client.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.meowreader.client.R
import com.meowreader.client.data.network.RetrofitClient
import com.meowreader.client.data.repository.SyncRepository
import com.meowreader.client.databinding.ActivityMainBinding
import com.meowreader.client.ui.analytics.AnalyticsFragment
import com.meowreader.client.ui.reading.ReadingFragment
import com.meowreader.client.ui.reading.RoomDatabaseClient
import com.meowreader.client.ui.settings.SettingsFragment
import com.google.android.material.navigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_reading -> switchFragment(ReadingFragment())
                R.id.nav_analytics -> switchFragment(AnalyticsFragment())
                R.id.nav_settings -> switchFragment(SettingsFragment())
            }
            true
        }

        // Initial fragment
        if (savedInstanceState == null) {
            switchFragment(ReadingFragment())
        }

        // Cold Start Sync
        lifecycleScope.launch {
            try {
                val db = RoomDatabaseClient.getDatabase(this@MainActivity)
                val repo = SyncRepository(RetrofitClient.getService(this@MainActivity), db)
                repo.syncIndex()
                repo.downloadPendingPapers()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
