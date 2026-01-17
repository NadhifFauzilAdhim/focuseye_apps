package com.surendramaran.yolov8tflite.ui.history

import com.surendramaran.yolov8tflite.R

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surendramaran.yolov8tflite.BuildConfig
import com.surendramaran.yolov8tflite.data.model.ImageCaptureItem
import com.surendramaran.yolov8tflite.databinding.ItemUnfocusedCaptureBinding
import java.text.SimpleDateFormat
import java.util.Locale

// DIUBAH: Hapus parameter dari konstruktor
class HistoryDetailAdapter : ListAdapter<ImageCaptureItem, HistoryDetailAdapter.DetailViewHolder>(DiffCallback) {

    private val serverBaseUrl = BuildConfig.BASE_IMG_URL

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemUnfocusedCaptureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailViewHolder(binding, serverBaseUrl)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class DetailViewHolder(
        private val binding: ItemUnfocusedCaptureBinding,
        private val serverBaseUrl: String
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ImageCaptureItem) {
            val fullImageUrl = serverBaseUrl + item.imageUrl
            Glide.with(itemView.context)
                .load(fullImageUrl)
                .placeholder(R.drawable.loader_icon)
                .error(R.drawable.loader_icon)
                .into(binding.imageViewCapture)

            binding.textViewCaptureTimestamp.text = formatTimestampToTimeOnly(item.captureTime)
        }

        private fun formatTimestampToTimeOnly(apiTimestamp: String): String {
            return try {
                apiTimestamp.split(" ")[1]
            } catch (e: Exception) {
                apiTimestamp
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ImageCaptureItem>() {
            override fun areItemsTheSame(oldItem: ImageCaptureItem, newItem: ImageCaptureItem): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: ImageCaptureItem, newItem: ImageCaptureItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}