package com.example.flowershopapp.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowershopapp.data.api.RetrofitClient
import com.example.flowershopapp.data.model.ChatMessage
import com.example.flowershopapp.data.model.ChatRoomDto
import kotlinx.coroutines.launch

class StaffChatViewModel : ViewModel() {

    private val _rooms = MutableLiveData<List<ChatRoomDto>>()
    val rooms: LiveData<List<ChatRoomDto>> = _rooms

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchChatRooms(token: String?) {
        if (token.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatApi.getChatRooms("Bearer $token")
                if (response.isSuccessful) {
                    _rooms.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Failed to load chat rooms: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun fetchHistory(token: String?, roomId: Int) {
        if (token.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatApi.getHistoryByRoomId("Bearer $token", roomId)
                if (response.isSuccessful) {
                    _messages.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Failed to load history"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun takeoverRoom(token: String?, roomId: Int, onSuccess: () -> Unit) {
        if (token.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatApi.takeoverRoom("Bearer $token", roomId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _errorMessage.value = "Failed to takeover"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun releaseRoom(token: String?, roomId: Int, onSuccess: () -> Unit) {
        if (token.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatApi.releaseRoom("Bearer $token", roomId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _errorMessage.value = "Failed to release"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}
