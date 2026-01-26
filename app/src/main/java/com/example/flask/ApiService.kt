package com.example.flask // 또는 com.example.flask

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// ▼▼▼ 1. 서버의 새 JSON 응답과 일치하는 데이터 클래스로 변경 ▼▼▼
data class PredictionResponse(
    val detections: List<Detection>? = null,
    val error: String? = null
)

data class Detection(
    val classId: Int,
    val className: String, // 'class_name' -> 'className' (서버 JSON 키와 일치)
    val confidence: Float,
    val boxNormalized: List<Float> // 'box' -> 'boxNormalized' (서버 JSON 키와 일치)
)
// ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

// 2. Retrofit 통신 인터페이스
interface ApiService {
    @Multipart
    // ▼▼▼ 3. 서버의 새 엔드포인트 주소로 변경 ▼▼▼
    @POST("detect") // "/predict" -> "detect"
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
        // ▼▼▼ 4. 응답 타입을 새 데이터 클래스로 변경 ▼▼▼
    ): Response<PredictionResponse>
}
