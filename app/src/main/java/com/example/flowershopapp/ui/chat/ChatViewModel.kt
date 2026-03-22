package com.example.flowershopapp.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowershopapp.data.model.ChatMessage
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.launch
import com.example.flowershopapp.data.api.RetrofitClient

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> get() = _messages

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _aiChatStatus = MutableLiveData<Boolean>()
    val aiChatStatus: LiveData<Boolean> get() = _aiChatStatus

    private lateinit var hubConnection: HubConnection

    fun loadMyHistory(token: String?) {
        if (token.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatApi.getMyHistory("Bearer $token")
                if (response.isSuccessful) {
                    _messages.postValue(response.body() ?: emptyList())
                } else {
                    _errorMessage.postValue("Failed to load history")
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            }
        }
    }

    fun connectToChatHub(token: String?) {
        val builder = HubConnectionBuilder.create("http://10.0.2.2:5175/chatHub")

        if (!token.isNullOrBlank()) {
            builder.withAccessTokenProvider(Single.defer { Single.just(token) })
        }

        hubConnection = builder.build()

        hubConnection.on("ReceiveMessage", { user, message ->
            val newMessage = ChatMessage(user, message)
            _messages.postValue(_messages.value?.plus(newMessage))
        }, String::class.java, String::class.java)

        hubConnection.on("ReceiveToggleAI", { isAI ->
            _aiChatStatus.postValue(isAI)
        }, Boolean::class.javaObjectType)

        hubConnection.onClosed { error ->
            if (error != null) {
                _errorMessage.postValue("Chat connection closed: ${error.message}")
            }
        }

        viewModelScope.launch {
            try {
                hubConnection.start().blockingAwait()
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to connect to chat server: ${e.message}")
            }
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            try {
                if (!::hubConnection.isInitialized || hubConnection.connectionState != HubConnectionState.CONNECTED) {
                    _errorMessage.postValue("Chat is not connected")
                    return@launch
                }

                // AI path: SendMessageToShop → backend calls Groq and replies automatically
                hubConnection.send("SendMessageToShop", message)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to send message: ${e.message}")
            }
        }
    }

    fun sendMessageToStaff(message: String) {
        viewModelScope.launch {
            try {
                if (!::hubConnection.isInitialized || hubConnection.connectionState != HubConnectionState.CONNECTED) {
                    _errorMessage.postValue("Chat is not connected")
                    return@launch
                }

                // Staff path: SendMessageToStaff → no AI reply, waits for human staff
                hubConnection.send("SendMessageToStaff", message)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to send message: ${e.message}")
            }
        }
    }

    fun sendMessageToUser(roomId: Int, message: String) {
        viewModelScope.launch {
            try {
                if (!::hubConnection.isInitialized || hubConnection.connectionState != HubConnectionState.CONNECTED) {
                    _errorMessage.postValue("Chat is not connected")
                    return@launch
                }
                hubConnection.send("SendMessageToUser", roomId, message)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to send message: ${e.message}")
            }
        }
    }

    fun joinRoomGroup(roomId: Int) {
        viewModelScope.launch {
            try {
                if (::hubConnection.isInitialized && hubConnection.connectionState == HubConnectionState.CONNECTED) {
                    hubConnection.send("JoinRoomGroup", roomId)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to join room: ${e.message}")
            }
        }
    }

    fun leaveRoomGroup(roomId: Int) {
        viewModelScope.launch {
            try {
                if (::hubConnection.isInitialized && hubConnection.connectionState == HubConnectionState.CONNECTED) {
                    hubConnection.send("LeaveRoomGroup", roomId)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to leave room: ${e.message}")
            }
        }
    }


    fun disconnectFromChatHub() {
        if (::hubConnection.isInitialized && hubConnection.connectionState != HubConnectionState.DISCONNECTED) {
            hubConnection.stop()
        }
    }
}