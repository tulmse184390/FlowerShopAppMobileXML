package com.example.flowershopapp.ui.checkout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.flowershopapp.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val viewModel: CheckoutViewModel by viewModels()
    private var userToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
        userToken = sharedPref.getString("ACCESS_TOKEN", null)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.btnPlaceOrder.setOnClickListener {
            val address = binding.edtAddress.text.toString().trim()
            val paymentMethod = if (binding.rbVnPay.isChecked) "VNPAY" else "COD"

            viewModel.placeOrder(userToken, address, paymentMethod)
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnPlaceOrder.isEnabled = !isLoading
        }

        viewModel.checkoutResult.observe(this) { result ->
            if (result != null) {
                if (!result.paymentUrl.isNullOrEmpty()) {
                    val intent = Intent(this, PaymentWebViewActivity::class.java)
                    intent.putExtra("PAYMENT_URL", result.paymentUrl)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Order successfully!.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }
}