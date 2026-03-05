package com.example.flowershopapp.ui.cart

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.flowershopapp.databinding.ActivityCartBinding

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter
    private var userToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
        userToken = sharedPref.getString("ACCESS_TOKEN", null)

        setupUI()
        setupObservers()

        viewModel.fetchCart(userToken)
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnClearCart.setOnClickListener {
            viewModel.clearCart(userToken)
        }

        binding.btnCheckout.setOnClickListener {
            Toast.makeText(this, "Tính năng Thanh toán đang được phát triển!", Toast.LENGTH_SHORT).show()
        }

        cartAdapter = CartAdapter(
            onPlusClick = { item ->
                viewModel.updateQuantity(userToken, item.productId, item.quantity + 1)
            },
            onMinusClick = { item ->
                if (item.quantity > 1) {
                    viewModel.updateQuantity(userToken, item.productId, item.quantity - 1)
                } else {
                    viewModel.removeItem(userToken, item.productId)
                }
            },
            onDeleteClick = { item ->
                viewModel.removeItem(userToken, item.productId)
            }
        )
        binding.rvCartItems.adapter = cartAdapter

        binding.btnCheckout.setOnClickListener {
            val intent = android.content.Intent(this, com.example.flowershopapp.ui.checkout.CheckoutActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.cartData.observe(this) { cart ->
            if (cart != null && cart.items.isNotEmpty()) {
                binding.rvCartItems.visibility = View.VISIBLE
                binding.layoutEmptyCart.visibility = View.GONE

                cartAdapter.submitList(cart.items)

                val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
                binding.tvTotalAmount.text = "${formatter.format(cart.totalAmount)} VNĐ"
            } else {
                binding.rvCartItems.visibility = View.GONE
                binding.layoutEmptyCart.visibility = View.VISIBLE
                binding.tvTotalAmount.text = "0 VNĐ"
                cartAdapter.submitList(emptyList())
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }
}