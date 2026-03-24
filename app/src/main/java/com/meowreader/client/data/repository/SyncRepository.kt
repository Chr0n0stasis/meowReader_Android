package com.meowreader.client.data.repository

import com.meowreader.client.data.database.*
import com.meowreader.client.data.network.*
import com.meowreader.client.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class SyncRepository(
    private val apiService: ApiService,
    private val appDatabase: AppDatabase
) {
    private val syncDao = appDatabase.syncDao()
    private val paperDao = appDatabase.paperDao()

    suspend fun syncIndex() = withContext(Dispatchers.IO) {
        try {
            val index = apiService.getIndex()
            val entities = index.map {
                SyncIndexEntity(
                    id = it.id,
                    date = it.date,
                    strategy = it.strategy,
                    remotePath = it.path,
                    isDownloaded = false
                )
            }
            syncDao.insertIndex(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun downloadPendingPapers() = withContext(Dispatchers.IO) {
        val pending = syncDao.getPendingDownloads()
        pending.forEach { item ->
            try {
                val group = apiService.getPaperGroup(item.remotePath)
                group.papers.forEach { paperDto ->
                    val paperId = "${item.id}-${paperDto.title.hashCode()}"
                    val paperEntity = PaperEntity(
                        id = paperId,
                        title = paperDto.title,
                        articleUpdateDate = paperDto.articleUpdateDate,
                        sourceJournal = paperDto.sourceJournal,
                        bodyText = paperDto.bodyText,
                        difficultyConstant = paperDto.difficultyConstant,
                        questionType = paperDto.questionType,
                        groupUpdateDate = group.groupUpdateDate
                    )
                    paperDao.insertPaper(paperEntity)

                    val questions = paperDto.questions.map { q ->
                        QuestionEntity(
                            paperId = paperId,
                            qNumber = q.qNumber,
                            stem = q.stem,
                            optionA = q.options.A,
                            optionB = q.options.B,
                            optionC = q.options.C,
                            optionD = q.options.D,
                            answer = q.answer,
                            explanation = q.explanation
                        )
                    }
                    paperDao.insertQuestions(questions)
                }
                syncDao.markAsDownloaded(item.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
