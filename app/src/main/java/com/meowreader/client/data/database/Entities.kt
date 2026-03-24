package com.meowreader.client.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "papers")
data class PaperEntity(
    @PrimaryKey val id: String, // e.g., "03.24.2026-common"
    val title: String,
    val articleUpdateDate: String,
    val sourceJournal: String,
    val bodyText: String,
    val difficultyConstant: Double,
    val questionType: String,
    val groupUpdateDate: String
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paperId: String,
    val qNumber: Int,
    val stem: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val answer: String,
    val explanation: String
)

@Entity(tableName = "user_history")
data class HistoryEntity(
    @PrimaryKey val paperId: String,
    val lastQuestionNumber: Int,
    val scrollPosition: Int,
    val isCompleted: Boolean,
    val score: Int,
    val rating: Double,
    val completionDate: Long // timestamp
)

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey val word: String,
    val definition: String,
    val phonetic: String?,
    val pos: String?, // Part of Speech
    val frequency: Int,
    val lastSeenDate: Long,
    val isKnown: Boolean = false
)

@Entity(tableName = "sync_index")
data class SyncIndexEntity(
    @PrimaryKey val id: String, // same as paperId
    val date: String,
    val strategy: String,
    val remotePath: String,
    val isDownloaded: Boolean = false
)
