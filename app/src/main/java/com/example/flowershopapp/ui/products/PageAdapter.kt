package com.example.flowershopapp.ui.products

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowershopapp.databinding.ItemPageBinding

class PageAdapter(
    private val onPageClick: (Int) -> Unit
) : RecyclerView.Adapter<PageAdapter.PageViewHolder>() {

    private var totalPages = 0
    private var currentPage = 1

    fun setPagination(total: Int, current: Int) {
        this.totalPages = total
        this.currentPage = current
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val pageNumber = position + 1
        holder.bind(pageNumber)
    }

    override fun getItemCount() = totalPages

    inner class PageViewHolder(private val binding: ItemPageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pageNumber: Int) {
            binding.tvPageNumber.text = pageNumber.toString()

            if (pageNumber == currentPage) {
                binding.tvPageNumber.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FF4081"))
                binding.tvPageNumber.setTextColor(Color.WHITE)
            } else {
                binding.tvPageNumber.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E6E6E6"))
                binding.tvPageNumber.setTextColor(Color.parseColor("#333333"))
            }

            binding.root.setOnClickListener {
                if (pageNumber != currentPage) {
                    onPageClick(pageNumber)
                }
            }
        }
    }
}