package com.example.flowershopapp.ui.products

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.flowershopapp.databinding.ActivityProductsListBinding
import org.json.JSONObject

class ProductsListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductsListBinding
    private val viewModel: ProductsListViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter
    private lateinit var pageAdapter: PageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()

        viewModel.fetchCategories()
        viewModel.fetchProducts(isRefresh = true)
    }

    private fun setupUI() {
        val sharedPref = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("ACCESS_TOKEN", null)
        val userName = decodeTokenToGetName(token)
        binding.tvWelcomeText.text = "Welcome,\n$userName"

        categoryAdapter = CategoryAdapter { selectedCategory ->
            viewModel.currentCategoryId = if (selectedCategory?.categoryId == -1) null else selectedCategory?.categoryId
            viewModel.fetchProducts(isRefresh = true)
        }
        binding.rvCategories.adapter = categoryAdapter

        productAdapter = ProductAdapter { clickedProduct ->
            android.widget.Toast.makeText(this, "Added ${clickedProduct.productName} to cart!", android.widget.Toast.LENGTH_SHORT).show()
        }
        binding.rvProducts.adapter = productAdapter

        pageAdapter = PageAdapter { selectedPage ->
            viewModel.currentPageIndex = selectedPage
            viewModel.fetchProducts(isRefresh = false)
        }
        binding.rvPagination.adapter = pageAdapter

        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }

        binding.edtPriceTo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                applyPriceFilter()
                true
            } else false
        }

        binding.edtPriceFrom.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                applyPriceFilter()
                true
            } else false
        }

        binding.btnApplyFilter.setOnClickListener {
            val fromStr = binding.edtPriceFrom.text.toString().trim()
            val toStr = binding.edtPriceTo.text.toString().trim()

            viewModel.currentMinPrice = fromStr.toDoubleOrNull()
            viewModel.currentMaxPrice = toStr.toDoubleOrNull()
            viewModel.fetchProducts(isRefresh = true) // Lọc mới thì reset về trang 1
        }

        binding.tvSortUp.setOnClickListener {
            viewModel.currentSortBy = "price_asc" // Trùng khớp với Backend C#
            updateSortUI(isUpSelected = true)
            viewModel.fetchProducts(isRefresh = true)
        }

        binding.tvSortDown.setOnClickListener {
            viewModel.currentSortBy = "price_desc" // Trùng khớp với Backend C#
            updateSortUI(isUpSelected = false)
            viewModel.fetchProducts(isRefresh = true)
        }
    }

    private fun performSearch() {
        val query = binding.edtSearch.text.toString().trim()
        viewModel.currentSearch = if (query.isEmpty()) null else query
        viewModel.fetchProducts(isRefresh = true)
    }

    private fun applyPriceFilter() {
        val fromStr = binding.edtPriceFrom.text.toString().trim()
        val toStr = binding.edtPriceTo.text.toString().trim()

        viewModel.currentMinPrice = fromStr.toDoubleOrNull()
        viewModel.currentMaxPrice = toStr.toDoubleOrNull()

        viewModel.fetchProducts(isRefresh = true)
    }

    private fun setupObservers() {
        viewModel.categories.observe(this) { list ->
            categoryAdapter.submitList(list)
        }

        viewModel.productsData.observe(this) { pagedResult ->
            if (pagedResult != null) {
                productAdapter.submitList(pagedResult.items)
                pageAdapter.setPagination(pagedResult.totalPages, pagedResult.currentPage)
            }
        }

        viewModel.errorMessage.observe(this) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun decodeTokenToGetName(token: String?): String {
        if (token.isNullOrEmpty()) return "Guest"
        return try {
            val split = token.split(".")
            if (split.size < 2) return "User"

            val payloadBytes = android.util.Base64.decode(split[1], android.util.Base64.URL_SAFE)
            val payloadString = String(payloadBytes, Charsets.UTF_8)

            val jsonObject = org.json.JSONObject(payloadString)

            if (jsonObject.has("unique_name")) {
                jsonObject.getString("unique_name")
            } else {
                "User"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "User"
        }
    }

    private fun updateSortUI(isUpSelected: Boolean) {
        val pinkColor = android.graphics.Color.parseColor("#FF4081")
        val grayColor = android.graphics.Color.parseColor("#5A5A5A")

        binding.tvSortUp.setTextColor(if (isUpSelected) pinkColor else grayColor)
        binding.tvSortDown.setTextColor(if (!isUpSelected) pinkColor else grayColor)
    }
}