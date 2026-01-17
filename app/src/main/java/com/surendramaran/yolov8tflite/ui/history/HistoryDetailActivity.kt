package com.surendramaran.yolov8tflite.ui.history

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.data.network.ApiService
import com.surendramaran.yolov8tflite.data.network.RetrofitClient
import com.surendramaran.yolov8tflite.databinding.ActivityHistoryDetailBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import io.noties.markwon.Markwon

class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding
    private lateinit var apiService: ApiService
    private var analyticId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        analyticId = intent.getIntExtra("ANALYTIC_ID", -1)
        if (analyticId == -1) {
            Toast.makeText(this, "ID Sesi tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        apiService = RetrofitClient.getInstance(this)
        setupRecyclerView()
        setupToolbar()
        setupButton()
        fetchAllDetails()
    }

    private fun setupButton() {
        binding.btnStartAnalysis.setOnClickListener {
            fetchAiSummaryOnly()
        }
    }

    private fun fetchAiSummaryOnly() {
        binding.progressBarDetail.visibility = View.VISIBLE
        val markwon = Markwon.create(this)

        lifecycleScope.launch {
            try {
                val response = apiService.getAiSummary(analyticId)
                binding.progressBarDetail.visibility = View.GONE

                if (response.isSuccessful) {
                    val summaryText = response.body()?.aiSummary ?: "Tidak ada analisis AI."
                    markwon.setMarkdown(binding.textViewAiSummary, summaryText)
                } else {
                    binding.textViewAiSummary.text = "Gagal memuat analisis AI."
                }
            } catch (e: Exception) {
                binding.progressBarDetail.visibility = View.GONE
                Toast.makeText(this@HistoryDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarHistoryDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Sesi #$analyticId"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        binding.recyclerViewDetail.adapter = HistoryDetailAdapter()
        binding.recyclerViewDetail.layoutManager = GridLayoutManager(this, 2)
    }

    private fun fetchAllDetails() {
        binding.progressBarDetail.visibility = View.VISIBLE
        val markwon = Markwon.create(this)

        lifecycleScope.launch {
            try {
                val imagesDeferred = async { apiService.getImageCapturesForSession(analyticId) }
                val imagesResponse = imagesDeferred.await()

                binding.progressBarDetail.visibility = View.GONE


                if (imagesResponse.isSuccessful && imagesResponse.body()?.success == true) {
                    val images = imagesResponse.body()?.data
                    (binding.recyclerViewDetail.adapter as HistoryDetailAdapter).submitList(images)

                    if (images.isNullOrEmpty()) {
                        binding.textViewNoPhotos.visibility = View.VISIBLE
                        binding.recyclerViewDetail.visibility = View.GONE
                    } else {
                        binding.textViewNoPhotos.visibility = View.GONE
                        binding.recyclerViewDetail.visibility = View.VISIBLE
                    }
                } else {
                    binding.textViewNoPhotos.visibility = View.VISIBLE
                    binding.recyclerViewDetail.visibility = View.GONE
                }

            } catch (e: Exception) {
                binding.progressBarDetail.visibility = View.GONE
                Toast.makeText(this@HistoryDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}