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

        viewModel.connectToChatHub(userToken)
    }

    private fun setupUI() {
        chatAdapter = ChatAdapter()
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        // Update status label when mode changes
        binding.rgChatMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbAi.id -> binding.tvChatModeStatus.text = "🤖 Chatting with AI"
                binding.rbStaff.id -> binding.tvChatModeStatus.text = "👤 Waiting for staff"
            }
        }

        binding.btnSend.setOnClickListener {
            val message = binding.edtMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                if (binding.rbAi.isChecked) {
                    viewModel.sendMessage(message)         // AI path → SendMessageToShop
                } else {
                    viewModel.sendMessageToStaff(message)  // Staff path → SendMessageToStaff
                }
                binding.edtMessage.text.clear()
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(this) { messages ->
            chatAdapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnectFromChatHub()
    }
}