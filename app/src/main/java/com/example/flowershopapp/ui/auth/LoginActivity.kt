package com.example.flowershopapp.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.flowershopapp.databinding.ActivityLoginBinding
import androidx.activity.viewModels
import com.example.flowershopapp.ui.products.ProductsListActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnLogin.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                binding.tvErrorMessage.text = error
                binding.tvErrorMessage.visibility = View.VISIBLE
            } else {
                binding.tvErrorMessage.visibility = View.GONE
            }
        }

        viewModel.loginSuccess.observe(this) { token ->
            if (token != null) {
                val sharedPref = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
                sharedPref.edit().putString("ACCESS_TOKEN", token).apply()

                val role = decodeTokenClaim(token, "role", "http://schemas.microsoft.com/ws/2008/06/identity/claims/role")
                if (role?.uppercase() == "STAFF") {
                    startActivity(Intent(this, com.example.flowershopapp.ui.chat.StaffChatRoomListActivity::class.java))
                } else {
                    startActivity(Intent(this, ProductsListActivity::class.java))
                }
                finish()
            }
        }
    }

    private fun setupListeners() {
        binding.tvSignUpTab.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edtUsername.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.tvErrorMessage.text = "Please enter Username and Password!"
                binding.tvErrorMessage.visibility = View.VISIBLE
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }
    }

    private fun decodeTokenClaim(token: String?, vararg claims: String): String? {
        if (token.isNullOrEmpty()) return null
        return try {
            val split = token.split(".")
            if (split.size < 2) return null
            val payloadBytes = android.util.Base64.decode(split[1], android.util.Base64.URL_SAFE)
            val payloadString = String(payloadBytes, Charsets.UTF_8)
            val payload = org.json.JSONObject(payloadString)
            for (claim in claims) {
                val value = payload.optString(claim)
                if (value.isNotBlank() && value.lowercase() != "null") {
                    return value
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }
}