package com.meowreader.client.ui.analytics

import android.os.Bundle
import android.view.*
import android.widget.GridLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meowreader.client.databinding.FragmentAnalyticsBinding
import com.meowreader.client.ui.reading.RoomDatabaseClient
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.graphics.Color
import android.view.View

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyticsViewModel by viewModels {
        val db = RoomDatabaseClient.getDatabase(requireContext())
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AnalyticsViewModel(db.historyDao()) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalRating.collectLatest { rating ->
                binding.totalRatingText.text = "%.1f".format(rating)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allHistory.collectLatest { history ->
                renderHeatmap(history.map { it.completionDate })
            }
        }
    }

    private fun renderHeatmap(dates: List<Long>) {
        binding.archiveHeatmap.removeAllViews()
        val context = requireContext()
        val dayMillis = 24 * 60 * 60 * 1000L
        val today = System.currentTimeMillis()
        
        // Show last 20 weeks (140 days)
        for (i in 0 until 140) {
            val dayTimestamp = today - (139 - i) * dayMillis
            val hasActivity = dates.any { Math.abs(it - dayTimestamp) < dayMillis / 2 }
            
            val view = View(context)
            val params = GridLayout.LayoutParams()
            params.width = 32
            params.height = 32
            params.setMargins(4, 4, 4, 4)
            // Row is day of week (i % 7), Column is week index (i / 7)
            params.columnSpec = GridLayout.spec(i / 7)
            params.rowSpec = GridLayout.spec(i % 7)
            view.layoutParams = params
            
            val color = if (hasActivity) "#66BB6A" else "#EEEEEE"
            view.setBackgroundColor(Color.parseColor(color))
            binding.archiveHeatmap.addView(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
