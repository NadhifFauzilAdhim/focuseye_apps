package com.surendramaran.yolov8tflite.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String
)