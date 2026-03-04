package com.example.flowershopapp.ui.products

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.flowershopapp.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val viewModel: ProductDetailViewModel by viewModels()

    private var currentQuantity = 1
    private var maxStock = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        if (productId != -1) {
            viewModel.fetchProductDetail(productId)
        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.productDetail.observe(this) { product ->
            if (product != null) {
                binding.tvProductName.text = product.productName

                val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
                binding.tvProductPrice.text = "${formatter.format(product.price)} VNĐ"

                binding.tvStock.text = "In stock: ${product.stockQuantity}"
                binding.tvFullDescription.text = product.fullDescription ?: "Không có mô tả."

                maxStock = product.stockQuantity

                if (product.images.isNotEmpty()) {
                    val sliderAdapter = ImageSliderAdapter(product.images)
                    binding.vpProductImages.adapter = sliderAdapter
                }
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnMinus.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                binding.tvQuantity.text = currentQuantity.toString()
            }
        }

        binding.btnPlus.setOnClickListener {
            if (currentQuantity < maxStock) {
                currentQuantity++
                binding.tvQuantity.text = currentQuantity.toString()
            } else {
                Toast.makeText(this, "Inventory limit has been reached!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddToCart.setOnClickListener {
            Toast.makeText(this, "Added $currentQuantity to cart!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}