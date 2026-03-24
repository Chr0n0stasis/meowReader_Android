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
        
        // Apply Theme
        val prefs = getSharedPreferences("reader_prefs", android.content.Context.MODE_PRIVATE)
        val mode = prefs.getInt("theme_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
        
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

    private val readingViewModel: com.meowreader.client.ui.reading.ReadingViewModel by androidx.activity.viewModels {
        val db = com.meowreader.client.ui.reading.RoomDatabaseClient.getDatabase(this)
        com.meowreader.client.ui.reading.ReadingViewModelFactory(db.paperDao())
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
                    item.isCheckable = true
                }

                // Initial Highlight
                updateSidebarHighlight(papers)

                binding.navView.setNavigationItemSelectedListener { menuItem ->
                    val paper = papers[menuItem.itemId]
                    readingViewModel.setPaper(paper.id)
                    
                    if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is ReadingFragment) {
                        binding.bottomNavigation.selectedItemId = R.id.nav_reading
                    }
                    
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                // Sync Highlight with VM
                launch {
                    readingViewModel.currentPaper.collectLatest { paper ->
                        updateSidebarHighlight(papers, paper?.id)
                    }
                }
            }
        }
    }

    private fun updateSidebarHighlight(papers: List<com.meowreader.client.domain.model.PaperEntity>, activeId: String? = null) {
        val menu = binding.navView.menu
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            item.isChecked = papers[i].id == activeId
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
