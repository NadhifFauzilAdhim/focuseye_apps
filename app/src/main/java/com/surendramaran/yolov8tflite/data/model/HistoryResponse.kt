package com.surendramaran.yolov8tflite.data.model

import com.google.gson.annotations.SerializedName

data class HistoryListResponse(
    val success: Boolean,
    val data: List<HistoryItem>?
)

data class GenericApiResponse(
    val success: Boolean,
    val message: String
)

data class HistoryItem(
    val id: Int,
    val duration: Int,
    @SerializedName("focus_duration")
    val focusDuration: Int,
    @SerializedName("unfocus_duration")
    val unfocusDuration: Int,
    @SerializedName("created_at")
    val createdAt: String
)

data class ImageCaptureListResponse(
    val success: Boolean,
    val data: List<ImageCaptureItem>?
)

data class ImageCaptureItem(
    val id: Int,
    @SerializedName("analytic_id")
    val analyticId: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("capture_time")
    val captureTime: String
)

data class AiSummaryResponse(
    @SerializedName("ai_summary")
    val aiSummary: String?
)


//data class NewsResponse(
//    val status: String,
//    val totalResults: Int,
//    val articles: List<Article>?
//)
//
//data class Article(
//    val source: Source?,
//    val author: String?,
//    val title: String?,
//    val description: String?,
//    val url: String?,
//    val urlToImage: String?,
//    val publishedAt: String?,
//    val content: String?
//)
//
//data class Source(
//    val id: String?,
//    val name: String?
//)