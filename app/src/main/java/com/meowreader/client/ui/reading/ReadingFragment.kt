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
                        setupClozeTabs(it.id)
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
                // Instant Grading
                val selectedAnswer = answers[index]
                val isCorrect = selectedAnswer == question.answer
                
                // MD3 Visual Feedback
                if (isCorrect) {
                    button.setIconResource(android.R.drawable.checkbox_on_background)
                } else {
                    button.setIconResource(android.R.drawable.ic_delete)
                }

                // Show Explanation
                qBinding.explanationLayout.visibility = View.VISIBLE
                markwon.setMarkdown(qBinding.explanationText, question.explanation)
                
                // Disable other buttons
                options.forEach { it.isEnabled = false }
            }
        }

        binding.questionsContainer.addView(qBinding.root)
    }

    private fun setupWordSelection(textView: android.widget.TextView) {
        textView.setTextIsSelectable(true)
        textView.customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
            override fun onCreateActionMode(mode: android.view.ActionMode?, menu: android.view.Menu?): Boolean {
                menu?.add("Lookup in meowReader")
                return true
            }

            override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: android.view.Menu?): Boolean = false

            override fun onActionItemClicked(mode: android.view.ActionMode?, item: android.view.Menu): Boolean {
                if (item.title == "Lookup in meowReader") {
                    val start = textView.selectionStart
                    val end = textView.selectionEnd
                    val word = textView.text.substring(start, end).trim()
                    if (word.isNotEmpty()) {
                        DictionaryBottomSheet.newInstance(word).show(parentFragmentManager, "dictionary")
                    }
                    mode?.finish()
                    return true
                }
                return false
            }

            override fun onDestroyActionMode(mode: android.view.ActionMode?) {}
        }
    }

    private fun setupClozeTabs(paperId: String) {
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
        // Logic to scroll binding.scroll_view to the specific question card
        for (i in 0 until binding.questionsContainer.childCount) {
            val view = binding.questionsContainer.getChildAt(i)
            // Assuming tag or order matches qNumber
            if (i + 1 == qNumber) {
                binding.scroll_view.smoothScrollTo(0, view.top)
                break
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
