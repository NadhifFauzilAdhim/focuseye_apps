package com.surendramaran.yolov8tflite.ui.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.data.network.ApiService
import com.surendramaran.yolov8tflite.data.network.RetrofitClient
import com.surendramaran.yolov8tflite.data.model.CreateScheduleRequest
import com.surendramaran.yolov8tflite.data.model.ScheduleDetailRequest
import com.surendramaran.yolov8tflite.databinding.ActivityAddScheduleBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddScheduleBinding
    private lateinit var apiService: ApiService
    private lateinit var formAdapter: ScheduleDetailFormAdapter
    private val scheduleDetailsList = mutableListOf<ScheduleDetailRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = RetrofitClient.getInstance(this)
        setupToolbar()
        setupRecyclerView()

        if (scheduleDetailsList.isEmpty()) {
            addNewDetailItem()
        }
        binding.buttonAddDetail.setOnClickListener {
            addNewDetailItem()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarAddSchedule)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_schedule_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveSchedule()
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun setupRecyclerView() {
        formAdapter = ScheduleDetailFormAdapter(
            scheduleDetailsList,
            onItemRemoved = { position ->
                scheduleDetailsList.removeAt(position)
                formAdapter.notifyItemRemoved(position)
                formAdapter.notifyItemRangeChanged(position, scheduleDetailsList.size)
            },
            onDateTimeClicked = { position, isStartDate ->
                showDateTimePicker(position, isStartDate)
            }
        )
        binding.recyclerViewScheduleDetails.apply {
            adapter = formAdapter
            layoutManager = LinearLayoutManager(this@AddScheduleActivity)
        }
    }

    private fun addNewDetailItem() {
        scheduleDetailsList.add(
            ScheduleDetailRequest(name = "", description = "", startDate = "", endDate = "")
        )
        formAdapter.notifyItemInserted(scheduleDetailsList.size - 1)
    }

    private fun showDateTimePicker(position: Int, isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedDateTime = sdf.format(calendar.time)

                if (isStartDate) {
                    scheduleDetailsList[position] = scheduleDetailsList[position].copy(startDate = formattedDateTime)
                } else {
                    scheduleDetailsList[position] = scheduleDetailsList[position].copy(endDate = formattedDateTime)
                }
                formAdapter.notifyItemChanged(position)

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveSchedule() {
        val scheduleName = binding.editTextScheduleName.text.toString().trim()
        val scheduleDesc = binding.editTextScheduleDesc.text.toString().trim()

        if (scheduleName.isEmpty()) {
            Toast.makeText(this, "Nama jadwal tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        if (scheduleDetailsList.isEmpty()) {
            Toast.makeText(this, "Detail mata kuliah tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        if (scheduleDetailsList.any { it.name.isEmpty() || it.startDate.isEmpty() || it.endDate.isEmpty() }) {
            Toast.makeText(this, "Nama, waktu mulai, dan waktu selesai di setiap mata kuliah harus diisi", Toast.LENGTH_LONG).show()
            return
        }

        val request = CreateScheduleRequest(
            name = scheduleName,
            description = scheduleDesc,
            details = scheduleDetailsList
        )

        lifecycleScope.launch {
            try {
                val response = apiService.createSchedule(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AddScheduleActivity, "Jadwal berhasil dibuat", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddScheduleActivity, "Gagal membuat jadwal", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddScheduleActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}