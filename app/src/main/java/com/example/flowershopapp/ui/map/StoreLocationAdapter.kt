package com.example.flowershopapp.ui.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowershopapp.data.model.StoreLocationDto
import com.example.flowershopapp.databinding.ItemStoreLocationBinding

class StoreLocationAdapter(
    private val onStoreClick: (StoreLocationDto) -> Unit,
    private val onDirectionsClick: (StoreLocationDto) -> Unit
) : RecyclerView.Adapter<StoreLocationAdapter.StoreViewHolder>() {

    private val items = mutableListOf<StoreLocationDto>()
    private var selectedLocationId: Int? = null

    fun submitList(newItems: List<StoreLocationDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun selectStore(locationId: Int?) {
        selectedLocationId = locationId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemStoreLocationBinding.inflate(inflater, parent, false)
        return StoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class StoreViewHolder(
        private val binding: ItemStoreLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StoreLocationDto) {
            binding.tvStoreName.text = item.storeName ?: "Store"
            binding.tvStoreAddress.text = item.address ?: "No address available"

            val isSelected = item.locationId == selectedLocationId
            val strokeColor = if (isSelected) android.graphics.Color.parseColor("#F4487D")
            else android.graphics.Color.parseColor("#EAEAEA")
            binding.root.strokeColor = strokeColor
            binding.root.strokeWidth = if (isSelected) 2 else 1

            binding.root.setOnClickListener { onStoreClick(item) }
            binding.btnDirections.setOnClickListener { onDirectionsClick(item) }
        }
    }
}
