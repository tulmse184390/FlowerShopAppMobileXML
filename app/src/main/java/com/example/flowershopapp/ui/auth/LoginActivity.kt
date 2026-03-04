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

                startActivity(Intent(this, ProductsListActivity::class.java))
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
}