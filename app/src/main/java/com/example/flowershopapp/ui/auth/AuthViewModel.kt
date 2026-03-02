package com.example.flowershopapp.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowershopapp.data.api.RetrofitClient
import com.example.flowershopapp.data.model.LoginRequestDto
import com.example.flowershopapp.data.model.RegisterRequestDto
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

class AuthViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _loginSuccess = MutableLiveData<String?>()
    val loginSuccess: LiveData<String?> get() = _loginSuccess

    fun login(email: String, pass: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val request = LoginRequestDto(userName = email, password = pass)
                val response = RetrofitClient.authApi.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success == true) {
                        _loginSuccess.value = apiResponse.data?.token
                    } else {
                        _errorMessage.value = "${apiResponse?.message}"
                    }
                } else {
                    _errorMessage.value = "Username or password is invalid!"
                }
            } catch (e: IOException) {
                Log.e("LoginActivity", "Network error!", e)

                _errorMessage.value = "Network error"
            } catch (e: Exception) {
                Log.e("LoginActivity", "System error: ${e.message}", e)

                _errorMessage.value = "Server error!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _registerSuccess = MutableLiveData<String?>()
    val registerSuccess: LiveData<String?> get() = _registerSuccess

    fun register(request: RegisterRequestDto) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.authApi.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success == true) {
                        _registerSuccess.value = apiResponse.data?.token
                    } else {
                        _errorMessage.postValue(apiResponse?.message ?: "Sign up failed1!")
                    }
                } else {
                    val errorString = response.errorBody()?.string()
                    if (errorString != null) {
                        try {
                            val jsonObject = JSONObject(errorString)
                            if (jsonObject.has("errors")) {
                                val errorsObj = jsonObject.getJSONObject("errors")
                                val keys = errorsObj.keys()
                                if (keys.hasNext()) {
                                    val firstKey = keys.next()
                                    val errorArray = errorsObj.getJSONArray(firstKey)
                                    _errorMessage.value = errorArray.getString(0)
                                    return@launch
                                }
                            }

                            val errorResponse = com.google.gson.Gson().fromJson(errorString, com.example.flowershopapp.data.model.ApiResponse::class.java)
                            if (errorResponse.message != null) {
                                _errorMessage.value = errorResponse.message
                            } else {
                                _errorMessage.value = "Invalid data!"
                            }

                        } catch (e: Exception) {
                            _errorMessage.value = "Sign up failed!"
                        }
                    } else {
                        _errorMessage.value = "Sign up failed!"
                    }
                }
            } catch (e: IOException) {
                Log.e("RegisterActivity", "Network error!", e)

                _errorMessage.value = "Network error"
            } catch (e: Exception) {
                Log.e("RegisterActivity", "System error: ${e.message}", e)

                _errorMessage.value = "Server error!"
            } finally {
                _isLoading.value = false
            }
        }
    }
}