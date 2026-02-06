package com.surendramaran.yolov8tflite.ui.news

import com.surendramaran.yolov8tflite.R

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.surendramaran.yolov8tflite.databinding.ActivityNewsDetailBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NewsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        val articleUrl = intent.getStringExtra("ARTICLE_URL")
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val title = intent.getStringExtra("TITLE")
        val source = intent.getStringExtra("SOURCE")
        val date = intent.getStringExtra("DATE")

        if (articleUrl == null) {
            finish()
            return
        }

        binding.collapsingToolbar.title = source ?: "Berita"
        binding.textViewArticleTitle.text = title ?: "Judul tidak tersedia"
        binding.textViewArticleSource.text = source ?: "Sumber tidak diketahui"
        binding.textViewArticleDate.text = formatDate(date)

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.news_no_image)
            .error(R.drawable.news_no_image)
            .into(binding.imageViewArticleHeader)

        setupWebView(articleUrl)
    }

    private fun formatDate(apiTimestamp: String?): String {
        if (apiTimestamp == null) return "Tanggal tidak tersedia"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val parsedDate = inputFormat.parse(apiTimestamp)
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            parsedDate?.let { outputFormat.format(it) } ?: "Format tanggal tidak valid"
        } catch (e: Exception) {
            "Tanggal tidak tersedia"
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarNewsDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupWebView(url: String) {
        binding.webViewNews.settings.javaScriptEnabled = true
        binding.webViewNews.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBarNewsDetail.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBarNewsDetail.visibility = View.GONE
            }
        }
        binding.webViewNews.loadUrl(url)
    }
}