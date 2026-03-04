package com.example.flowershopapp.ui.products

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flowershopapp.data.model.ProductDto
import com.example.flowershopapp.databinding.ItemProductBinding

class ProductAdapter(
    private val onItemClick: (ProductDto) -> Unit,
    private val onAddToCartClick: (ProductDto) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var productList = listOf<ProductDto>()

    fun submitList(list: List<ProductDto>) {
        productList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    override fun getItemCount() = productList.size

    inner class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductDto) {
            binding.tvProductName.text = product.productName
            val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
            binding.tvProductPrice.text = "${formatter.format(product.price)} VNĐ"
            binding.tvBriefDescription.text = product.briefDescription ?: "No description"

            Glide.with(binding.root.context)
                .load(product.imageUrl)
                .into(binding.ivProductImage)

            binding.root.setOnClickListener {
                onItemClick(product)
            }

            binding.btnAddToCart.setOnClickListener {
                onAddToCartClick(product)
            }
        }
    }
}