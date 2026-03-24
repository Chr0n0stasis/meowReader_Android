package com.meowreader.client.ui.reading

import androidx.lifecycle.*
import com.meowreader.client.data.database.PaperDao
import com.meowreader.client.domain.model.PaperEntity
import com.meowreader.client.domain.model.QuestionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingViewModel(private val paperDao: PaperDao) : ViewModel() {

    private val _currentPaperId = MutableStateFlow<String?>(null)
    
    val currentPaper: Flow<PaperEntity?> = _currentPaperId.flatMapLatest { id ->
        if (id == null) paperDao.getLatestPaper()
        else flowOf(paperDao.getPaperById(id))
    }

    val currentQuestions: Flow<List<QuestionEntity>> = _currentPaperId.flatMapLatest { id ->
        if (id == null) {
            paperDao.getLatestPaper().flatMapLatest { paper ->
                paper?.let { paperDao.getQuestionsForPaper(it.id) } ?: flowOf(emptyList())
            }
        } else {
            paperDao.getQuestionsForPaper(id)
        }
    }

    fun setPaper(id: String) {
        _currentPaperId.value = id
    }
}

class ReadingViewModelFactory(private val paperDao: PaperDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReadingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReadingViewModel(paperDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
