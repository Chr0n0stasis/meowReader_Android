package com.meowreader.client.ui.reading

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.meowreader.client.data.database.AppDatabase
import com.meowreader.client.databinding.FragmentReadingBinding
import com.meowreader.client.databinding.ItemQuestionBinding
import com.meowreader.client.domain.model.QuestionEntity
import com.meowreader.client.R
import android.graphics.Color
import io.noties.markwon.Markwon
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.Modifier

class ReadingFragment : Fragment() {

    private var _binding: FragmentReadingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReadingViewModel by activityViewModels {
        val db = RoomDatabaseClient.getDatabase(requireContext())
        ReadingViewModelFactory(db.paperDao())
    }

    private lateinit var markwon: Markwon
    private var selectedQuestionForSheet by mutableStateOf<QuestionEntity?>(null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReadingBinding.inflate(inflater, container, false)
        markwon = Markwon.create(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setContent {
            MaterialTheme {
                selectedQuestionForSheet?.let { question ->
                    val options = listOf(question.optionA, question.optionB, question.optionC, question.optionD)
                    var selectedIndex by remember { mutableStateOf<Int?>(null) }
                    
                    com.meowreader.client.ui.components.QuizBottomSheet(
                        questionStem = question.stem,
                        options = options,
                        selectedIndex = selectedIndex,
                        onOptionSelected = { index ->
                            selectedIndex = index
                            handleAnswerSelection(question, index)
                        },
                        onDismiss = { selectedQuestionForSheet = null }
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentPaper.collectLatest { paper ->
                paper?.let {
                    binding.articleTitle.text = it.title
                    binding.articleMeta.text = "${it.sourceJournal} | ${it.articleUpdateDate}"
                    markwon.setMarkdown(binding.articleBody, it.bodyText)
                    
                    setupWordSelection(binding.articleBody)
                    
                    if (it.questionType == "Use of English") {
                        setupClozeTabs()
                    } else {
                        binding.clozeTabs.visibility = View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentQuestions.collectLatest { questions ->
                binding.questionsContainer.removeAllViews()
                questions.forEach { question ->
                    addQuestionView(question)
                }
            }
        }
    }

    private fun addQuestionView(question: QuestionEntity) {
        val qBinding = ItemQuestionBinding.inflate(layoutInflater, binding.questionsContainer, false)
        qBinding.questionStem.text = "${question.qNumber}. ${question.stem}"
        qBinding.optionA.text = "A. ${question.optionA}"
        qBinding.optionB.text = "B. ${question.optionB}"
        qBinding.optionC.text = "C. ${question.optionC}"
        qBinding.optionD.text = "D. ${question.optionD}"

        val options = listOf(qBinding.optionA, qBinding.optionB, qBinding.optionC, qBinding.optionD)

        options.forEach { button ->
            button.setOnClickListener {
                selectedQuestionForSheet = question
            }
        }

        // Restore state if already answered
        viewLifecycleOwner.lifecycleScope.launch {
            val db = RoomDatabaseClient.getDatabase(requireContext())
            db.answerDao().getAnswersForPaper(question.paperId).collectLatest { savedAnswers ->
                val saved = savedAnswers.find { it.qNumber == question.qNumber }
                saved?.let {
                    val optionIndex = when(it.selectedAnswer) {
                        "A" -> 0; "B" -> 1; "C" -> 2; "D" -> 3; else -> -1
                    }
                    if (optionIndex != -1) {
                        showGradingIndicator(options[optionIndex], it.isCorrect)
                    }
                }
            }
        }

        binding.questionsContainer.addView(qBinding.root)
    }

    private fun handleAnswerSelection(question: QuestionEntity, index: Int) {
        val answers = listOf("A", "B", "C", "D")
        val selectedAnswer = answers[index]
        val isCorrect = selectedAnswer == question.answer
        
        viewLifecycleOwner.lifecycleScope.launch {
            val db = RoomDatabaseClient.getDatabase(requireContext())
            db.answerDao().insertAnswer(
                com.meowreader.client.domain.model.UserAnswerEntity(
                    paperId = question.paperId,
                    qNumber = question.qNumber,
                    selectedAnswer = selectedAnswer,
                    isCorrect = isCorrect,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private fun showGradingIndicator(button: com.google.android.material.button.MaterialButton, isCorrect: Boolean) {
        if (isCorrect) {
            button.setIconResource(R.drawable.ic_launcher)
            button.setIconTintResource(R.color.md_theme_light_primary)
        } else {
            button.setIconResource(android.R.drawable.ic_delete)
            button.setIconTintResource(android.R.color.holo_red_dark)
        }
    }

    private fun setupWordSelection(textView: android.widget.TextView) {
        textView.setTextIsSelectable(true)
        textView.customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
            override fun onCreateActionMode(mode: android.view.ActionMode?, menu: android.view.Menu?): Boolean {
                menu?.add("Lookup in meowReader")
                return true
            }

            override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: android.view.Menu?): Boolean = false
            
            override fun onActionItemClicked(mode: android.view.ActionMode?, item: android.view.MenuItem?): Boolean {
                if (item?.title == "Lookup in meowReader") {
                    val start = textView.selectionStart
                    val end = textView.selectionEnd
                    val word = textView.text.substring(start, end).trim()
                    if (word.isNotEmpty()) {
                        com.meowreader.client.ui.dictionary.DictionaryBottomSheet.newInstance(word).show(parentFragmentManager, "dictionary")
                    }
                    mode?.finish()
                    return true
                }
                return false
            }

            override fun onDestroyActionMode(mode: android.view.ActionMode?) {}
        }
    }

    private fun setupClozeTabs() {
        binding.clozeTabs.visibility = View.VISIBLE
        binding.clozeTabs.removeAllTabs()
        for (i in 1..20) {
            binding.clozeTabs.addTab(binding.clozeTabs.newTab().setText("$i"))
        }

        binding.clozeTabs.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                val qNum = tab?.text?.toString()?.toIntOrNull() ?: 1
                scrollToQuestion(qNum)
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun scrollToQuestion(qNumber: Int) {
        for (i in 0 until binding.questionsContainer.childCount) {
            val view = binding.questionsContainer.getChildAt(i)
            if (i + 1 == qNumber) {
                binding.scrollView.smoothScrollTo(0, view.top)
                break
            }
        }
    }

    fun switchToPaper(paperId: String) {
        viewModel.setPaper(paperId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
