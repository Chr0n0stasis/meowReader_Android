package com.meowreader.client.ui.analytics

import androidx.lifecycle.*
import com.meowreader.client.data.database.HistoryDao
import com.meowreader.client.domain.model.HistoryEntity
import kotlinx.coroutines.flow.*

class AnalyticsViewModel(private val historyDao: HistoryDao) : ViewModel() {

    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    val b35Rating: Flow<Double> = allHistory.map { history ->
        history.filter { it.isCompleted }
            .sortedByDescending { it.rating }
            .take(35)
            .sumOf { it.rating }
    }

    val b15RatingWeekly: Flow<Double> = allHistory.map { history ->
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        history.filter { it.isCompleted && it.completionDate > oneWeekAgo }
            .sortedByDescending { it.rating }
            .take(15)
            .sum()
    }

    val totalRating: Flow<Double> = combine(b35Rating, b15RatingWeekly) { b35, b15 ->
        b35 + b15
    }
}
