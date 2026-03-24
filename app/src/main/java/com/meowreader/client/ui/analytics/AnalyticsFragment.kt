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
            viewModel.b35Rating.collectLatest { rating ->
                binding.b35Text.text = "B35: ${"%.1f".format(rating)}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.b15RatingWeekly.collectLatest { rating ->
                binding.b15Text.text = "B15: ${"%.1f".format(rating)}"
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
        // Simplified heatmap logic: 7x52 grid
        for (i in 0 until 7 * 10) { // Just show 10 weeks for demo
            val view = View(requireContext())
            val params = GridLayout.LayoutParams()
            params.width = 40
            params.height = 40
            params.setMargins(4, 4, 4, 4)
            view.layoutParams = params
            
            // Random colors for aesthetics in demo
            val color = if (dates.isNotEmpty()) "#4CAF50" else "#E0E0E0"
            view.setBackgroundColor(Color.parseColor(color))
            binding.archiveHeatmap.addView(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
