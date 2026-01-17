package com.surendramaran.yolov8tflite.ui.schedule

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.data.network.ApiService
import com.surendramaran.yolov8tflite.data.network.RetrofitClient
import com.surendramaran.yolov8tflite.data.model.Schedule
import com.surendramaran.yolov8tflite.databinding.ActivityScheduleListBinding
import kotlinx.coroutines.launch

class ScheduleListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleListBinding
    private lateinit var apiService: ApiService
    private lateinit var scheduleAdapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = RetrofitClient.getInstance(this)
        setupToolbar()
        setupRecyclerView()
        setupSwipeToRefresh()

        binding.fabAddSchedule.setOnClickListener {
            startActivity(Intent(this, AddScheduleActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchSchedules(false)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarSchedules)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayoutSchedules.setOnRefreshListener {
            fetchSchedules(true)
        }
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter { schedule ->
            showDeleteConfirmation(schedule)
        }
        binding.recyclerViewSchedules.adapter = scheduleAdapter
        binding.recyclerViewSchedules.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchSchedules(isRefresh: Boolean) {
        if (!isRefresh) binding.progressBarSchedules.visibility = View.VISIBLE
        binding.textViewNoSchedules.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val response = apiService.getSchedules()
                if (response.isSuccessful) {
                    val schedules = response.body()
                    scheduleAdapter.submitList(schedules)
                    if (schedules.isNullOrEmpty()) {
                        binding.textViewNoSchedules.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@ScheduleListActivity, "Gagal memuat jadwal", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ScheduleListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBarSchedules.visibility = View.GONE
                binding.swipeRefreshLayoutSchedules.isRefreshing = false
            }
        }
    }

    private fun showDeleteConfirmation(schedule: Schedule) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Jadwal")
            .setMessage("Anda yakin ingin menghapus jadwal '${schedule.name}'?")
            .setPositiveButton("Hapus") { _, _ -> deleteSchedule(schedule.uuid) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteSchedule(uuid: String) {
        binding.progressBarSchedules.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = apiService.deleteSchedule(uuid)
                if (response.isSuccessful) {
                    Toast.makeText(this@ScheduleListActivity, "Jadwal berhasil dihapus", Toast.LENGTH_SHORT).show()
                    fetchSchedules(true)
                } else {
                    Toast.makeText(this@ScheduleListActivity, "Gagal menghapus jadwal", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ScheduleListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBarSchedules.visibility = View.GONE
            }
        }
    }
}