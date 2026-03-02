package com.example.flowershopapp.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.flowershopapp.MainActivity
import com.example.flowershopapp.data.model.RegisterRequestDto
import com.example.flowershopapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSignUp.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                binding.tvErrorMessage.text = error
                binding.tvErrorMessage.visibility = View.VISIBLE
            } else {
                binding.tvErrorMessage.visibility = View.GONE
            }
        }
        viewModel.registerSuccess.observe(this) { token ->
            if (token != null) {
                val sharedPref = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
                sharedPref.edit().putString("ACCESS_TOKEN", token).apply()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun setupListeners() {
        binding.tvSignInTab.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val username = binding.edtUsername.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val confirmPass = binding.edtConfirmPassword.text.toString().trim()
            val fullName = binding.edtFullName.text.toString().trim()
            val phone = binding.edtPhoneNumber.text.toString().trim()
            val address = binding.edtAddress.text.toString().trim()

            binding.tvErrorMessage.visibility = View.GONE

            if (username.isEmpty() || fullName.isEmpty() || password.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                binding.tvErrorMessage.text = "Please fill in all the information!"
                binding.tvErrorMessage.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password != confirmPass) {
                binding.tvErrorMessage.text = "The verification password doesn't match!"
                binding.tvErrorMessage.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val request = RegisterRequestDto(
                userName = username,
                password = password,
                fullName = fullName,
                email = email,
                phoneNumber = phone,
                address = address
            )

            viewModel.register(request)
        }
    }
}