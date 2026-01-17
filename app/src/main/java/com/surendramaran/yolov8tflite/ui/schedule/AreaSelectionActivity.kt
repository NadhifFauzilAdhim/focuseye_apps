package com.surendramaran.yolov8tflite.ui.schedule

import com.surendramaran.yolov8tflite.R

import android.content.Context
import android.content.SharedPreferences
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.surendramaran.yolov8tflite.databinding.ActivityAreaSelectionBinding

class AreaSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAreaSelectionBinding
    private lateinit var sharedPrefs: SharedPreferences
    private var isFrontCamera = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAreaSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefs = getSharedPreferences("AppGlobalSettings", Context.MODE_PRIVATE)
        startCamera()
        binding.fabSaveArea.setOnClickListener {
            saveAreaAndFinish()
        }
        binding.buttonSwitchCameraArea.setOnClickListener {
            isFrontCamera = !isFrontCamera
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewViewArea.surfaceProvider)
            }
            val cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
                loadInitialBox()
            } catch (exc: Exception) {
                Log.e("AreaSelection", "Use case binding failed", exc)
                Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun loadInitialBox() {
        binding.draggableBoxView.post {
            val currentArea = RectF(
                sharedPrefs.getFloat("board_x1", 0.25f),
                sharedPrefs.getFloat("board_y1", 0.15f),
                sharedPrefs.getFloat("board_x2", 0.75f),
                sharedPrefs.getFloat("board_y2", 0.40f)
            )
            binding.draggableBoxView.setInitialRect(currentArea)
        }
    }

    private fun saveAreaAndFinish() {
        val newArea = binding.draggableBoxView.getNormalizedRect()
        sharedPrefs.edit().apply {
            putFloat("board_x1", newArea.left)
            putFloat("board_y1", newArea.top)
            putFloat("board_x2", newArea.right)
            putFloat("board_y2", newArea.bottom)
            apply()
        }
        Toast.makeText(this, "Area papan tulis berhasil disimpan", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}