package com.example.flowershopapp.ui.checkout

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flowershopapp.databinding.ActivityPaymentWebViewBinding

class PaymentWebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val paymentUrl = intent.getStringExtra("PAYMENT_URL")

        if (paymentUrl.isNullOrEmpty()) {
            Toast.makeText(this, "Payment link not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupWebView()
        binding.webView.loadUrl(paymentUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.contains("vnp_ResponseCode")) {
                    if (url.contains("vnp_ResponseCode=00")) {
                        val intent = android.content.Intent(this@PaymentWebViewActivity, PaymentSuccessActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@PaymentWebViewActivity, "Payment failed or canceled!", Toast.LENGTH_LONG).show()

                        val intent = android.content.Intent(this@PaymentWebViewActivity, com.example.flowershopapp.ui.products.ProductsListActivity::class.java)
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    return true
                }

                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }
}