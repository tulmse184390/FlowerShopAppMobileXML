package com.example.flowershopapp.ui.orders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.flowershopapp.R
import com.example.flowershopapp.data.api.RetrofitClient
import com.example.flowershopapp.databinding.ActivityOrderHistoryBinding
import com.example.flowershopapp.ui.auth.LoginActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var orderAdapter: OrderHistoryAdapter
    private var accessToken: String = ""

    private val totalFormatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).apply {
        maximumFractionDigits = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = getSharedPreferences("FlowerShopPrefs", Context.MODE_PRIVATE)
            .getString("ACCESS_TOKEN", null)

        if (token.isNullOrBlank()) {
            redirectToLogin()
            return
        }

        accessToken = token

        setupUI()
        fetchOrderHistory()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        orderAdapter = OrderHistoryAdapter { item ->
            fetchOrderDetail(item)
        }
        binding.rvOrders.adapter = orderAdapter
    }

    private fun fetchOrderHistory() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.orderApi.getOrders("Bearer $accessToken")
                if (response.isSuccessful) {
                    val items = parseOrders(response.body())
                    orderAdapter.submitList(items)

                    binding.rvOrders.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
                    binding.tvEmptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                    binding.tvEmptyState.text = getString(R.string.order_history_empty_text)
                } else {
                    showLoadError()
                }
            } catch (_: Exception) {
                showLoadError()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun fetchOrderDetail(item: OrderHistoryItemUi) {
        val orderId = item.orderId
        if (orderId == null) {
            Toast.makeText(this, getString(R.string.order_history_no_id_text), Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.orderApi.getOrderById("Bearer $accessToken", orderId)
                if (response.isSuccessful) {
                    val detailPayload = extractDetailPayload(response.body())
                    showOrderDetailDialog(orderId, detailPayload)
                } else {
                    Toast.makeText(
                        this@OrderHistoryActivity,
                        getString(R.string.order_history_detail_failed_text),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: Exception) {
                Toast.makeText(
                    this@OrderHistoryActivity,
                    getString(R.string.order_history_detail_failed_text),
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun showOrderDetailDialog(orderId: Int, detailPayload: JsonElement?) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.order_history_detail_title, orderId))
            .setMessage(buildOrderDetailMessage(detailPayload))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun buildOrderDetailMessage(detailPayload: JsonElement?): String {
        if (detailPayload == null || detailPayload.isJsonNull) {
            return getString(R.string.order_history_detail_unavailable_text)
        }

        if (!detailPayload.isJsonObject) {
            return GsonBuilder().setPrettyPrinting().create().toJson(detailPayload)
        }

        val detail = detailPayload.asJsonObject

        val status = readString(detail, "status", "orderStatus", "state")
        val orderDateRaw = readString(detail, "orderDate", "createdAt", "createdDate", "createdOn")
        val total = readDouble(detail, "totalAmount", "total", "amount", "grandTotal")
        val paymentMethod = readString(detail, "paymentMethod", "payment", "paymentType")
        val address = readString(detail, "shippingAddress", "address", "deliveryAddress")

        val lines = mutableListOf<String>()
        lines += getString(R.string.order_history_label_status, status ?: "-")
        lines += getString(
            R.string.order_history_label_date,
            if (orderDateRaw.isNullOrBlank()) getString(R.string.order_history_unknown_date_text) else formatDate(orderDateRaw)
        )
        lines += getString(
            R.string.order_history_label_total,
            total?.let { "${totalFormatter.format(it)} VNĐ" }
                ?: getString(R.string.order_history_unknown_total_text)
        )

        if (!paymentMethod.isNullOrBlank()) {
            lines += getString(R.string.order_history_label_payment_method, paymentMethod)
        }

        if (!address.isNullOrBlank()) {
            lines += getString(R.string.order_history_label_address, address)
        }

        return lines.joinToString("\n")
    }

    private fun parseOrders(responseBody: JsonElement?): List<OrderHistoryItemUi> {
        val ordersArray = extractOrdersArray(responseBody)
        if (ordersArray.size() == 0) {
            return emptyList()
        }

        return ordersArray.mapNotNull { element ->
            if (!element.isJsonObject) return@mapNotNull null

            val orderObject = element.asJsonObject
            val orderId = readInt(orderObject, "orderId", "id")
            val status = readString(orderObject, "status", "orderStatus", "state")
                ?: getString(R.string.order_history_unknown_status_text)

            val orderDateRaw = readString(
                orderObject,
                "orderDate",
                "createdAt",
                "createdDate",
                "createdOn"
            )
            val orderDateText = formatDate(orderDateRaw)

            val total = readDouble(orderObject, "totalAmount", "total", "amount", "grandTotal")
            val totalText = total?.let { "${totalFormatter.format(it)} VNĐ" }
                ?: getString(R.string.order_history_unknown_total_text)

            OrderHistoryItemUi(
                orderId = orderId,
                status = status,
                orderDateText = orderDateText,
                totalText = totalText
            )
        }
    }

    private fun extractOrdersArray(root: JsonElement?): JsonArray {
        if (root == null || root.isJsonNull) return JsonArray()
        if (root.isJsonArray) return root.asJsonArray
        if (!root.isJsonObject) return JsonArray()

        val rootObject = root.asJsonObject

        extractJsonArray(rootObject.get("data"))?.let { return it }
        extractJsonArray(rootObject.get("items"))?.let { return it }
        extractJsonArray(rootObject.get("orders"))?.let { return it }
        extractJsonArray(rootObject.get("result"))?.let { return it }

        val data = rootObject.get("data")
        if (data != null && data.isJsonObject) {
            val dataObject = data.asJsonObject
            extractJsonArray(dataObject.get("items"))?.let { return it }
            extractJsonArray(dataObject.get("orders"))?.let { return it }
            extractJsonArray(dataObject.get("results"))?.let { return it }
            extractJsonArray(dataObject.get("data"))?.let { return it }
        }

        return JsonArray()
    }

    private fun extractDetailPayload(root: JsonElement?): JsonElement? {
        if (root == null || root.isJsonNull) return null
        if (!root.isJsonObject) return root

        val rootObject = root.asJsonObject
        val data = rootObject.get("data")
        if (data != null && !data.isJsonNull) {
            return data
        }
        return root
    }

    private fun extractJsonArray(element: JsonElement?): JsonArray? {
        if (element == null || element.isJsonNull) return null
        if (element.isJsonArray) return element.asJsonArray
        if (!element.isJsonObject) return null

        val obj = element.asJsonObject
        val nestedKeys = listOf("items", "orders", "results", "data")
        for (key in nestedKeys) {
            val nested = obj.get(key)
            if (nested != null && nested.isJsonArray) {
                return nested.asJsonArray
            }
        }
        return null
    }

    private fun formatDate(rawDate: String?): String {
        if (rawDate.isNullOrBlank()) {
            return getString(R.string.order_history_unknown_date_text)
        }

        val outputFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

        val parsedDate = runCatching {
            OffsetDateTime.parse(rawDate).toLocalDateTime()
        }.recoverCatching {
            Instant.parse(rawDate).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }.recoverCatching {
            LocalDateTime.parse(rawDate)
        }.getOrNull()

        return parsedDate?.format(outputFormat) ?: rawDate
    }

    private fun readString(json: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            val value = json.get(key) ?: continue
            if (!value.isJsonPrimitive) continue

            val primitive = value.asJsonPrimitive
            if (primitive.isString) return primitive.asString
            if (primitive.isNumber || primitive.isBoolean) return primitive.toString()
        }
        return null
    }

    private fun readInt(json: JsonObject, vararg keys: String): Int? {
        for (key in keys) {
            val value = json.get(key) ?: continue
            if (!value.isJsonPrimitive) continue

            val primitive = value.asJsonPrimitive
            val parsed = runCatching {
                when {
                    primitive.isNumber -> primitive.asInt
                    primitive.isString -> primitive.asString.toIntOrNull()
                    else -> null
                }
            }.getOrNull()

            if (parsed != null) {
                return parsed
            }
        }
        return null
    }

    private fun readDouble(json: JsonObject, vararg keys: String): Double? {
        for (key in keys) {
            val value = json.get(key) ?: continue
            if (!value.isJsonPrimitive) continue

            val primitive = value.asJsonPrimitive
            val parsed = runCatching {
                when {
                    primitive.isNumber -> primitive.asDouble
                    primitive.isString -> primitive.asString.toDoubleOrNull()
                    else -> null
                }
            }.getOrNull()

            if (parsed != null) {
                return parsed
            }
        }
        return null
    }

    private fun showLoadError() {
        binding.rvOrders.visibility = View.GONE
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.tvEmptyState.text = getString(R.string.order_history_load_failed_text)
        Toast.makeText(this, getString(R.string.order_history_load_failed_text), Toast.LENGTH_SHORT)
            .show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun redirectToLogin() {
        Toast.makeText(this, getString(R.string.order_history_session_expired_text), Toast.LENGTH_SHORT)
            .show()

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
