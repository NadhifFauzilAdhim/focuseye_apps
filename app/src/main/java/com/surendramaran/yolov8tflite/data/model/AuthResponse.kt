package com.surendramaran.yolov8tflite.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val success: Boolean,
    val status: Int,
    val message: String,
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("token_type")
    val tokenType: String?,
    val user: User?,
    val errors: Map<String, List<String>>?
)

data class ErrorResponse(
    val message: String
)

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val avatar: String?,
    val role: String
)


data class AnalyticsCaptureRequest(
    val duration: Int,
    @SerializedName("focus_duration")
    val focusDuration: Int,
    @SerializedName("unfocus_duration")
    val unfocusDuration: Int,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String
)

data class AnalyticsCaptureResponse(
    val success: Boolean,
    val message: String,
    val data: AnalyticsData?
)

data class AnalyticsData(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val duration: Int,
)

data class ImageCaptureResponse(
    val success: Boolean,
    val message: String
)

data class CreateScheduleRequest(
    val name: String,
    val description: String,
    val details: List<ScheduleDetailRequest>
)

data class ScheduleDetailRequest(
    val name: String,
    val description: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String
)

data class Schedule(
    val id: Int,
    val uuid: String,
    val name: String,
    val description: String,
    val details: List<ScheduleDetail>
)

data class ScheduleDetail(
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("created_at")
    val createdAt: String
)
