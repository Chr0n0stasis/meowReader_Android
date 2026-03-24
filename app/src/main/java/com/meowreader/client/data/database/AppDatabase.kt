package com.meowreader.client.data.database

import androidx.room.*
import com.meowreader.client.domain.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PaperDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: PaperEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT * FROM papers WHERE id = :id")
    suspend fun getPaperById(id: String): PaperEntity?

    @Query("SELECT * FROM questions WHERE paperId = :paperId ORDER BY qNumber ASC")
    fun getQuestionsForPaper(paperId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM papers ORDER BY groupUpdateDate DESC LIMIT 1")
    fun getLatestPaper(): Flow<PaperEntity?>
    
    @Query("SELECT * FROM papers ORDER BY groupUpdateDate DESC")
    fun getAllPapers(): Flow<List<PaperEntity>>
}

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateHistory(history: HistoryEntity)

    @Query("SELECT * FROM user_history WHERE paperId = :paperId")
    suspend fun getHistoryForPaper(paperId: String): HistoryEntity?

    @Query("SELECT * FROM user_history ORDER BY completionDate DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>
}

@Dao
interface SyncDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIndex(items: List<SyncIndexEntity>)

    @Query("SELECT * FROM sync_index WHERE isDownloaded = 0")
    suspend fun getPendingDownloads(): List<SyncIndexEntity>

    @Query("UPDATE sync_index SET isDownloaded = 1 WHERE id = :id")
    suspend fun markAsDownloaded(id: String)

    @Query("SELECT id FROM papers")
    suspend fun getAllPaperIds(): List<String>
}

@Dao
interface AnswerDao {
    @Query("SELECT * FROM user_answers WHERE paperId = :paperId")
    fun getAnswersForPaper(paperId: String): Flow<List<UserAnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: UserAnswerEntity)
}

@Database(
    entities = [PaperEntity::class, QuestionEntity::class, HistoryEntity::class, VocabularyEntity::class, SyncIndexEntity::class, UserAnswerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun paperDao(): PaperDao
    abstract fun historyDao(): HistoryDao
    abstract fun syncDao(): SyncDao
    abstract fun answerDao(): AnswerDao
}
