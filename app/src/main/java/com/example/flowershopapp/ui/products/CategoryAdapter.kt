package com.example.flowershopapp.ui.products

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowershopapp.data.model.CategoryDto
import com.example.flowershopapp.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onCategoryClick: (CategoryDto?) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var categoryList = listOf<CategoryDto>()
    private var selectedPosition = 0

    fun submitList(list: List<CategoryDto>) {
        categoryList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categoryList[position], position)
    }

    override fun getItemCount() = categoryList.size

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: CategoryDto, position: Int) {
            binding.tvCategoryName.text = category.categoryName

            if (position == selectedPosition) {
                binding.tvCategoryName.setTextColor(Color.parseColor("#FF4081"))
            } else {
                binding.tvCategoryName.setTextColor(Color.parseColor("#333333"))
            }

            binding.root.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = position

                notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)

                onCategoryClick(category)
            }
        }
    }
}