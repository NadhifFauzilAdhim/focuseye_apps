package com.surendramaran.yolov8tflite.ui.schedule

import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.data.model.Schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.surendramaran.yolov8tflite.databinding.ItemScheduleBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduleAdapter(
    private val onDeleteClicked: (Schedule) -> Unit
) : ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = getItem(position)
        holder.bind(schedule)
        holder.binding.buttonDeleteSchedule.setOnClickListener {
            onDeleteClicked(schedule)
        }
    }

    class ScheduleViewHolder(val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(schedule: Schedule) {
            binding.textViewScheduleName.text = schedule.name
            binding.textViewScheduleDesc.text = schedule.description

            val detailsText = schedule.details.take(3).joinToString("\n") { detail ->
                val formattedDate = formatDetailDate(detail.startDate)
                val formattedStartTime = formatDetailTime(detail.startDate)
                val formattedEndTime = formatDetailTime(detail.endDate)
                "• ${detail.name} ($formattedDate, ${formattedStartTime} - ${formattedEndTime})"
            }
            binding.textViewScheduleDetailsPreview.text = detailsText
        }

        private fun formatDetailDate(dateTimeString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(dateTimeString)
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                date?.let { outputFormat.format(it) } ?: "Invalid Date"
            } catch (e: Exception) {
                "Invalid Date"
            }
        }

        private fun formatDetailTime(dateTimeString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(dateTimeString)
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                date?.let { outputFormat.format(it) } ?: "--:--"
            } catch (e: Exception) {
                "--:--"
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Schedule>() {
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.uuid == newItem.uuid
            }
            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem == newItem
            }
        }
    }
}