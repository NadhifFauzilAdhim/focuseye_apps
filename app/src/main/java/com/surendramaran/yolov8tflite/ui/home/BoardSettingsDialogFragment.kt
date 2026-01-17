package com.surendramaran.yolov8tflite.ui.home

import com.surendramaran.yolov8tflite.R

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.surendramaran.yolov8tflite.databinding.DialogBoardSettingsBinding
import com.surendramaran.yolov8tflite.ui.schedule.AreaSelectionActivity

class BoardSettingsDialogFragment : DialogFragment() {

    interface BoardSettingsListener {
        fun onBoardSettingsSaved(
            x1: Float, y1: Float, x2: Float, y2: Float,
            detectionMode: String, scaleFactor: Float, skipFrames: Int,
            phoneAlertEnabled: Boolean,
            unfocusedAlertEnabled: Boolean
        )
        fun onLogoutClicked()
    }

    private var listener: BoardSettingsListener? = null
    private lateinit var binding: DialogBoardSettingsBinding

    private val areaSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Area papan tulis berhasil diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? BoardSettingsListener
            ?: throw ClassCastException("$context must implement BoardSettingsListener")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogBoardSettingsBinding.inflate(LayoutInflater.from(context))

        setupViews()
        loadCurrentSettings()
        binding.buttonSetArea.setOnClickListener {
            val intent = Intent(requireContext(), AreaSelectionActivity::class.java)
            areaSelectionLauncher.launch(intent)
        }

        binding.buttonDialogCancel.setOnClickListener { dismiss() }
        binding.buttonDialogSave.setOnClickListener { saveSettings() }
        binding.buttonDialogLogout.setOnClickListener {
            listener?.onLogoutClicked()
            dismiss()
        }

        val dialog = AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    private fun setupViews() {
        val detectionModes = resources.getStringArray(R.array.detection_modes)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, detectionModes)
        binding.autoCompleteDetectionMode.setAdapter(adapter)
    }

    private fun loadCurrentSettings() {
        val sharedPrefs = activity?.getSharedPreferences("AppGlobalSettings", Context.MODE_PRIVATE) ?: return
        val savedDetectionMode = sharedPrefs.getString("detection_mode", "Both")
        binding.autoCompleteDetectionMode.setText(savedDetectionMode, false)
        val savedScaleFactor = sharedPrefs.getFloat("scale_factor", 1.0f)
        binding.sliderScaleFactor.value = savedScaleFactor
        binding.editTextSkipFrames.setText(sharedPrefs.getInt("skip_frames", 1).toString())
        binding.switchPhoneAlert.isChecked = sharedPrefs.getBoolean("phone_alert_enabled", true)
        binding.switchUnfocusedAlert.isChecked = sharedPrefs.getBoolean("unfocused_alert_enabled", true)
    }

    private fun saveSettings() {
        val sharedPrefs = activity?.getSharedPreferences("AppGlobalSettings", Context.MODE_PRIVATE) ?: return
        try {
            val x1 = sharedPrefs.getFloat("board_x1", 0.25f)
            val y1 = sharedPrefs.getFloat("board_y1", 0.15f)
            val x2 = sharedPrefs.getFloat("board_x2", 0.75f)
            val y2 = sharedPrefs.getFloat("board_y2", 0.40f)

            val selectedDetectionMode = binding.autoCompleteDetectionMode.text.toString()
            val scaleFactor = binding.sliderScaleFactor.value
            val skipFrames = binding.editTextSkipFrames.text.toString().toInt()
            val phoneAlertEnabled = binding.switchPhoneAlert.isChecked
            val unfocusedAlertEnabled = binding.switchUnfocusedAlert.isChecked

            sharedPrefs.edit().apply {
                putString("detection_mode", selectedDetectionMode)
                putFloat("scale_factor", scaleFactor)
                putInt("skip_frames", skipFrames)
                putBoolean("phone_alert_enabled", phoneAlertEnabled)
                putBoolean("unfocused_alert_enabled", unfocusedAlertEnabled)
                apply()
            }

            listener?.onBoardSettingsSaved(x1, y1, x2, y2, selectedDetectionMode, scaleFactor, skipFrames, phoneAlertEnabled, unfocusedAlertEnabled)
            Toast.makeText(context, "Pengaturan disimpan", Toast.LENGTH_SHORT).show()
            dismiss()

        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Format angka pada input tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        const val TAG = "BoardSettingsDialog"
    }
}