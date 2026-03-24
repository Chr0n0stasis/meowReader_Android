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
            
            val context = binding.root.context
            val typedValue = android.util.TypedValue()
            
            if (isSelected) {
                binding.activeIndicator.visibility = View.VISIBLE
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
                binding.articleTitle.setTextColor(typedValue.data)
                binding.articleTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                binding.activeIndicator.visibility = View.INVISIBLE
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true)
                binding.articleTitle.setTextColor(typedValue.data)
                binding.articleTitle.setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            binding.root.setOnClickListener { onArticleClick(paper) }
        }
    }
}
