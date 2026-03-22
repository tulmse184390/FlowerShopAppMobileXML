package com.example.flowershopapp.ui.chat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowershopapp.databinding.ActivityChatBinding
import com.example.flowershopapp.ui.chat.adapters.ChatAdapter

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    private var userToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userToken = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE)
            .getString("ACCESS_TOKEN", null)

        setupUI()
        setupObservers()

        viewModel.loadMyHistory(userToken)
        viewModel.connectToChatHub(userToken)
    }

    private fun setupUI() {
        chatAdapter = ChatAdapter()
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        // Update status label when mode changes
        binding.switchAiChat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvChatModeStatus.text = "Chatting with AI"
            } else {
                binding.tvChatModeStatus.text = "Waiting for staff"
            }
        }

        binding.btnSend.setOnClickListener {
            val message = binding.edtMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                if (binding.switchAiChat.isChecked) {
                    viewModel.sendMessage(message)
                } else {
                    viewModel.sendMessageToStaff(message)
                }
                binding.edtMessage.text.clear()
            }
            // Silently ignore if empty (prevents double-tap showing toast)
        }

        // Auto-scroll when keyboard opens
        binding.rvMessages.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom && chatAdapter.itemCount > 0) {
                binding.rvMessages.postDelayed({
                    binding.rvMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
                }, 100)
            }
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(this) { messages ->
            chatAdapter.submitList(messages) {
                if (messages.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.aiChatStatus.observe(this) { isAI ->
            if (binding.switchAiChat.isChecked != isAI) {
                binding.switchAiChat.isChecked = isAI
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnectFromChatHub()
    }
}
