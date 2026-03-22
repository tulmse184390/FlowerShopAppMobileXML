package com.example.flowershopapp.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowershopapp.databinding.ActivityStaffChatRoomListBinding
import com.example.flowershopapp.ui.chat.adapters.StaffChatRoomAdapter

class StaffChatRoomListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStaffChatRoomListBinding
    private val viewModel: StaffChatViewModel by viewModels()
    private lateinit var adapter: StaffChatRoomAdapter
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffChatRoomListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
            .getString("ACCESS_TOKEN", null)

        setupUI()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchChatRooms(token)
    }

    private fun setupUI() {
        adapter = StaffChatRoomAdapter { room ->
            val intent = Intent(this, StaffChatDetailActivity::class.java)
            intent.putExtra("ROOM_ID", room.roomId)
            intent.putExtra("IS_AI_ASSISTED", room.isAIAssisted)
            startActivity(intent)
        }
        binding.rvChatRooms.layoutManager = LinearLayoutManager(this)
        binding.rvChatRooms.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.rooms.observe(this) { roomsList ->
            adapter.submitList(roomsList)
        }

        viewModel.errorMessage.observe(this) { err ->
            if (!err.isNullOrBlank()) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
