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
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import android.view.Menu

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

        val bottomNav = binding.bottomNavigation as com.google.android.material.bottomnavigation.BottomNavigationView
        bottomNav.setOnItemSelectedListener { item: android.view.MenuItem ->
            when(item.itemId) {
                R.id.nav_reading -> switchFragment(ReadingFragment())
                R.id.nav_analytics -> switchFragment(AnalyticsFragment())
                R.id.nav_settings -> switchFragment(SettingsFragment())
                else -> false
            }
            true
        }

        // Initial fragment
        if (savedInstanceState == null) {
            switchFragment(ReadingFragment())
        }

        setupNavigationDrawer()

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

    private fun setupNavigationDrawer() {
        val db = RoomDatabaseClient.getDatabase(this)
        lifecycleScope.launch {
            db.paperDao().getAllPapers().collectLatest { papers ->
                val menu = binding.navView.menu
                menu.clear()
                papers.forEachIndexed { index, paper ->
                    val item = menu.add(Menu.NONE, index, Menu.NONE, paper.title)
                    item.setIcon(R.drawable.ic_launcher)
                }

                binding.navView.setNavigationItemSelectedListener { menuItem ->
                    val paper = papers[menuItem.itemId]
                    // Tell ReadingFragment to switch
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (currentFragment is ReadingFragment) {
                        currentFragment.switchToPaper(paper.id)
                    } else {
                        // Switch to Reading tab first then load paper
                        binding.bottomNavigation.selectedItemId = R.id.nav_reading
                        // Need a small delay or use shared ViewModel
                        lifecycleScope.launch {
                            kotlinx.coroutines.delay(100)
                            (supportFragmentManager.findFragmentById(R.id.fragment_container) as? ReadingFragment)?.switchToPaper(paper.id)
                        }
                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
