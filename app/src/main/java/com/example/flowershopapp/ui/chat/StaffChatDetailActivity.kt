package com.example.flowershopapp.ui.chat

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowershopapp.data.model.ChatMessage
import com.example.flowershopapp.databinding.ActivityStaffChatDetailBinding
import com.example.flowershopapp.ui.chat.adapters.ChatAdapter

class StaffChatDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffChatDetailBinding
    private val staffViewModel: StaffChatViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    private var roomId: Int = -1
    private var token: String? = null
    private var allMessages: MutableList<ChatMessage> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getIntExtra("ROOM_ID", -1)
        val isAIAssisted = intent.getBooleanExtra("IS_AI_ASSISTED", true)

        binding.tvRoomTitle.text = "Room #$roomId"
        binding.switchAiChat.isChecked = isAIAssisted

        token = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
            .getString("ACCESS_TOKEN", null)

        setupUI()
        setupObservers()

        // When staff opens the room, we optionally turn off AI chat or at least let them see history
        staffViewModel.fetchHistory(token, roomId)

        // Connect real-time chat
        chatViewModel.connectToChatHub(token)

        // As soon as staff enters, maybe auto turn off AI chat (Takeover)? 
        // User requested: "Staff enter room meaning turn off AI chat"
        autoTakeOver()
    }

    private fun autoTakeOver() {
        if (binding.switchAiChat.isChecked) {
            staffViewModel.takeoverRoom(token, roomId) {
                binding.switchAiChat.isChecked = false
            }
        }
    }

    private fun setupUI() {
        chatAdapter = ChatAdapter()
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@StaffChatDetailActivity)
            adapter = chatAdapter
        }

        setupSwitchListener()

        binding.btnSend.setOnClickListener {
            val msg = binding.edtMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                chatViewModel.sendMessageToUser(roomId, msg)
                binding.edtMessage.text.clear()
            }
        }
    }

    private fun setupSwitchListener() {
        binding.switchAiChat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                staffViewModel.releaseRoom(token, roomId) {}
            } else {
                staffViewModel.takeoverRoom(token, roomId) {}
            }
        }
    }

    private fun setupObservers() {
        // Observers for history
        staffViewModel.messages.observe(this) { history ->
            allMessages.clear()
            allMessages.addAll(history)
            updateAdapter()
            
            // Now join the real-time group to start receiving live msgs
            chatViewModel.joinRoomGroup(roomId)
        }

        // Observer for live chat
        chatViewModel.messages.observe(this) { liveMsgs ->
            if (liveMsgs.isNotEmpty()) {
                val newLiveMsg = liveMsgs.last()
                allMessages.add(newLiveMsg)
                updateAdapter()
            }
        }

        staffViewModel.errorMessage.observe(this) { err ->
            if (!err.isNullOrBlank()) Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
        }

        chatViewModel.aiChatStatus.observe(this) { isAI ->
            if (binding.switchAiChat.isChecked != isAI) {
                binding.switchAiChat.setOnCheckedChangeListener(null)
                binding.switchAiChat.isChecked = isAI
                setupSwitchListener()
            }
        }
    }

    private fun updateAdapter() {
        chatAdapter.submitList(allMessages.toList())
        if (allMessages.isNotEmpty()) {
            binding.rvMessages.scrollToPosition(allMessages.size - 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chatViewModel.leaveRoomGroup(roomId)
        chatViewModel.disconnectFromChatHub()
    }
}
