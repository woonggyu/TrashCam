//package com.example.googlevision
//
//import android.graphics.Bitmap
//import android.util.Base64
//import android.util.Log
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody.Companion.toRequestBody
//import org.json.JSONArray
//import org.json.JSONObject
//import java.io.ByteArrayOutputStream
//import java.io.IOException
//
//// 분석 결과를 전달할 약속 (이제 간단한 데이터 쌍으로 전달)
//
//
//class VisionApiAnalyzer(private val listener: VisionAnalysisListener) {
//
//    // ⚠️⚠️⚠️ 보안 경고 ⚠️⚠️⚠️
//    // API 키를 코드에 직접 넣는 것은 매우 위험합니다!
//    // 실제 앱에서는 서버를 통해 호출하거나, 최소한 BuildConfig 등을 사용해 숨겨야 합니다.
//    private val apiKey = "AIzaSyCEiy7vxPVT0ZDnbxm19c-2y2UI-0PE8VI"
//
//    private val client = OkHttpClient()
//
//    suspend fun analyze(bitmap: Bitmap) {
//        withContext(Dispatchers.IO) {
//            try {
//                // 1. 이미지를 Base64 문자열로 인코딩
//                val base64Image = bitmapToBase64(bitmap)
//
//                // 2. Vision API가 요구하는 JSON 요청 본문 생성
//                val requestJson = createRequestJson(base64Image)
//
//                // 3. OkHttp를 사용하여 POST 요청 생성
//                val body = requestJson.toRequestBody("application/json; charset=utf-8".toMediaType())
//                val request = Request.Builder()
//                    .url("https://vision.googleapis.com/v1/images:annotate?key=$apiKey")
//                    .post(body)
//                    .build()
//
//                // 4. 네트워크 요청 실행 및 응답 처리
//                client.newCall(request).execute().use { response ->
//                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//
//                    val responseBody = response.body!!.string()
//                    val labels = parseResponse(responseBody)
//
//                    // 5. 성공 결과를 Main 스레드로 전달
//                    withContext(Dispatchers.Main) {
//                        listener.onVisionAnalyzed(labels)
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("VisionApiAnalyzer", "API Error", e)
//                // 6. 실패 결과를 Main 스레드로 전달
//                withContext(Dispatchers.Main) {
//                    listener.onVisionError(e)
//                }
//            }
//        }
//    }
//
//    private fun bitmapToBase64(bitmap: Bitmap): String {
//        val outputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
//        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
//    }
//
//    private fun createRequestJson(base64Image: String): String {
//        return JSONObject().apply {
//            put("requests", JSONArray().apply {
//                put(JSONObject().apply {
//                    put("image", JSONObject().apply {
//                        put("content", base64Image)
//                    })
//                    put("features", JSONArray().apply {
//                        put(JSONObject().apply {
//                            put("type", "LABEL_DETECTION")
//                            put("maxResults", 5) // 결과는 5개까지만 받기
//                        })
//                    })
//                })
//            })
//        }.toString()
//    }
//
//    private fun parseResponse(responseBody: String): List<Pair<String, Float>> {
//        val labels = mutableListOf<Pair<String, Float>>()
//        val labelsJson = JSONObject(responseBody)
//            .getJSONArray("responses")
//            .getJSONObject(0)
//            .optJSONArray("labelAnnotations") ?: return emptyList()
//
//        for (i in 0 until labelsJson.length()) {
//            val obj = labelsJson.getJSONObject(i)
//            val description = obj.getString("description")
//            val score = obj.getDouble("score").toFloat()
//            labels.add(description to score)
//        }
//        return labels
//    }
//}