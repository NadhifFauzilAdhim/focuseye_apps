package com.surendramaran.yolov8tflite.ui.schedule

import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.data.model.ScheduleDetailRequest

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.surendramaran.yolov8tflite.databinding.ItemScheduleDetailFormBinding

class ScheduleDetailFormAdapter(
    private val detailsList: MutableList<ScheduleDetailRequest>,
    private val onItemRemoved: (position: Int) -> Unit,
    private val onDateTimeClicked: (position: Int, isStartDate: Boolean) -> Unit
) : RecyclerView.Adapter<ScheduleDetailFormAdapter.DetailFormViewHolder>() {

    inner class DetailFormViewHolder(val binding: ItemScheduleDetailFormBinding) : RecyclerView.ViewHolder(binding.root) {

        private var currentTextWatcher: TextWatcher? = null

        fun bind(detail: ScheduleDetailRequest) {
            currentTextWatcher?.let { binding.editTextDetailName.removeTextChangedListener(it) }
            currentTextWatcher?.let { binding.editTextDetailDesc.removeTextChangedListener(it) }

            binding.editTextDetailName.setText(detail.name)
            binding.editTextDetailDesc.setText(detail.description)
            binding.editTextStartDate.setText(detail.startDate)
            binding.editTextEndDate.setText(detail.endDate)

            currentTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val updatedItem = detailsList[adapterPosition].copy(
                            name = binding.editTextDetailName.text.toString(),
                            description = binding.editTextDetailDesc.text.toString()
                        )
                        detailsList[adapterPosition] = updatedItem
                    }
                }
            }
            binding.editTextDetailName.addTextChangedListener(currentTextWatcher)
            binding.editTextDetailDesc.addTextChangedListener(currentTextWatcher)

            binding.buttonRemoveDetail.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemRemoved(adapterPosition)
                }
            }
            binding.editTextStartDate.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onDateTimeClicked(adapterPosition, true)
                }
            }
            binding.editTextEndDate.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onDateTimeClicked(adapterPosition, false)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailFormViewHolder {
        val binding = ItemScheduleDetailFormBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailFormViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailFormViewHolder, position: Int) {
        holder.bind(detailsList[position])
    }

    override fun getItemCount(): Int = detailsList.size
}