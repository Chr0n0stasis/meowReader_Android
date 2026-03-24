package com.meowreader.client.ui.reading

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meowreader.client.data.database.AppDatabase
import com.meowreader.client.databinding.FragmentReadingBinding
import com.meowreader.client.databinding.ItemQuestionBinding
import com.meowreader.client.domain.model.QuestionEntity
import com.meowreader.client.R
import android.graphics.Color
import io.noties.markwon.Markwon
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReadingFragment : Fragment() {

    private var _binding: FragmentReadingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReadingViewModel by viewModels {
        val db = RoomDatabaseClient.getDatabase(requireContext())
        ReadingViewModelFactory(db.paperDao())
    }

    private lateinit var markwon: Markwon

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReadingBinding.inflate(inflater, container, false)
        markwon = Markwon.create(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        val answers = listOf("A", "B", "C", "D")

        options.forEachIndexed { index, button ->
            button.setOnClickListener {
                val selectedAnswer = answers[index]
                val isCorrect = selectedAnswer == question.answer
                
                // Save to DB
                viewLifecycleOwner.lifecycleScope.launch {
                    val db = RoomDatabaseClient.getDatabase(requireContext())
                    val paperId = question.paperId
                    
                    db.answerDao().insertAnswer(
                        com.meowreader.client.domain.model.UserAnswerEntity(
                            paperId = paperId,
                            qNumber = question.qNumber,
                            selectedAnswer = selectedAnswer,
                            isCorrect = isCorrect,
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    // Check for completion
                    val allQuestions = viewModel.currentQuestions.first()
                    val userAnswers = db.answerDao().getAnswersForPaper(paperId).first()
                    
                    if (userAnswers.size == allQuestions.size) {
                        // Completed!
                        val correctCount = userAnswers.count { it.isCorrect }
                        val score = (correctCount.toDouble() / allQuestions.size * 100).toInt()
                        val paper = viewModel.currentPaper.first()
                        val rating = score * (paper?.difficultyConstant ?: 1.0)
                        
                        db.historyDao().updateHistory(
                            com.meowreader.client.domain.model.HistoryEntity(
                                paperId = paperId,
                                lastQuestionNumber = question.qNumber,
                                scrollPosition = 0,
                                isCompleted = true,
                                score = score,
                                rating = rating,
                                completionDate = System.currentTimeMillis()
                            )
                        )
                    }
                }

                showGrading(qBinding, button, isCorrect, question.explanation, options)
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
                        showGrading(qBinding, options[optionIndex], it.isCorrect, question.explanation, options)
                    }
                }
            }
        }

        binding.questionsContainer.addView(qBinding.root)
    }

    private fun showGrading(qBinding: ItemQuestionBinding, button: com.google.android.material.button.MaterialButton, isCorrect: Boolean, explanation: String, options: List<com.google.android.material.button.MaterialButton>) {
        if (isCorrect) {
            button.setIconResource(R.drawable.ic_launcher)
            button.setIconTintResource(R.color.md_theme_light_primary)
            button.setTextColor(resources.getColor(R.color.md_theme_light_primary, null))
        } else {
            button.setIconResource(android.R.drawable.ic_delete)
            button.setIconTintResource(android.R.color.holo_red_dark)
            button.setTextColor(Color.RED)
        }
        qBinding.explanationLayout.visibility = View.VISIBLE
        markwon.setMarkdown(qBinding.explanationText, explanation)
        options.forEach { it.isEnabled = false }
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
        // Example: showing tabs for questions 1, 2, 3 as per requirements
        for (i in 1..3) {
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
        // Logic to scroll binding.scrollView to the specific question card
        for (i in 0 until binding.questionsContainer.childCount) {
            val view = binding.questionsContainer.getChildAt(i)
            // Assuming tag or order matches qNumber
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
