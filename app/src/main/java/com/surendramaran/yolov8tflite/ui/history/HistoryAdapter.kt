package com.surendramaran.yolov8tflite.ui.history

import com.surendramaran.yolov8tflite.R

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.surendramaran.yolov8tflite.data.model.HistoryItem
import com.surendramaran.yolov8tflite.databinding.ItemHistorySessionBinding
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale
import kotlin.math.roundToInt
import java.time.format.DateTimeFormatter

class HistoryAdapter(
    private val onViewPhotosClicked: (HistoryItem) -> Unit,
    private val onDeleteClicked: (HistoryItem) -> Unit
) : ListAdapter<HistoryItem, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistorySessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding, onViewPhotosClicked, onDeleteClicked)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemHistorySessionBinding,
        private val onViewPhotosClicked: (HistoryItem) -> Unit,
        private val onDeleteClicked: (HistoryItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val context: Context = binding.root.context

        fun bind(item: HistoryItem) {
            binding.buttonViewPhotos.setOnClickListener { onViewPhotosClicked(item) }
            binding.buttonDelete.setOnClickListener { onDeleteClicked(item) }

            binding.textViewTimestamp.text = formatDate(item.createdAt)

            val totalFocusTime = (item.focusDuration + item.unfocusDuration).toFloat()
            val focusPercent = if (totalFocusTime > 0) (item.focusDuration / totalFocusTime * 100).roundToInt() else 0
            val unfocusPercent = 100 - focusPercent

            binding.textViewFocusedLabel.text = "Fokus (${focusPercent}%)"
            binding.textViewUnfocusedLabel.text = "Tdk Fokus (${unfocusPercent}%)"

            setupPieChart(item)
        }

        private fun formatDate(dateString: String): String {
            return try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                    .withZone(ZoneOffset.UTC)

                val instant = Instant.from(formatter.parse(dateString))
                val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
                    .withZone(ZoneId.systemDefault())

                outputFormatter.format(instant)
            } catch (e: Exception) {
                e.printStackTrace()
                dateString
            }
        }

        private fun setupPieChart(item: HistoryItem) {
            val entries = ArrayList<PieEntry>().apply {
                add(PieEntry(item.focusDuration.toFloat()))
                add(PieEntry(item.unfocusDuration.toFloat()))
            }

            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(
                    ContextCompat.getColor(context, R.color.focused_green),
                    ContextCompat.getColor(context, R.color.unfocused_red)
                )
                setDrawValues(false)
            }

            binding.pieChartHistory.apply {
                data = PieData(dataSet)
                description.isEnabled = false
                legend.isEnabled = false
                isRotationEnabled = false
                holeRadius = 75f
                transparentCircleRadius = 80f
                setDrawEntryLabels(false)
                setUsePercentValues(false)
                setHoleColor(ContextCompat.getColor(context, R.color.history_background_light))

                centerText = formatDurationForPie(item.duration)
                setCenterTextTypeface(Typeface.DEFAULT_BOLD)
                setCenterTextColor(ContextCompat.getColor(context, R.color.history_text_primary_dark))
                setCenterTextSize(16f)
                invalidate()
            }
        }

        private fun formatTimestamp(apiTimestamp: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(apiTimestamp)
                val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
                date?.let { outputFormat.format(it) } ?: apiTimestamp
            } catch (e: Exception) {
                apiTimestamp
            }
        }

        private fun formatDurationForPie(totalSeconds: Int): SpannableStringBuilder {
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val durationText = "$minutes\nmenit"
            val builder = SpannableStringBuilder(durationText)
            builder.setSpan(StyleSpan(Typeface.NORMAL), durationText.indexOf("\n"), durationText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return builder
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<HistoryItem>() {
            override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}