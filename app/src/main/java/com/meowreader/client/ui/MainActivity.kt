package com.meowreader.client.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.meowreader.client.R
import com.meowreader.client.data.network.RetrofitClient
import com.meowreader.client.data.repository.SyncRepository
import com.meowreader.client.databinding.ActivityMainBinding
import com.meowreader.client.ui.reading.ReadingFragment
import com.meowreader.client.ui.reading.RoomDatabaseClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReadingFragment())
                .commit()
        }

        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Module 1: Cold Start Sync
        val syncRepo = SyncRepository(
            RetrofitClient.apiService,
            RoomDatabaseClient.getDatabase(this)
        )
        lifecycleScope.launch {
            syncRepo.syncIndex()
            syncRepo.downloadPendingPapers()
        }
    }
}
