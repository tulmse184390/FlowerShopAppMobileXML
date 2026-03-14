package com.example.flowershopapp.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowershopapp.databinding.ItemOrderHistoryBinding

class OrderHistoryAdapter(
    private val onItemClick: (OrderHistoryItemUi) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    private var items: List<OrderHistoryItemUi> = emptyList()

    fun submitList(newItems: List<OrderHistoryItemUi>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class OrderHistoryViewHolder(
        private val binding: ItemOrderHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderHistoryItemUi) {
            val orderIdText = item.orderId?.toString() ?: "-"
            binding.tvOrderId.text = "#${orderIdText}"
            binding.tvOrderStatus.text = item.status
            binding.tvOrderDate.text = item.orderDateText
            binding.tvOrderTotal.text = item.totalText

            binding.root.setOnClickListener {
                onItemClick(item)
            }
            binding.tvViewDetail.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
