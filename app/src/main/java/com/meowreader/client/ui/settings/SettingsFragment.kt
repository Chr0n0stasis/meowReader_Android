package com.meowreader.client.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.meowreader.client.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val prefs = requireContext().getSharedPreferences("reader_prefs", Context.MODE_PRIVATE)
        val currentUrl = prefs.getString("backend_url", "https://raw.githubusercontent.com/Chr0n0stasis/meowReader_server/main/")
        binding.urlInput.setText(currentUrl)

        val currentTheme = prefs.getInt("theme_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        when(currentTheme) {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO -> binding.themeToggleGroup.check(com.meowreader.client.R.id.theme_light)
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES -> binding.themeToggleGroup.check(com.meowreader.client.R.id.theme_dark)
            else -> binding.themeToggleGroup.check(com.meowreader.client.R.id.theme_auto)
        }

        binding.themeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val mode = when(checkedId) {
                    com.meowreader.client.R.id.theme_light -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                    com.meowreader.client.R.id.theme_dark -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                    else -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                prefs.edit().putInt("theme_mode", mode).apply()
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
            }
        }

        binding.saveButton.setOnClickListener {
            val newUrl = binding.urlInput.text.toString()
            if (newUrl.startsWith("http")) {
                prefs.edit().putString("backend_url", newUrl).apply()
                Toast.makeText(context, "Settings Updated.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
