package com.surendramaran.yolov8tflite.data.network

import com.surendramaran.yolov8tflite.data.model.*

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("api/v1/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/analytics/capture")
    suspend fun postAnalyticsCapture(@Body request: AnalyticsCaptureRequest): Response<AnalyticsCaptureResponse>

    @Multipart
    @POST("api/v1/image/captures")
    suspend fun postImageCapture(
        @Part("analytic_id") analyticId: RequestBody,
        @Part("capture_time") captureTime: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ImageCaptureResponse>

    @GET("api/v1/analytics/history")
    suspend fun getHistoryList(): Response<HistoryListResponse>

    @GET("api/v1/image/captures/{id}")
    suspend fun getImageCapturesForSession(@Path("id") analyticId: Int): Response<ImageCaptureListResponse>

    @DELETE("api/v1/analytics/history/{id}")
    suspend fun deleteHistoryItem(@Path("id") historyId: Int): Response<GenericApiResponse>

    @POST("api/v1/logout")
    suspend fun logout(): Response<GenericApiResponse>

    @GET("api/v1/analytics/{id}/analyze")
    suspend fun getAiSummary(@Path("id") analyticId: Int): Response<AiSummaryResponse>

    @GET("api/v1/news")
    suspend fun getNews(): Response<NewsResponse>

    @GET("api/v1/test/server")
    suspend fun checkServerStatus(): Response<GenericApiResponse>

    @GET("api/v1/schedules")
    suspend fun getSchedules(): Response<List<Schedule>>

    @POST("api/v1/schedules")
    suspend fun createSchedule(@Body request: CreateScheduleRequest): Response<Schedule>

    @DELETE("api/v1/schedules/{uuid}")
    suspend fun deleteSchedule(@Path("uuid") uuid: String): Response<GenericApiResponse>

}