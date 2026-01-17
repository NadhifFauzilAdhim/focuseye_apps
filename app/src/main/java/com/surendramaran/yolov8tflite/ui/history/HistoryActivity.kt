package com.surendramaran.yolov8tflite.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.BuildConfig
import com.surendramaran.yolov8tflite.data.network.ApiService
import com.surendramaran.yolov8tflite.data.network.RetrofitClient
import com.surendramaran.yolov8tflite.data.prefs.SessionManager
import com.surendramaran.yolov8tflite.databinding.ActivityHistoryBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var apiService: ApiService
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var sessionManager: SessionManager
    private val serverBaseUrl = BuildConfig.BASE_IMG_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = RetrofitClient.getInstance(this)
        sessionManager = SessionManager(this)

        setupToolbar()
        setupUserInfo()
        setupRecyclerView()
        setupSwipeToRefresh()
        fetchHistoryData(isInitialLoad = true)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarHistory)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupUserInfo() {
        val user = sessionManager.fetchUser()
        user?.let {
            val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                in 0..11 -> "Selamat Pagi"
                in 12..17 -> "Selamat Siang"
                else -> "Selamat Malam"
            }
            binding.textViewGreeting.text = "Halo , ${greeting} ! "
            binding.textViewUserName.text = it.name


            if (!it.avatar.isNullOrEmpty()) {
                val fullAvatarUrl = serverBaseUrl + it.avatar
                Glide.with(this).load(fullAvatarUrl).circleCrop().placeholder(R.drawable.default_no_profile).error(R.drawable.default_no_profile).into(binding.imageViewAvatar)
            } else {
                Glide.with(this).load(R.drawable.default_no_profile).circleCrop().into(binding.imageViewAvatar)
            }
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchHistoryData(isInitialLoad = false)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onViewPhotosClicked = { historyItem ->
                val intent = Intent(this, HistoryDetailActivity::class.java).apply {
                    putExtra("ANALYTIC_ID", historyItem.id)
                }
                startActivity(intent)
            },
            onDeleteClicked = { historyItem ->
                showDeleteConfirmationDialog(historyItem.id)
            }
        )
        binding.recyclerViewHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@HistoryActivity)
        }
    }

    private fun fetchHistoryData(isInitialLoad: Boolean) {
        if (isInitialLoad) {
            binding.progressBar.visibility = View.VISIBLE
        }
        binding.textViewNoData.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = apiService.getHistoryList()
                if (response.isSuccessful && response.body()?.success == true) {
                    val historyList = response.body()?.data
                    if (historyList.isNullOrEmpty()) {
                        binding.textViewNoData.visibility = View.VISIBLE
                        historyAdapter.submitList(emptyList())
                    } else {
                        binding.textViewNoData.visibility = View.GONE
                        historyAdapter.submitList(historyList)
                    }
                } else {
                    Toast.makeText(this@HistoryActivity, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
                    binding.textViewNoData.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(this@HistoryActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                binding.textViewNoData.visibility = View.VISIBLE
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun showDeleteConfirmationDialog(historyId: Int) {
        val user = sessionManager.fetchUser()
        val userName = user?.name ?: "Anda"

        val message = "Apakah Anda yakin ingin menghapus sesi analisis milik $userName ini? Aksi ini tidak dapat dibatalkan."

        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Riwayat")
            .setMessage(message)
            .setIcon(R.drawable.baseline_auto_delete_24)
            .setPositiveButton("Ya, Hapus") { _, _ ->
                deleteHistoryItem(historyId)
            }
            .setNegativeButton("Batal", null)
            .setBackground(getDrawable(R.drawable.dialog_background))
            .show()
    }

    private fun deleteHistoryItem(historyId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = apiService.deleteHistoryItem(historyId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@HistoryActivity, "Riwayat berhasil dihapus", Toast.LENGTH_SHORT).show()
                    fetchHistoryData(isInitialLoad = false)
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@HistoryActivity, "Gagal menghapus riwayat", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@HistoryActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}