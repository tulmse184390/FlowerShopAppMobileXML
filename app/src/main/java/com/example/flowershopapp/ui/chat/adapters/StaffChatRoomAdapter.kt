package com.example.flowershopapp.ui.chat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowershopapp.data.model.ChatRoomDto
import com.example.flowershopapp.databinding.ItemChatRoomBinding

class StaffChatRoomAdapter(
    private val onItemClick: (ChatRoomDto) -> Unit
) : RecyclerView.Adapter<StaffChatRoomAdapter.ViewHolder>() {

    private var rooms: List<ChatRoomDto> = emptyList()

    fun submitList(list: List<ChatRoomDto>) {
        rooms = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount(): Int = rooms.size

    class ViewHolder(
        private val binding: ItemChatRoomBinding,
        private val onItemClick: (ChatRoomDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(room: ChatRoomDto) {
            // Name would be fine. If Name is null, default
            binding.tvRoomName.text = room.name ?: "Room #${room.roomId}"
            binding.tvAiStatus.text = if (room.isAIAssisted) "AI is on" else "Waiting for Staff..."
            binding.tvAiStatus.setTextColor(
                if (room.isAIAssisted) android.graphics.Color.GREEN else android.graphics.Color.parseColor("#FF4081")
            )
            binding.root.setOnClickListener {
                onItemClick(room)
            }
        }
    }
}
