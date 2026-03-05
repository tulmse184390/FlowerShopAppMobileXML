package com.example.flowershopapp.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flowershopapp.data.model.CartItemDto
import com.example.flowershopapp.databinding.ItemCartBinding

class CartAdapter(
    private val onPlusClick: (CartItemDto) -> Unit,
    private val onMinusClick: (CartItemDto) -> Unit,
    private val onDeleteClick: (CartItemDto) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var cartItems = listOf<CartItemDto>()

    fun submitList(list: List<CartItemDto>) {
        cartItems = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount() = cartItems.size

    inner class CartViewHolder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItemDto) {
            binding.tvProductName.text = item.productName
            binding.tvQuantity.text = item.quantity.toString()

            val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
            binding.tvProductPrice.text = "${formatter.format(item.price)} VNĐ"

            Glide.with(binding.root.context)
                .load(item.productImage)
                .into(binding.ivProductImage)

            binding.btnPlus.setOnClickListener { onPlusClick(item) }
            binding.btnMinus.setOnClickListener { onMinusClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }
}