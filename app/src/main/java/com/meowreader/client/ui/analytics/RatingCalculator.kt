package com.meowreader.client.ui.analytics

import kotlin.math.floor

object RatingCalculator {
    /**
     * R = floor(C * A * M) + B
     * C: Difficulty Constant (1.0 - 15.0)
     * A: Accuracy (0.0 - 1.0)
     * M: Performance Coefficient (standard 1.0)
     * B: Full combo/All perfect bonus
     */
    fun calculateRating(difficulty: Double, correctCount: Int, totalCount: Int): Double {
        if (totalCount == 0) return 0.0
        val accuracy = correctCount.toDouble() / totalCount
        val performanceMultiplier = 1.0
        val bonus = if (accuracy == 1.0) 50.0 else 0.0
        
        // Scale factor to make it feel like maimai rating
        val baseRating = floor(difficulty * accuracy * 10.0 * performanceMultiplier)
        return baseRating + bonus
    }
}
