package com.surendramaran.yolov8tflite

import android.app.Application
import com.surendramaran.yolov8tflite.data.local.AppDatabase

class FocusEyeApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
