package com.surendramaran.yolov8tflite.ui.news

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.data.network.ApiService
import com.surendramaran.yolov8tflite.data.network.RetrofitClient
import com.surendramaran.yolov8tflite.databinding.ActivityNewsBinding
import kotlinx.coroutines.launch

class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding
    private lateinit var apiService: ApiService
    private val newsAdapter = NewsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = RetrofitClient.getInstance(this)

        setupToolbar()
        setupRecyclerView()
        setupSwipeToRefresh()
        fetchNews(isInitialLoad = true)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarNews)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        binding.recyclerViewNews.adapter = newsAdapter
        binding.recyclerViewNews.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayoutNews.setOnRefreshListener {
            fetchNews(isInitialLoad = false)
        }
    }

    private fun fetchNews(isInitialLoad: Boolean) {
        if (isInitialLoad) {
            binding.progressBarNews.visibility = View.VISIBLE
        }
        lifecycleScope.launch {
            try {
                val response = apiService.getNews()
                if (response.isSuccessful) {
                    newsAdapter.submitList(response.body()?.articles)
                } else {
                    Toast.makeText(this@NewsActivity, "Gagal memuat berita", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NewsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBarNews.visibility = View.GONE
                binding.swipeRefreshLayoutNews.isRefreshing = false
            }
        }
    }
}