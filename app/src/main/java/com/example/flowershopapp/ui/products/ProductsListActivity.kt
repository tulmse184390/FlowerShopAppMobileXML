package com.example.flowershopapp.ui.products

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.flowershopapp.data.api.RetrofitClient
import com.example.flowershopapp.databinding.ActivityProductsListBinding
import com.example.flowershopapp.ui.chat.ChatViewModel
import com.example.flowershopapp.ui.chat.adapters.ChatAdapter
import com.example.flowershopapp.utils.CartBadgeHelper
import kotlinx.coroutines.launch
import org.json.JSONObject
import androidx.recyclerview.widget.LinearLayoutManager

class ProductsListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductsListBinding
    private val viewModel: ProductsListViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter
    private lateinit var pageAdapter: PageAdapter
    private lateinit var chatAdapter: ChatAdapter

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CartBadgeHelper.createNotificationChannel(this)
        requestNotificationPermission()

        setupUI()
        setupObservers()
        setupFloatingChat()

        viewModel.fetchCategories()
        viewModel.fetchProducts(isRefresh = true)
    }

    override fun onResume() {
        super.onResume()
        fetchCartBadge()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun fetchCartBadge() {
        val sharedPref = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("ACCESS_TOKEN", null) ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.cartApi.getMyCart("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val count = response.body()?.data?.items?.size ?: 0
                    CartBadgeHelper.updateBadge(binding.tvCartBadge, count)
                }
            } catch (_: Exception) { }
        }
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

        productAdapter = ProductAdapter(
            onItemClick = { clickedProduct ->
                val intent = android.content.Intent(this, ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", clickedProduct.productId)
                startActivity(intent)
            },
            onAddToCartClick = { clickedProduct ->
                val sharedPref = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
                val token = sharedPref.getString("ACCESS_TOKEN", null)
                viewModel.addToCart(token, clickedProduct.productId, 1)
            }
        )

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

        binding.btnCart.setOnClickListener {
            val intent = android.content.Intent(this, com.example.flowershopapp.ui.cart.CartActivity::class.java)
            startActivity(intent)
        }

        binding.btnMap.setOnClickListener {
            val intent = android.content.Intent(this, com.example.flowershopapp.ui.map.MapActivity::class.java)
            startActivity(intent)
        }

        binding.btnFloatingChat.setOnClickListener {
            showFloatingChat()
        }
    }

    private fun setupFloatingChat() {
        val sharedPref = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("ACCESS_TOKEN", null)

        chatAdapter = ChatAdapter()
        binding.rvFloatingMessages.apply {
            layoutManager = LinearLayoutManager(this@ProductsListActivity)
            adapter = chatAdapter
        }

        chatViewModel.connectToChatHub(token)

        binding.chatOutsideOverlay.setOnClickListener {
            hideFloatingChat()
        }

        binding.btnMinimizeChat.setOnClickListener {
            hideFloatingChat()
        }

        binding.chatFloatingPanel.setOnClickListener {
            // Consume panel clicks so only outside clicks minimize chat.
        }

        binding.btnSendFloatingMessage.setOnClickListener {
            sendFloatingMessage()
        }

        binding.edtFloatingMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendFloatingMessage()
                true
            } else {
                false
            }
        }

        // Update status label when customer switches chat mode
        binding.rgChatMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbAi.id -> binding.tvChatModeStatus.text = "🤖 Chatting with AI"
                binding.rbStaff.id -> binding.tvChatModeStatus.text = "👤 Waiting for staff"
            }
        }

        chatViewModel.messages.observe(this) { messages ->
            chatAdapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.rvFloatingMessages.scrollToPosition(messages.size - 1)
            }
        }

        chatViewModel.errorMessage.observe(this) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendFloatingMessage() {
        val message = binding.edtFloatingMessage.text.toString().trim()
        if (message.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.rbAi.isChecked) {
            chatViewModel.sendMessage(message)       // AI path → SendMessageToShop
        } else {
            chatViewModel.sendMessageToStaff(message) // Staff path → SendMessageToStaff
        }

        binding.edtFloatingMessage.text.clear()
    }

    private fun showFloatingChat() {
        binding.chatOutsideOverlay.visibility = android.view.View.VISIBLE
        binding.chatFloatingPanel.visibility = android.view.View.VISIBLE
        binding.btnFloatingChat.visibility = android.view.View.GONE
    }

    private fun hideFloatingChat() {
        binding.chatOutsideOverlay.visibility = android.view.View.GONE
        binding.chatFloatingPanel.visibility = android.view.View.GONE
        binding.btnFloatingChat.visibility = android.view.View.VISIBLE
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

        viewModel.addToCartSuccess.observe(this) { message ->
            if (message != null) {
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
                fetchCartBadge()
                showCartNotificationAfterAdd()
            }
        }
    }

    private fun showCartNotificationAfterAdd() {
        val sharedPref = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("ACCESS_TOKEN", null) ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.cartApi.getMyCart("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val count = response.body()?.data?.items?.size ?: 0
                    CartBadgeHelper.showCartNotification(this@ProductsListActivity, count)
                }
            } catch (_: Exception) { }
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

    override fun onDestroy() {
        super.onDestroy()
        chatViewModel.disconnectFromChatHub()
    }
}