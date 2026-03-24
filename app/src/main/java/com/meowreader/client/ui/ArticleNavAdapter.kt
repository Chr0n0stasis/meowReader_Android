package com.meowreader.client.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meowreader.client.databinding.ItemNavArticleBinding
import com.meowreader.client.domain.model.PaperEntity

class ArticleNavAdapter(
    private val onArticleClick: (PaperEntity) -> Unit
) : RecyclerView.Adapter<ArticleNavAdapter.ViewHolder>() {

    private var items: List<PaperEntity> = emptyList()
    private var selectedPaperId: String? = null

    fun submitList(newItems: List<PaperEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun setSelectedId(id: String?) {
        selectedPaperId = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNavArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paper = items[position]
        holder.bind(paper, paper.id == selectedPaperId)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemNavArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(paper: PaperEntity, isSelected: Boolean) {
            binding.articleTitle.text = paper.title
            binding.root.isActivated = isSelected
            
            val context = binding.root.context
            val typedValue = android.util.TypedValue()
            
            if (isSelected) {
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondaryContainer, typedValue, true)
                val color = typedValue.data
                binding.articleTitle.setTextColor(color)
                binding.articleIcon.setColorFilter(color)
                binding.articleTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true)
                val color = typedValue.data
                binding.articleTitle.setTextColor(color)
                binding.articleIcon.setColorFilter(color)
                binding.articleTitle.setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            binding.root.setOnClickListener { onArticleClick(paper) }
        }
    }
}
