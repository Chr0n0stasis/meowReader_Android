package com.meowreader.client.ui.dictionary

import android.os.Bundle
import android.view.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meowreader.client.databinding.BottomSheetDictionaryBinding

class DictionaryBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDictionaryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetDictionaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val word = arguments?.getString("word") ?: "Unknown"
        binding.wordText.text = word
        binding.phoneticText.text = "/ˈ.../" 
        binding.definitionText.text = "Definition for $word in postgraduate syllabus..."
    }

    companion object {
        fun newInstance(word: String): DictionaryBottomSheet {
            val fragment = DictionaryBottomSheet()
            val args = Bundle()
            args.putString("word", word)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
